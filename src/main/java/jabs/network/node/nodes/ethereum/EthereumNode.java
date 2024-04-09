package jabs.network.node.nodes.ethereum;

import jabs.consensus.algorithm.AbstractChainBasedConsensus;
import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.algorithm.GhostProtocol;
import jabs.consensus.algorithm.VotingBasedConsensus;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.DataMessage;
import jabs.network.message.InvMessage;
import jabs.network.message.Packet;
import jabs.network.message.VoteMessage;
import jabs.network.networks.Network;
import jabs.ledgerdata.TransactionFactory;
import jabs.network.node.nodes.PeerBlockchainNode;
import jabs.network.node.nodes.Node;
import jabs.network.p2p.EthereumGethP2P;
import jabs.simulator.Simulator;

import static org.apache.commons.math3.util.FastMath.sqrt;

public class EthereumNode extends PeerBlockchainNode<EthereumBlock, EthereumTx> {
    //Class ethereumnode
    //the constructors to instantiate a new ethereum node
    public EthereumNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
                        EthereumBlock genesisBlock, GhostProtocolConfig ghostProtocolConfig) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new EthereumGethP2P(),
                new GhostProtocol<>(new LocalBlockTree<>(genesisBlock), ghostProtocolConfig));
        this.consensusAlgorithm.setNode(this);

    }

    public EthereumNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
                        AbstractChainBasedConsensus<EthereumBlock, EthereumTx> consensusAlgorithm) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new EthereumGethP2P(),
                consensusAlgorithm);
        this.consensusAlgorithm.setNode(this);
    }

    @Override
    protected void processNewTx(EthereumTx ethereumTx, Node from) {
        this.broadcastTransaction(ethereumTx, from);
    }

    @Override
    protected void processNewBlock(EthereumBlock ethereumBlock) {
        //how the processing of a new block works, and what is the difference between
        //this and the other function
        this.consensusAlgorithm.newIncomingBlock(ethereumBlock);
        this.broadcastNewBlockAndBlockHashes(ethereumBlock);
    }

    @Override
    protected void processNewVote(Vote vote) { //This is the function to process the new vote
        if (this.consensusAlgorithm instanceof VotingBasedConsensus) {
            ((VotingBasedConsensus) this.consensusAlgorithm).newIncomingVote(vote);
            for (Node neighbor : this.p2pConnections.getNeighbors()) {
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                new VoteMessage(vote)
                        )
                );
            }
        }
    }

    @Override
    protected void processNewQuery(jabs.ledgerdata.Query query) {

    }

    @Override
    public void generateNewTransaction() {
        //and the function to Generatenewtransaction, and it is used in the Txgenerationprocess
        broadcastTransaction(TransactionFactory.sampleEthereumTransaction(network.getRandom()));
    }

    protected void broadcastNewBlockAndBlockHashes(EthereumBlock ethereumBlock){
        //So then we broadcast this new block to all the neighbours
        for (int i = 0; i < this.p2pConnections.getNeighbors().size(); i++) {
            Node neighbor = this.p2pConnections.getNeighbors().get(i);
            if (i < sqrt(this.p2pConnections.getNeighbors().size())){
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                new DataMessage(ethereumBlock)
                        )
                );
            } else {
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                new InvMessage(ethereumBlock.getHash().getSize(), ethereumBlock.getHash())
                        )
                );
            }
        }
    }

    protected void broadcastTransaction(EthereumTx tx, Node excludeNeighbor) {
        //Simply broadcast the transaction so neighbors nodes.
        for (Node neighbor:this.p2pConnections.getNeighbors()) {
            if (neighbor != excludeNeighbor){
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                new DataMessage(tx)
                        )
                );
            }
        }
    }

    protected void broadcastTransaction(EthereumTx tx) {
        broadcastTransaction(tx, null);
    }

}
