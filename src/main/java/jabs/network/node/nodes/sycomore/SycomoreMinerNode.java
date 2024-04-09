package jabs.network.node.nodes.sycomore;

import jabs.consensus.algorithm.AbstractChainBasedConsensus;
import jabs.consensus.algorithm.AbstractDAGBasedConsensus;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.Block;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumBlockWithTx;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreBlockUtils;
import jabs.ledgerdata.sycomore.SycomoreBlockWithTx;
import jabs.ledgerdata.sycomore.SycomoreTx;
import jabs.network.message.DataMessage;
import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.node.nodes.MinerNode;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.ethereum.EthereumNode;
import jabs.simulator.Simulator;
import jabs.simulator.event.BlockMiningProcess;
import jabs.simulator.randengine.RandomnessEngine;


import java.util.*;
import java.util.random.RandomGenerator;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_MIN_DIFFICULTY;

public class SycomoreMinerNode extends SycomoreNode implements MinerNode {
    RandomnessEngine randomnessEngine = new RandomnessEngine(new Random().nextInt());
    protected Set<SycomoreTx> memPool = new HashSet<>();
    //mempool, sort of waiting room for transaction still not included in a block
    protected Set<SycomoreBlock> alreadyUncledBlocks = new HashSet<>();
    protected final double hashPower;
    protected Simulator.ScheduledEvent miningProcess;
    static final long MAXIMUM_BLOCK_GAS = 12500000;
    private final int MAX_UNBALANCE = 5;
    Random rand = new Random();

