package jabs.network.node.nodes.sycomore;

import jabs.consensus.algorithm.*;
import jabs.consensus.blockchain.LocalBlockDAG;
import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreTx;
import jabs.network.message.DataMessage;
import jabs.network.message.InvMessage;
import jabs.network.message.Packet;
import jabs.network.message.VoteMessage;
import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.PeerBlockchainNode;
import jabs.network.node.nodes.PeerDLTNode;
import jabs.network.p2p.EthereumGethP2P;
import jabs.simulator.Simulator;

import static org.apache.commons.math3.util.FastMath.sqrt;

public class SycomoreNode extends PeerDLTNode<SycomoreBlock, SycomoreTx> {
    //qui abbiamo 2 costruttori
    public SycomoreNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
                        SycomoreBlock genesisBlock, SycomoreProtocolConfig sycomoreProtocolConfig) { //the constructor,
        //takes as parameters, simulator, network, nodeid, download and upload bandwidth, and protocol config
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new EthereumGethP2P(), //this is abstractp2pconnection -> used with ethereumgetp2p
                //new GhostProtocol<>(new LocalBlockTree<>(genesisBlock), ghostProtocolConfig));
                new SycomoreConsensusAlgorithm(new LocalBlockDAG<>(genesisBlock), sycomoreProtocolConfig));
    }

    public SycomoreNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
                        AbstractDAGBasedConsensus<SycomoreBlock, SycomoreTx> consensusAlgorithm) {
        //this is the second costructor, here we don't have the genesisblock and the ghostprocoilconfig, but the
         //abstractchainbasedconsensus
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new EthereumGethP2P(),
                consensusAlgorithm);
    }

    @Override
    protected void processNewTx(SycomoreTx sycomoreTx, Node from) {
        System.err.println("4321 Transaction received");

        this.broadcastTransaction(sycomoreTx, from);
    }

    @Override
    protected void processNewBlock(SycomoreBlock sycomoreBlock) {
        this.consensusAlgorithm.newIncomingBlock(sycomoreBlock);
        this.broadcastNewBlockAndBlockHashes(sycomoreBlock);
    }

    @Override
    protected void processNewVote(Vote vote) {
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
        System.err.println("1234 Generate new transaction syco called");
        //This is to generate new transactions.
        broadcastTransaction(TransactionFactory.sampleSycomoreTransaction(network.getRandom()));
    }

    protected void broadcastNewBlockAndBlockHashes(SycomoreBlock sycomoreBlock){
        for (int i = 0; i < this.p2pConnections.getNeighbors().size(); i++) {
            Node neighbor = this.p2pConnections.getNeighbors().get(i);
            if (i < sqrt(this.p2pConnections.getNeighbors().size())){
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                new DataMessage(sycomoreBlock)
                        )
                );
            } else {
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                new InvMessage(sycomoreBlock.getHash().getSize(), sycomoreBlock.getHash())
                        )
                );
            }
        }
    }

    protected void broadcastTransaction(SycomoreTx tx, Node excludeNeighbor) {
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

    protected void broadcastTransaction(SycomoreTx tx) {
        broadcastTransaction(tx, null);
    }

}
