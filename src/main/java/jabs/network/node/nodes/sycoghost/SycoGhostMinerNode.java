package jabs.network.node.nodes.sycoghost;

import jabs.consensus.algorithm.AbstractDAGBasedConsensus;
import jabs.consensus.algorithm.SycoGhostProtocol;
import jabs.consensus.algorithm.SycomoreConsensusAlgorithm;
import jabs.consensus.config.SycoGhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.sycoghost.SycoGhostBlock;
import jabs.ledgerdata.sycoghost.SycoGhostBlockWithTx;
import jabs.ledgerdata.sycoghost.SycoGhostTx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreBlockUtils;
import jabs.ledgerdata.sycomore.SycomoreBlockWithTx;
import jabs.ledgerdata.sycomore.SycomoreTx;
import jabs.network.message.DataMessage;
import jabs.network.message.Packet;
import jabs.network.networks.GlobalProofOfWorkNetwork;
import jabs.network.networks.Network;
import jabs.network.node.nodes.MinerNode;
import jabs.network.node.nodes.Node;
import jabs.simulator.Simulator;
import jabs.simulator.event.BlockMiningProcess;
import jabs.simulator.event.TxGenerationProcessSingleNode;
import jabs.simulator.randengine.RandomnessEngine;

import java.util.*;

public class SycoGhostMinerNode extends SycoGhostNode implements MinerNode {
    RandomnessEngine randomnessEngine = new RandomnessEngine(new Random().nextInt());
    protected Set<SycoGhostTx> memPool = new HashSet<>();
    //mempool, sort of waiting room for transaction still not included in a block
    protected Set<SycoGhostBlock> alreadyUncledBlocks = new HashSet<>();
    protected double hashPower;
    protected Simulator.ScheduledEvent miningProcess;
    static final long MAXIMUM_BLOCK_GAS = 12500000;
    private final int MAX_UNBALANCE = 5;
    Random rand = new Random();
    private TxGenerationProcessSingleNode txGenerationProcessSingleNode;
    private BlockMiningProcess blockMiningProcess;
    private final int H_MAX = 18;
    int currentNumUncles;
    private int last_diff_adjustment_height;
    private int generate_block_counter = 0;



    Set<SycoGhostTx> blockTxs = new HashSet<>();

