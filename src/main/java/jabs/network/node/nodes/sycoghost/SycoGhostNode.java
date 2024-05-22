package jabs.network.node.nodes.sycoghost;

import jabs.consensus.algorithm.AbstractDAGBasedConsensus;
import jabs.consensus.algorithm.SycoGhostProtocol;
import jabs.consensus.algorithm.SycomoreConsensusAlgorithm;
import jabs.consensus.algorithm.VotingBasedConsensus;
import jabs.consensus.blockchain.LocalBlockDAG;
import jabs.consensus.config.SycoGhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.sycoghost.SycoGhostBlock;
import jabs.ledgerdata.sycoghost.SycoGhostTx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreTx;
import jabs.network.message.DataMessage;
import jabs.network.message.InvMessage;
import jabs.network.message.Packet;
import jabs.network.message.VoteMessage;
import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.PeerDLTNode;
import jabs.network.p2p.EthereumGethP2P;
import jabs.simulator.Simulator;

import static org.apache.commons.math3.util.FastMath.sqrt;

public class SycoGhostNode extends PeerDLTNode<SycoGhostBlock, SycoGhostTx> {
    //qui abbiamo 2 costruttori
    public SycoGhostNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
                         SycoGhostBlock genesisBlock, SycoGhostProtocolConfig sycoGhostProtocolConfig) { //the constructor,
        //takes as parameters, simulator, network, nodeid, download and upload bandwidth, and protocol config
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new EthereumGethP2P(), //this is abstractp2pconnection -> used with ethereumgetp2p
                //new GhostProtocol<>(new LocalBlockTree<>(genesisBlock), ghostProtocolConfig));
                new SycoGhostProtocol(new LocalBlockDAG<>(genesisBlock), sycoGhostProtocolConfig));
    }

    public SycoGhostNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
                         AbstractDAGBasedConsensus<SycoGhostBlock, SycoGhostTx> consensusAlgorithm) {
        //this is the second costructor, here we don't have the genesisblock and the ghostprocoilconfig, but the
         //abstractchainbasedconsensus
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new EthereumGethP2P(),
                consensusAlgorithm);
    }

    @Override
    protected void processNewTx(SycoGhostTx sycomoreTx, Node from) {
        System.err.println("4321 Transaction received");

        this.broadcastTransaction(sycomoreTx, from);
    }

    @Override
    protected void processNewBlock(SycoGhostBlock sycomoreBlock) {
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
        broadcastTransaction(TransactionFactory.sampleSycoGhostTransaction(network.getRandom()));
    }

    protected void broadcastNewBlockAndBlockHashes(SycoGhostBlock sycomoreBlock){
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

    protected void broadcastTransaction(SycoGhostTx tx, Node excludeNeighbor) {
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

    protected void broadcastTransaction(SycoGhostTx tx) {
        broadcastTransaction(tx, null);
    }




}