    //anche qui abbiamo 2 costruttori
    public SycomoreMinerNode(Simulator simulator, Network network, int nodeID,
                             long downloadBandwidth, long uploadBandwidth, double hashPower, SycomoreBlock genesisBlock,
                             SycomoreProtocolConfig sycomoreProtocolConfig) {
        //nel primo prendiamo il genesisblock e la config del protocol
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, genesisBlock, sycomoreProtocolConfig);
        this.hashPower = hashPower;
    }

    public SycomoreMinerNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth,
                             long uploadBandwidth, double hashPower,
                             AbstractDAGBasedConsensus<SycomoreBlock, SycomoreTx> consensusAlgorithm) {
        //nel secondo prendiamo abstractchainbasedconsensus
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, consensusAlgorithm);
        this.hashPower = hashPower;
    }


    public void generateNewBlock() {
        //What do we do:
        //1 - Find all the leaves of the chain
        BlockHeader header = new BlockHeader();
        int height;
        int newBlockChainHeight;
        String newBlockLabel;

        Set<SycomoreBlock> leafBlocks = this.localBlockTree.getChildlessBlocks();

        List<SycomoreBlock> usableLeaves = usableLeaves(leafBlocks);
        //Since this is a simulation, we extract at random the predecessor
        System.err.println(usableLeaves.size());
        SycomoreBlock parentBlock = usableLeaves.get(rand.nextInt(usableLeaves.size()));
        newBlockChainHeight=parentBlock.getChainHeight();
        newBlockLabel = parentBlock.getLabel();

        //Ora dobbiamo controllare se il blocco è splittable o mergeable!


        //once the header is constructed we use POW to find the nonce v...
        //1.3 once we constructed the header we find the nonce v
        int nonce = find_nonce( header, 10);
        //here we pass the header to this function and the function will return us the
        //choosen label
        //1.4 Now we have the block to which append the new block
        LinkedList<SycomoreBlock> parents = new LinkedList<SycomoreBlock>();
        parents.add(parentBlock);
        //TODO we have to get the parents from the labels
        //TODO implement multiple blocks... in case of mergeable/splittble blocks

        //1.5 Add transactions in the block

        Set<SycomoreTx> blockTxs = new HashSet<>();
        long totalGas = 0;
        for (SycomoreTx sycomoreTx:memPool) { //for each transaction in the mempool
            if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                break;
            }
            blockTxs.add(sycomoreTx);
            totalGas += sycomoreTx.getGas();
        }

        //1.6 Create the new Block
        //The height in sycomoreblock is the legth of the label?
        SycomoreBlockWithTx sycomoreBlockWithTx = new SycomoreBlockWithTx(header,newBlockChainHeight,newBlockLabel,parents.getFirst().getHeight()+1, simulator.getSimulationTime(),
                this, parents,null, blockTxs, 0,0);
        //System.err.println("New Syco Block Created");
        //System.err.println("Label: "+ sycomoreBlockWithTx.getLabel()+ "* from miner: " + sycomoreBlockWithTx.getCreator() + "height: " + sycomoreBlockWithTx.getHeight());
        //1.7 process the new block
        this.processIncomingPacket(
                new Packet(
                        this, this, new DataMessage(sycomoreBlockWithTx)
                )
        );
    }
    /**
     *
     */
    @Override
    public void startMining() {
        System.err.println("Startmining called");
        //here you have to implement the average time between blocks
        BlockMiningProcess blockMiningProcess = new BlockMiningProcess(this.simulator, this.network.getRandom(),
                4, this);
        this.miningProcess = this.simulator.putEvent(blockMiningProcess, blockMiningProcess.timeToNextGeneration());
    }

    /**
     *
     */
    @Override
    public void stopMining() {
        simulator.removeEvent(this.miningProcess);
    }

    public double getHashPower() {
        return this.hashPower;
    }
    private  List<SycomoreBlock> usableLeaves ( Set<SycomoreBlock> leafBlocks) {
        //Since we want to avoid the growth of just one chain...
        //the sycomore protocol allows to append new blocks only on chains which are not
        //so long
        LinkedList<SycomoreBlock> usableLeaves = new LinkedList<SycomoreBlock>();
        int minChainHeight = Integer.MAX_VALUE;
        for (SycomoreBlock leaf : leafBlocks){
            if(leaf.getHeight()<minChainHeight)
                minChainHeight = leaf.getHeight();
        }

        for (SycomoreBlock leaf : leafBlocks){
            if(leaf.getHeight()<minChainHeight+5)
                usableLeaves.add(leaf);
        }

        return usableLeaves;
    }

    @Override
    protected void processNewTx(SycomoreTx sycomoreTx, Node from) {
        // add to memPool
        memPool.add((SycomoreTx) sycomoreTx);

        this.broadcastTransaction((SycomoreTx) sycomoreTx, from);
    }

    @Override
    protected void processNewBlock(SycomoreBlock sycomoreBlock) {
        //this is the function to process a new block
        //the consensusalgorithm processes the new block
        this.consensusAlgorithm.newIncomingBlock(sycomoreBlock);


        //alreadyUncledBlocks.addAll(sycomoreBlock.getUncles());

        // remove from memPool

        if (sycomoreBlock instanceof SycomoreBlockWithTx) {
            for (SycomoreTx sycomoreTx: ((SycomoreBlockWithTx) sycomoreBlock).getTxs()) {
                memPool.remove(sycomoreTx); // TODO: This should be changed. Ethereum reverts Txs from non canonical chain
            }
        }

        this.broadcastNewBlockAndBlockHashes(sycomoreBlock);
    }
    private String compute_l_j (SycomoreBlock block) {
        //here we compute the label of the next block to be appended
        String label = block.getLabel().toString();
        if(block.isSplittable()){
            label +='0';
        }
        if (block.isMergeable()){
            label = label.substring(0, label.length());
        }

        return label;
    }
    private String compute_m (){
        //we have to extract the locally pending transactions whose identifier is prefixed by l_i
        //by now we generate this number at random
        //But since in this case we do not want to simulate the security of the network we can just insert
        // a random number
        return String.valueOf(randomnessEngine.nextInt()).substring(0, 4);
    }
    private int find_nonce(BlockHeader header, int difficulty){
        //We should find a nonce v such that hash(H|v)<T, where T depends on difficulty.
        //Since we don't want to do the effort to find the nonce, but we still want it to
        //depend on the header we do the following.
        int resultLength=10;
        String input = header.toString();
        StringBuilder transformedString = new StringBuilder();


        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            // Apply the deterministic transformation
            char transformedChar = (char) (currentChar + 1);
            transformedString.append(transformedChar);
        }

        // Ensure the length of the transformed string is equal to resultLength
        while (transformedString.length() < resultLength) {
            transformedString.append('0'); // Pad with zeros if necessary
        }

        // Truncate or pad if necessary to ensure the length matches resultLength
        transformedString.setLength(resultLength);

        // Calculate the hash code of the transformed string
        int result = Math.abs(transformedString.toString().hashCode());

        return result;

    }
    private LinkedList<SycomoreBlock> find_predecessors(BlockHeader header, short nonce){
        //s is the number of bit of the longest successor label
        //Here we get the predecessor, or predecessor, in case of 2 mergeable blocks,
        //but don't add any reference to the block header.
        LinkedList<SycomoreBlock> parents = new LinkedList<SycomoreBlock>();
            int s =2;
            long b = Long.parseLong(String.valueOf(header.hashCode()).concat(String.valueOf(nonce))) % 2^s;
        int minDistance = Integer.MAX_VALUE; // Initialize with maximum value
        BlockHeaderEntry closestElement = null; // Initialize with null
        ArrayList<BlockHeaderEntry> headers = header.getHeadersList();
        for (BlockHeaderEntry element : headers) {
            int distance = SycomoreBlockUtils.binaryDistance(element.getLabel(), String.valueOf(b)); // Call your distance function
            if (distance < minDistance) {
                minDistance = distance;
                closestElement = element;
            }
        }
        closestElement.getHash().getData();
        //Verifica
        parents.add((SycomoreBlock)closestElement.getHash().getData());

        return parents;


    }
}
