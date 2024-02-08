package jabs.network.node.nodes.sycomore;

import jabs.consensus.algorithm.AbstractChainBasedConsensus;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumBlockWithTx;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
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

import java.util.HashSet;
import java.util.Set;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_MIN_DIFFICULTY;

public class SycomoreMinerNode extends SycomoreNode implements MinerNode {
    protected Set<SycomoreTx> memPool = new HashSet<>();
    //mempool, sort of waiting room for transaction still not included in a block
    protected Set<SycomoreBlock> alreadyUncledBlocks = new HashSet<>();
    protected final double hashPower;
    protected Simulator.ScheduledEvent miningProcess;
    static final long MAXIMUM_BLOCK_GAS = 12500000;

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
                             AbstractChainBasedConsensus<SycomoreBlock, SycomoreTx> consensusAlgorithm) {
        //nel secondo prendiamo abstractchainbasedconsensus
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, consensusAlgorithm);
        this.hashPower = hashPower;
    }

    public void generateNewBlock() {
        SycomoreBlock canonicalChainHead = this.consensusAlgorithm.getCanonicalChainHead();

        Set<SycomoreBlock> tipBlocks = this.localBlockTree.getChildlessBlocks();
        //tip block should be unconfirmed block or blocks without child --- so leaf
        //here we remove the head, and the alreadyuncledblocks
        tipBlocks.remove(canonicalChainHead);
        tipBlocks.removeAll(alreadyUncledBlocks); //da rivedere

        Set<SycomoreTx> blockTxs = new HashSet<>();
        long totalGas = 0;
        for (SycomoreTx sycomoreTx:memPool) { //for each transaction in the mempool
            if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                break;
            }
            blockTxs.add(sycomoreTx);
            totalGas += sycomoreTx.getGas();
        } //here we add the transactions to the block

        double weight = this.network.getRandom().sampleExponentialDistribution(1);
        SycomoreBlockWithTx ethereumBlockWithTx = new SycomoreBlockWithTx(
                canonicalChainHead.getHeight()+1, simulator.getSimulationTime(), this,
                this.getConsensusAlgorithm().getCanonicalChainHead(), tipBlocks, blockTxs, ETHEREUM_MIN_DIFFICULTY,
                weight); // TODO: Difficulty?

        this.processIncomingPacket(
                new Packet(
                        this, this, new DataMessage(ethereumBlockWithTx)
                )
        );
    }

    /**
     *
     */
    @Override
    public void startMining() {
        BlockMiningProcess blockMiningProcess = new BlockMiningProcess(this.simulator, this.network.getRandom(),
                this.consensusAlgorithm.getCanonicalChainHead().getDifficulty()/((double) this.hashPower), this);
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


        alreadyUncledBlocks.addAll(sycomoreBlock.getUncles());

        // remove from memPool

        if (sycomoreBlock instanceof SycomoreBlockWithTx) {
            for (SycomoreTx sycomoreTx: ((SycomoreBlockWithTx) sycomoreBlock).getTxs()) {
                memPool.remove(sycomoreTx); // TODO: This should be changed. Ethereum reverts Txs from non canonical chain
            }
        }

        this.broadcastNewBlockAndBlockHashes(sycomoreBlock);
    }
}
