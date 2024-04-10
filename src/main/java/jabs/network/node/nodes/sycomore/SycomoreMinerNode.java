package jabs.network.node.nodes.sycomore;

import jabs.consensus.algorithm.AbstractChainBasedConsensus;
import jabs.consensus.algorithm.AbstractDAGBasedConsensus;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.Block;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumBlockWithTx;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.sycomore.*;
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

    Set<SycomoreTx> blockTxs = new HashSet<>();

    //anche qui abbiamo 2 costruttori
    public SycomoreMinerNode(Simulator simulator, Network network, int nodeID,
                             long downloadBandwidth, long uploadBandwidth, double hashPower, SycomoreBlock genesisBlock,
                             SycomoreProtocolConfig sycomoreProtocolConfig) {
        //nel primo prendiamo il genesisblock e la config del protocol
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, genesisBlock, sycomoreProtocolConfig);
        this.hashPower = hashPower;
        blockTxs = new HashSet<>();
        System.err.println("Hi, this is sycomore node: " + this.toString());

    }

    public SycomoreMinerNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth,
                             long uploadBandwidth, double hashPower,
                             AbstractDAGBasedConsensus<SycomoreBlock, SycomoreTx> consensusAlgorithm) {
        //nel secondo prendiamo abstractchainbasedconsensus
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, consensusAlgorithm);
        this.hashPower = hashPower;
        blockTxs = new HashSet<>();
    }


    public void generateNewBlock() {

        System.err.println("Generate new Block called!");

        //IMPORTANT, since we cannot create an HASHMAP in the consensus algo with an empty string
        //as key, we have to handle the thing.
        //IDEA: each label starts with "ε", we and we cut it when we compute binary_distance
        //We'll never meet the condition to remove "ε"
        //What do we do:
        //1 - Find all the leaves of the chain
        BlockHeader header = new BlockHeader();
        int height;
        int newBlockChainLabel;
        int newBlockTotalHeight;
        int newBlockHeightInChain;
        String newBlockLabel;

        Set<SycomoreBlock> leafBlocks = this.localBlockTree.getChildlessBlocks();

        System.err.println("leaf Blocks: " + leafBlocks.toString());

        List<SycomoreBlock> usableLeaves = usableLeaves(leafBlocks);
        //Since this is a simulation, we extract at random the predecessor
        System.err.println("Usable leaves: "+ String.valueOf(usableLeaves.size()));
        SycomoreBlock parentBlock = usableLeaves.get(rand.nextInt(usableLeaves.size()));
        LinkedList<SycomoreBlock> newBlockParents = new LinkedList<SycomoreBlock>();
        if(parentBlock.isSplittable()){
            System.err.println("if splittable ");


            //The block is splittable, so we can produce 2 child blocks:
            newBlockParents.add(parentBlock);

            //BLOCK 1:
            //Header??
            newBlockLabel = parentBlock.getLabel().concat("0");
            newBlockHeightInChain=0; //because we are starting a new Chain
            newBlockTotalHeight = parentBlock.getTotalHeight()+1;
            this.blockTxs = new HashSet<>();
            long totalGas = 0;
            for (SycomoreTx sycomoreTx:memPool) { //for each transaction in the mempool
                if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                    break;
                }
                blockTxs.add(sycomoreTx);
                totalGas += sycomoreTx.getGas();
            }
            SycomoreBlockWithTx newSycoBlockWithTX = new SycomoreBlockWithTx(new BlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,null, blockTxs,0,0);
            spreadBlock(newSycoBlockWithTX);
            //BLOCK 2
            //Header??
            newBlockLabel = parentBlock.getLabel().concat("1");
            newBlockHeightInChain=0; //because we are starting a new Chain
            newBlockTotalHeight = parentBlock.getTotalHeight()+1;
            this.blockTxs = new HashSet<>();
            totalGas = 0;
            for (SycomoreTx sycomoreTx:memPool) { //for each transaction in the mempool
                if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                    break;
                }
                blockTxs.add(sycomoreTx);
                totalGas += sycomoreTx.getGas();
            }
            newSycoBlockWithTX = new SycomoreBlockWithTx(new BlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,null, blockTxs,0,0);
            spreadBlock(newSycoBlockWithTX);


        }

        if(parentBlock.isMergeable()){
            System.err.println("if mergeable ");

            //Se il blocco è mergeable:
            //1: Controlliamo se anche il fratello è mergeable, se no lo trattiamo come un blocco normale
            //TODO controllare che getleaf funzioni bene, anche per blocchi con piu parent
            //Se tutti e 2 sono mergeable, creiamo un nuovo blocco con quei 2 blocchi come parents
            SycomoreBlock brother = find_brother(parentBlock);
            if (brother!=null){
                //Quindi esiste unn fratello mergeable
                newBlockParents.add(parentBlock);
                newBlockParents.add(brother);
                newBlockLabel = parentBlock.getLabel().substring(0, parentBlock.getLabel().length() - 1);
                newBlockHeightInChain=0; //because we are starting a new Chain
                newBlockTotalHeight = parentBlock.getTotalHeight()+1;

                this.blockTxs = new HashSet<>();
                long totalGas = 0;
                for (SycomoreTx sycomoreTx:memPool) { //for each transaction in the mempool
                    if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                        break;
                    }
                    blockTxs.add(sycomoreTx);
                    totalGas += sycomoreTx.getGas();
                }
                SycomoreBlockWithTx newSycoBlockWithTX = new SycomoreBlockWithTx(new BlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,null, blockTxs,0,0);
                spreadBlock(newSycoBlockWithTX);

            }}

            //The block is not splittable nor mergeable
            else{
                System.err.println("The block is not splittable nor mergeable: ");

                newBlockParents.add(parentBlock);
                newBlockLabel = parentBlock.getLabel();
                newBlockHeightInChain= parentBlock.getHeightInChain()+1; //because we are starting a new Chain
                newBlockTotalHeight = parentBlock.getTotalHeight()+1;

                this.blockTxs = new HashSet<>();
                long totalGas = 0;
                for (SycomoreTx sycomoreTx:memPool) { //for each transaction in the mempool
                    if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                        break;
                    }
                    blockTxs.add(sycomoreTx);
                    totalGas += sycomoreTx.getGas();
                }
                SycomoreBlockWithTx newSycoBlockWithTX = new SycomoreBlockWithTx(new BlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,null, blockTxs,0,0);
                spreadBlock(newSycoBlockWithTX);
            }





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

    private SycomoreBlock find_brother(SycomoreBlock mergeable_block){
        return this.localBlockTree.getAllBlocks().stream().filter(block ->((block.getLabel().length()==mergeable_block.getLabel().length()) && SycomoreBlockUtils.binaryDistance(mergeable_block.getLabel(),block.getLabel())==1) && (mergeable_block.getTotalHeight()==block.getTotalHeight())).toList().get(0);

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

    private void spreadBlock(SycomoreBlock sycomoreBlockWithTx){
        this.processIncomingPacket(
                new Packet(
                        this, this, new DataMessage(sycomoreBlockWithTx)
                )
        );

    }



}