    //anche qui abbiamo 2 costruttori
    public SycoGhostMinerNode(Simulator simulator, Network network, int nodeID,
                              long downloadBandwidth, long uploadBandwidth, double hashPower, SycoGhostBlock genesisBlock,
                              SycoGhostProtocolConfig sycomoreProtocolConfig) {
        //nel primo prendiamo il genesisblock e la config del protocol
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, genesisBlock, sycomoreProtocolConfig);
        this.hashPower = hashPower;
        blockTxs = new HashSet<>();
        currentNumUncles=0;
        last_diff_adjustment_height=0;
        //System.err.println("Hi, this is sycomore node: " + this.toString());
//      txGenerationProcessSingleNode = new TxGenerationProcessSingleNode(this.getSimulator(), randomnessEngine, this,1);

    }

    public SycoGhostMinerNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth,
                              long uploadBandwidth, double hashPower,
                              AbstractDAGBasedConsensus<SycoGhostBlock, SycoGhostTx> consensusAlgorithm) {
        //nel secondo prendiamo abstractchainbasedconsensus
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, consensusAlgorithm);
        this.hashPower = hashPower;
        blockTxs = new HashSet<>();
        currentNumUncles=0;
        last_diff_adjustment_height=0;
    }


    public void generateNewBlock() {
        //System.err.println("Generate new block called, time to next generation: " + blockMiningProcess.timeToNextGeneration() + " on node: " + this.nodeID + "hashpower: " + this.hashPower);

        generate_block_counter ++;
        //System.err.println("Generate block counter: " + generate_block_counter + " on node: " + this.nodeID);
        //System.err.println("-------GENERATE BLOCK CALLED Block time: " + this.blockMiningProcess.averageTimeBetweenGenerations + " called for time: " + generate_block_counter + "on node: " + this.nodeID);
        //System.err.println("Generate new Block called!");

        //IMPORTANT, since we cannot create an HASHMAP in the consensus algo with an empty string
        //as key, we have to handle the thing.
        //IDEA: each label starts with "ε", we and we cut it when we compute binary_distance
        //We'll never meet the condition to remove "ε"
        //What do we do:
        //1 - Find all the leaves of the chain

        generateTransactionSet();

        Set<SycoGhostBlock> tipBlocks = this.localBlockTree.getChildlessBlocks();
        tipBlocks.removeAll(((SycoGhostProtocol) this.consensusAlgorithm).getChainHeads());
        tipBlocks.removeAll(alreadyUncledBlocks);

        //System.err.println("In the mempool there are: "+ memPool.size() + " transactions" );

        SGBlockHeader header = new SGBlockHeader();
        int newBlockChainLabel;
        int newBlockTotalHeight;
        int newBlockHeightInChain;
        String newBlockLabel;

        Set<SycoGhostBlock> leafBlocks = this.localBlockTree.getChildlessBlocks();

        Set<SycoGhostBlock> usableLeaves = usableLeaves(leafBlocks);
        //Since this is a simulation, we extract at random the predecessor
        //System.err.println("Usable leaves: "+ String.valueOf(usableLeaves.size()));
        SycoGhostBlock parentBlock = new ArrayList<SycoGhostBlock>(usableLeaves).get(rand.nextInt(usableLeaves.size()));
        LinkedList<SycoGhostBlock> newBlockParents = new LinkedList<SycoGhostBlock>();
        if(parentBlock.isSplittable()){
            System.err.println("******************************BLOCK SPLITTABLE ");
            //The block is splittable, so we can produce 2 child blocks:
            newBlockParents.add(parentBlock);

            //BLOCK 1:
            //Header??
            newBlockLabel = parentBlock.getLabel().concat("0");
            newBlockHeightInChain=0; //because we are starting a new Chain
            newBlockTotalHeight = parentBlock.getTotalHeight()+1;
            this.blockTxs = new HashSet<>();
            long totalGas = 0;
            for (SycoGhostTx sycomoreTx:memPool) { //for each transaction in the mempool
                if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                    break;
                }
                blockTxs.add(sycomoreTx);
                totalGas += sycomoreTx.getGas();
            }
            SycoGhostBlockWithTx newSycoBlockWithTX = new SycoGhostBlockWithTx(new SGBlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,tipBlocks, blockTxs,0,0);
            spreadBlock(newSycoBlockWithTX);
            //BLOCK 2
            //Header??
            newBlockLabel = parentBlock.getLabel().concat("1");
            newBlockHeightInChain=0; //because we are starting a new Chain
            newBlockTotalHeight = parentBlock.getTotalHeight()+1;
            this.blockTxs = new HashSet<>();
            totalGas = 0;
            for (SycoGhostTx sycomoreTx:memPool) { //for each transaction in the mempool
                //System.err.println("ADDING TRANSACTION");
                if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                    break;
                }
                blockTxs.add(sycomoreTx);
                totalGas += sycomoreTx.getGas();
            }
            //System.err.println("In the block there are: "+ blockTxs.size() + " transactions" );

            newSycoBlockWithTX = new SycoGhostBlockWithTx(new SGBlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,tipBlocks, blockTxs,0,0);
            spreadBlock(newSycoBlockWithTX);
            //updateAverageTimeBetweenBlocks(600/countUniqueLabels(usableLeaves));


        }

        if(parentBlock.isMergeable() && this.countUniqueLabels(leafBlocks)>1){
            System.err.println("*******************if mergeable ");

            //Se il blocco è mergeable:
            //1: Controlliamo se anche il fratello è mergeable, se no lo trattiamo come un blocco normale
            //TODO controllare che getleaf funzioni bene, anche per blocchi con piu parent
            //Se tutti e 2 sono mergeable, creiamo un nuovo blocco con quei 2 blocchi come parents
            SycoGhostBlock brother = find_brother(parentBlock);
            if (brother!=null){
                //Quindi esiste unn fratello mergeable
                newBlockParents.add(parentBlock);
                newBlockParents.add(brother);
                newBlockLabel = parentBlock.getLabel().substring(0, parentBlock.getLabel().length() - 1);
                newBlockHeightInChain=0; //because we are starting a new Chain
                newBlockTotalHeight = parentBlock.getTotalHeight()+1;

                this.blockTxs = new HashSet<>();
                long totalGas = 0;
                for (SycoGhostTx sycomoreTx:memPool) { //for each transaction in the mempool
                    if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                        break;
                    }
                    blockTxs.add(sycomoreTx);
                    totalGas += sycomoreTx.getGas();
                }
                SycoGhostBlockWithTx newSycoBlockWithTX = new SycoGhostBlockWithTx(new SGBlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,tipBlocks, blockTxs,0,0);
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
                for (SycoGhostTx sycomoreTx:memPool) { //for each transaction in the mempool
                    if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                        break;
                    }
                    blockTxs.add(sycomoreTx);
                    totalGas += sycomoreTx.getGas();
                }

            //System.err.println("456In the block there are: "+ blockTxs.size() + " transactions" );
            SycoGhostBlock newSycoGhostBlockWithTX = new SycoGhostBlockWithTx(new SGBlockHeader(),newBlockLabel,newBlockHeightInChain,newBlockTotalHeight,simulator.getSimulationTime(),this, newBlockParents,tipBlocks, blockTxs,0,0);
                spreadBlock(newSycoGhostBlockWithTX);
            }
}

    private void generateTransactionSet() {
        //int scaleFactor = 1;
        //We use this method only to speed up simulation:
        //What we should would be generate transaction by the TxGenerationProcess, but it
        //this case the simulation would take an infinite time.
        //Since out aim is to understand how the protocol behaves in a specific load situation,
        //we can simply fill memPool this way.
        //int[] load_array=SycomoreNodeUtils.populateArray()*scaleFactor;
        //int[] load_array=SycomoreNodeUtils.populateArray();
        //HashMap<Integer,Integer> map = SycomoreNodeUtils.arrayToMap(load_array);
        //while(this.getSimulator().getSimulationTime()<map.ge)
        for (int i = 1; i <= 10; i++) {
            memPool.add((SycoGhostTx) new SycoGhostTx(3000, 22000));
        }
    }

    /**
     *
     */
    @Override
    public void startMining() {
        //System.err.println("Startmining called");
        //here you have to implement the average time between blocks
        SycoGhostProtocol consensusAlgorithm = (SycoGhostProtocol) this.getConsensusAlgorithm();
        this.blockMiningProcess = new BlockMiningProcess(this.simulator, this.network.getRandom(),
                2097/(double) this.hashPower, this);
        this.miningProcess = this.simulator.putEvent(blockMiningProcess, blockMiningProcess.timeToNextGeneration());
        System.err.println("Startmining called on node: " + this.nodeID + "hashpower: " + this.hashPower + "average time between blocks: "+blockMiningProcess.averageTimeBetweenGenerations);

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
    private  Set<SycoGhostBlock> usableLeaves ( Set<SycoGhostBlock> leafBlocks) {
        //HERE USABLE LEAVES HAS TO DO 2 THINGS.
        //1: FIND THE MAIN CHAIN FOR EACH LABEL
        //2: SELECT ONLY THE CHAINS WE CAN APPEND THE BLOCK TO BECAUSE OF THE LENGTH OF THE CHAIN

        //1: FIND THE MAIN CHAIN FOR EACH LABEL
        Map<String, SycoGhostBlock> maxWeightBlocksByLabel = new HashMap<>();

        for (SycoGhostBlock block : leafBlocks) {
            String label = block.getLabel();
            SycoGhostProtocol consensusAlgorithm = (SycoGhostProtocol) this.getConsensusAlgorithm();
            double curr_block_weight = consensusAlgorithm.getWeight(block);
            // Check if this label already exists in the map
            if (maxWeightBlocksByLabel.containsKey(label)) {
                // If the current block has higher weight, update the map
                if (curr_block_weight > consensusAlgorithm.getWeight(maxWeightBlocksByLabel.get(label))) {
                    maxWeightBlocksByLabel.put(label, block);
                }
            } else {
                // If this label doesn't exist in the map, add it
                maxWeightBlocksByLabel.put(label, block);
            }
        }

        // Now maxWeightBlocksByLabel contains the block with maximum weight for each label,
        HashSet<SycoGhostBlock> uniqueMaxWeightBlocks = new HashSet<>(maxWeightBlocksByLabel.values());
        //System.err.println("Unique max weight blocks: "+ uniqueMaxWeightBlocks.size());

        //2: SELECT ONLY THE CHAINS WE CAN APPEND THE BLOCK TO BECAUSE OF THE LENGTH OF THE CHAIN

        HashSet<SycoGhostBlock> usableLeaves = new HashSet<>();
        for (SycoGhostBlock leaf : uniqueMaxWeightBlocks){
            //System.err.println("Total height: "+ leaf.getTotalHeight() + "Last diff adjustment height: "+ last_diff_adjustment_height + "H_MAX: "+ H_MAX + "Difference: "+ (leaf.getTotalHeight()-last_diff_adjustment_height));
            if((leaf.getTotalHeight()-last_diff_adjustment_height)<H_MAX){
                usableLeaves.add(leaf);
            //System.err.println("Usable leaves size in if: "+ usableLeaves.size());
            //System.err.println("If triggered!");
                }
        }
        //If there are no usable leaves, all the leaves have reached the re_adjustment height, so we
        //can trigger the difficulty adjustment
        //System.err.println("Usable leaves: "+ usableLeaves.size());
        if(usableLeaves.isEmpty()){
            int uniqueLabels = countUniqueLabels(leafBlocks);
            updateAverageTimeBetweenBlocks((double) ((SycoGhostProtocol) this.getConsensusAlgorithm()).averageBlockMiningInterval /uniqueLabels);
            System.err.println("Updating average time between blocks, new time: " + ((SycoGhostProtocol) this.getConsensusAlgorithm()).averageBlockMiningInterval/uniqueLabels + "number of chains: "+ uniqueLabels + "currect chain height: "+ leafBlocks.iterator().next().getTotalHeight());
            last_diff_adjustment_height= leafBlocks.iterator().next().getTotalHeight();
            usableLeaves.addAll(uniqueMaxWeightBlocks);
        }

        return usableLeaves;
    }

    private SycoGhostBlock find_brother(SycoGhostBlock mergeable_block){

        Set<SycoGhostBlock> blocks = this.localBlockTree.getAllBlocks();
        List<SycoGhostBlock> filteredBlocks = new ArrayList<>();

        for (SycoGhostBlock block : blocks) {
            if (block.getLabel().length() == mergeable_block.getLabel().length() &&
                    SycomoreBlockUtils.binaryDistance(mergeable_block.getLabel(), block.getLabel()) == 1 &&
                    mergeable_block.getTotalHeight() == block.getTotalHeight()) {
                filteredBlocks.add(block);
            }
        }
        if (!filteredBlocks.isEmpty())
            return filteredBlocks.get(0);
        return null;
        //return this.localBlockTree.getAllBlocks().stream().filter(block ->((block.getLabel().length()==mergeable_block.getLabel().length()) && SycomoreBlockUtils.binaryDistance(mergeable_block.getLabel(),block.getLabel())==1) && (mergeable_block.getTotalHeight()==block.getTotalHeight())).toList().get(0);

    }

    @Override
    protected void processNewTx(SycoGhostTx sycomoreTx, Node from) {
        System.err.println("123 Process new transaction syco called");

        // add to memPool
        memPool.add((SycoGhostTx) sycomoreTx);

        this.broadcastTransaction((SycoGhostTx) sycomoreTx, from);
    }

    @Override
    protected void processNewBlock(SycoGhostBlock sycomoreBlock) {
        //this is the function to process a new block
        //the consensusalgorithm processes the new block
        this.consensusAlgorithm.newIncomingBlock(sycomoreBlock);
        alreadyUncledBlocks.addAll(sycomoreBlock.getUncles());



        //alreadyUncledBlocks.addAll(sycomoreBlock.getUncles());

        // remove from memPool

        if (sycomoreBlock instanceof SycoGhostBlockWithTx) {
            for (SycoGhostTx sycomoreTx: ((SycoGhostBlockWithTx) sycomoreBlock).getTxs()) {
                memPool.remove(sycomoreTx); // TODO: This should be changed. Ethereum reverts Txs from non canonical chain
            }
        }

        this.broadcastNewBlockAndBlockHashes(sycomoreBlock);
    }


    private void spreadBlock(SycoGhostBlock sycomoreBlockWithTx){
        this.processIncomingPacket(
                new Packet(
                        this, this, new DataMessage(sycomoreBlockWithTx)
                )
        );

    }

    private void updateAverageTimeBetweenBlocks (double newAvgTimeBetweenBlocks){
        GlobalProofOfWorkNetwork globalProofOfWorkNetwork = (GlobalProofOfWorkNetwork) this.getNetwork();
        long totalHashPower = globalProofOfWorkNetwork.totalHashPower;
        List<Double> hashPowers = globalProofOfWorkNetwork.hashPowers;
        //System.err.println( "Node id: " + this.nodeID+ "avg time between blocks: "+blockMiningProcess.averageTimeBetweenGenerations);
        double hashPowerScale = 2097.0 / (totalHashPower * newAvgTimeBetweenBlocks);
        this.hashPower= hashPowerScale * hashPowers.get(this.nodeID);
        //this.blockMiningProcess.averageTimeBetweenGenerations= this.consensusAlgorithm.getCanonicalChainHead().getDifficulty()/((double) this.hashPower);
        this.blockMiningProcess.averageTimeBetweenGenerations = 2097 / (hashPowerScale* hashPowers.get(this.nodeID));
        //System.err.println( "Node id: " + this.nodeID+ "UPDATED avg time between blocks: "+blockMiningProcess.averageTimeBetweenGenerations);

    }

    public int countUniqueLabels(Set<SycoGhostBlock> leafBlocks) {
        Set<String> uniqueLabels = new HashSet<>();
        for (SycoGhostBlock block : leafBlocks) {
            uniqueLabels.add(block.getLabel());
        }
        return uniqueLabels.size();
    }



}
