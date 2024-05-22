package jabs.network.node.nodes.ethereum;

import jabs.consensus.algorithm.AbstractChainBasedConsensus;
import jabs.consensus.algorithm.ConsensusAlgorithm;
import jabs.consensus.algorithm.GhostProtocol;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumBlockWithTx;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.DataMessage;
import jabs.network.message.Packet;
import jabs.network.networks.GlobalProofOfWorkNetwork;
import jabs.network.networks.Network;
import jabs.network.node.nodes.MinerNode;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.PeerBlockchainNode;
import jabs.simulator.Simulator;
import jabs.simulator.event.BlockMiningProcess;
import jabs.simulator.event.NewUncleEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_MIN_DIFFICULTY;

public class EthereumMinerNode extends EthereumNode implements MinerNode {
    //This is the ethereum miner node, and in ethereum only miner nodes can generate new blocks
    protected Set<EthereumTx> memPool = new HashSet<>();
    protected Set<EthereumBlock> alreadyUncledBlocks = new HashSet<>();
    protected double hashPower;
    protected Simulator.ScheduledEvent miningProcess;
    static final long MAXIMUM_BLOCK_GAS = 12500000;
    private int currentNumUncles;
    private BlockMiningProcess blockMiningProcess;

    public EthereumMinerNode(Simulator simulator, Network network, int nodeID,
                             long downloadBandwidth, long uploadBandwidth, double hashPower, EthereumBlock genesisBlock,
                             GhostProtocolConfig ghostProtocolConfig) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, genesisBlock, ghostProtocolConfig);
        this.hashPower = hashPower;
        currentNumUncles=0;
    }

    public EthereumMinerNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth,
                             long uploadBandwidth, double hashPower,
                             AbstractChainBasedConsensus<EthereumBlock, EthereumTx> consensusAlgorithm) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, (GhostProtocol) consensusAlgorithm);
        this.hashPower = hashPower;
        currentNumUncles=0;

    }
    boolean done_flag = false;

    public void generateNewBlock() {//qui noi generiamo un nuovo blocco
        EthereumBlock canonicalChainHead = this.consensusAlgorithm.getCanonicalChainHead();
        Simulator simulator = this.getSimulator();
        //if(((int) simulator.getSimulationTime()%10)==0){
          //  System.err.println("Time: "+simulator.getSimulationTime() + " current avg time between blocks: "+blockMiningProcess.averageTimeBetweenGenerations);
        //}


        Set<EthereumBlock> tipBlocks = this.localBlockTree.getChildlessBlocks();
        tipBlocks.remove(canonicalChainHead);
        tipBlocks.removeAll(alreadyUncledBlocks);

        Set<EthereumTx> blockTxs = new HashSet<>();
        long totalGas = 0;
        for (EthereumTx ethereumTx:memPool) {
            if ((totalGas + ethereumTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                break;
            }
            blockTxs.add(ethereumTx);
            totalGas += ethereumTx.getGas();
        }

        double weight = this.network.getRandom().sampleExponentialDistribution(1);
        EthereumBlockWithTx ethereumBlockWithTx = new EthereumBlockWithTx(
                canonicalChainHead.getHeight()+1, simulator.getSimulationTime(), this,
                this.getConsensusAlgorithm().getCanonicalChainHead(), tipBlocks, blockTxs, ETHEREUM_MIN_DIFFICULTY,
                weight); // TODO: Difficulty?

        //System.err.println(ethereumBlockWithTx.getDifficulty());
        this.processIncomingPacket(//and the block is propagated
                new Packet(
                        this, this, new DataMessage(ethereumBlockWithTx)
                )
        );
    }

    /**
     *
     */
    @Override
    public void startMining() { //il mining del singolo node
        blockMiningProcess  = new BlockMiningProcess(this.simulator, this.network.getRandom(),
                this.consensusAlgorithm.getCanonicalChainHead().getDifficulty()/((double) this.hashPower), this);
        this.miningProcess = this.simulator.putEvent(blockMiningProcess, blockMiningProcess.timeToNextGeneration());
        System.err.println("Starting mining" + this.nodeID + " difficulty" + this.consensusAlgorithm.getCanonicalChainHead().getDifficulty()+ " hashpower " +  this.hashPower + "average time between blocks: "+blockMiningProcess.averageTimeBetweenGenerations);

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

    @Override
    protected void processNewTx(EthereumTx ethereumTx, Node from) {
        // add to memPool
        memPool.add((EthereumTx) ethereumTx);

        this.broadcastTransaction((EthereumTx) ethereumTx, from);
    }

    @Override
    protected void processNewBlock(EthereumBlock ethereumBlock) {
        this.consensusAlgorithm.newIncomingBlock(ethereumBlock);
        //Quindi... newincoming block, se ne occupa l'algoritmo di consenso, processnewblock è come il
        //nodo processa un blocck in arrivo

        alreadyUncledBlocks.addAll(ethereumBlock.getUncles());


        // remove from memPool
        if (ethereumBlock instanceof EthereumBlockWithTx) {//when a new block arrives remove the
            //transactions from the mempool
            for (EthereumTx ethereumTx: ((EthereumBlockWithTx) ethereumBlock).getTxs()) {
                memPool.remove(ethereumTx); // TODO: This should be changed. Ethereum reverts Txs from non canonical chain
            }
        }

        this.broadcastNewBlockAndBlockHashes(ethereumBlock);
        //each 10 blocks of height we call the Event to count the uncles
            int canonicalHeadLen = this.getConsensusAlgorithm().getCanonicalChainHead().getHeight();
            int totalBlocks = this.getConsensusAlgorithm().getLocalBlockTree().size();
            int newNumUncles = totalBlocks - canonicalHeadLen - 1; // The genesis block shall not be counted
            if(newNumUncles!=currentNumUncles){
                //We have a new uncle, so we can call the event;
                currentNumUncles=newNumUncles;
                simulator.putEvent(new NewUncleEvent(simulator.getSimulationTime(),this,currentNumUncles),0);
            }

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
        System.err.println( "Node id: " + this.nodeID+ "UPDATED avg time between blocks: "+blockMiningProcess.averageTimeBetweenGenerations);

    }

}
