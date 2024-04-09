package jabs.network.node.nodes;

import jabs.consensus.algorithm.AbstractChainBasedConsensus;
import jabs.consensus.blockchain.LocalBlockTree;
import jabs.ledgerdata.*;
import jabs.network.message.*;
import jabs.network.networks.Network;
import jabs.network.p2p.AbstractP2PConnections;
import jabs.simulator.Simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
//PeerBlockchainNode, this could be any node, just a blockchain node
public abstract class PeerBlockchainNode<B extends SingleParentBlock<B>, T extends Tx<T>> extends PeerDLTNode<B, T> {
    protected final AbstractChainBasedConsensus<B, T> consensusAlgorithm;

    protected final HashMap<Hash, T> alreadySeenTxs = new HashMap<>();
    protected final HashMap<Hash, B> alreadySeenBlocks = new HashMap<>();
    protected final HashSet<Vote> alreadySeenVotes = new HashSet<>();
    protected final HashSet<Query> alreadySeenQueries = new HashSet<>();
    protected final LocalBlockTree<B> localBlockTree;

    public PeerBlockchainNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth,
                              long uploadBandwidth, AbstractP2PConnections routingTable,
                              AbstractChainBasedConsensus<B, T> consensusAlgorithm) {
        //so this is the costructor of a peer blockchain node
        //And we take: network, simulator, nodeid, download and upload bandwidth, routingtable? and the consensus Algorithm
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, routingTable, consensusAlgorithm);
        this.consensusAlgorithm = consensusAlgorithm;
        this.localBlockTree = consensusAlgorithm.getLocalBlockTree();
    }

    @Override
    public void processIncomingPacket(Packet packet) {
        //this is the function that we call to process an incoming packet...
        //When is it called? By the packetReceivingProcess
        Message message = packet.getMessage();
        if (message instanceof DataMessage) {
            Data data = ((DataMessage) message).getData();
            if (data instanceof Block) { //if the data is a block
                B block = (B) data;
                if (!localBlockTree.contains(block)){
                    localBlockTree.add(block);//add the block to the localBlocktree
                    alreadySeenBlocks.put(block.getHash(), block);
                    if (localBlockTree.getLocalBlock(block).isConnectedToGenesis) { //ok this returns the local block in the LocalBlockDAG
                        this.processNewBlock(block); //we process this new block, and even all successors
                        SortedSet<B> newBlocks = new TreeSet<>(localBlockTree.getAllSuccessors(block));
                        for (B newBlock:newBlocks){
                            this.processNewBlock(newBlock);
                        }
                    } else {//if it is not connected to genesis we need the hash of them parent...
                        this.networkInterface.addToUpLinkQueue(
                                new Packet(this, packet.getFrom(),
                                        new RequestDataMessage(block.getParent().getHash())
                                )
                        );
                    }
                }
            } else if (data instanceof Tx) {//if the data is a transaction...
                T tx = (T) data;
                if (!alreadySeenTxs.containsValue(tx)){
                    alreadySeenTxs.put(tx.getHash(), tx);
                    this.processNewTx(tx, packet.getFrom());
                }
            }
        } else if (message instanceof InvMessage) {
            Hash hash = ((InvMessage) message).getHash();
            if (hash.getData() instanceof Block){
                if (!alreadySeenTxs.containsKey(hash)) {
                    alreadySeenTxs.put(hash, null);
                    this.networkInterface.addToUpLinkQueue(
                            new Packet(this, packet.getFrom(),
                                    new RequestDataMessage(hash)
                            )
                    );
                }
            } else if (hash.getData() instanceof Tx) {
                if (!alreadySeenBlocks.containsKey(hash)) {
                    alreadySeenBlocks.put(hash, null);
                    this.networkInterface.addToUpLinkQueue(
                            new Packet(this, packet.getFrom(),
                                    new RequestDataMessage(hash)
                            )
                    );
                }
            }
        } else if (message instanceof RequestDataMessage) {
            Hash hash = ((RequestDataMessage) message).getHash();
            if (hash.getData() instanceof Block) {
                if (alreadySeenBlocks.containsKey(hash)) {
                    B block = alreadySeenBlocks.get(hash);
                    if (block != null) {
                        this.networkInterface.addToUpLinkQueue(
                                new Packet(this, packet.getFrom(),
                                        new DataMessage(block)
                                )
                        );
                    }
                }
            } else if (hash.getData() instanceof Tx) {
                if (alreadySeenTxs.containsKey(hash)) {
                    T tx = alreadySeenTxs.get(hash);
                    if (tx != null) {
                        this.networkInterface.addToUpLinkQueue(
                                new Packet(this, packet.getFrom(),
                                        new DataMessage(tx)
                                )
                        );
                    }
                }
            }
        } else if (message instanceof VoteMessage) {
            Vote vote = ((VoteMessage) message).getVote();
            if (!alreadySeenVotes.contains(vote)) {
                alreadySeenVotes.add(vote);
                this.processNewVote(vote);
            }
        }else if (message instanceof QueryMessage) {
            Query query = ((QueryMessage) message).getQuery();
            if (!alreadySeenQueries.contains(query)) {
                alreadySeenQueries.add(query);
                this.processNewQuery(query);
            }
        }
    }

    protected abstract void processNewBlock(B block);
    //process new block etc is implemended in the consensus algorithm.
    protected abstract void processNewVote(Vote vote);
    protected abstract void processNewQuery(Query query);

    public AbstractChainBasedConsensus<B, T> getConsensusAlgorithm() {
        return this.consensusAlgorithm;
    }

    public int numberOfAlreadySeenBlocks() {
        return alreadySeenBlocks.size();
    }
}
