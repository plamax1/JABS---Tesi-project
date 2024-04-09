package jabs.network.node.nodes;

import jabs.consensus.algorithm.AbstractDAGBasedConsensus;
import jabs.consensus.blockchain.LocalBlockDAG;
import jabs.ledgerdata.*;
import jabs.network.message.*;
import jabs.network.networks.Network;
import jabs.network.p2p.AbstractP2PConnections;
import jabs.simulator.Simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class PeerDLTNode<B extends Block<B>, T extends Tx<T>> extends Node {
    //This is the peerdltNode, the most generic node
    //It has as parameters: consensus, so the consensus algorithm
    protected final AbstractDAGBasedConsensus<B, T> consensusAlgorithm;
    //The hashmap with what? alreadyseentxs, alreadyseenblocks, the votes, the queries
    protected final HashMap<Hash, T> alreadySeenTxs = new HashMap<>();
    protected final HashMap<Hash, B> alreadySeenBlocks = new HashMap<>();
    protected final HashSet<Vote> alreadySeenVotes = new HashSet<>();
    protected final HashSet<Query> alreadySeenQueries = new HashSet<>();
    //And we have the local node view of the dag, so the LocalBlockDAG
    protected final LocalBlockDAG<B> localBlockTree;

    public PeerDLTNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth, AbstractP2PConnections routingTable,
                       AbstractDAGBasedConsensus<B, T> consensusAlgorithm) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, routingTable);
        this.consensusAlgorithm = consensusAlgorithm;
        //the localblockDAG is in the consensusAlgorithm
        this.localBlockTree = consensusAlgorithm.getLocalBlockDAG();
        this.consensusAlgorithm.setNode(this);
    }

    @Override //with this function we define the processing of an incoming packet
    public void processIncomingPacket(Packet packet) {
        Message message = packet.getMessage();
        if (message instanceof DataMessage) { //if we have a data message
            Data data = ((DataMessage) message).getData();
            if (data instanceof Block) { //if invece data era è un block
                B block = (B) data;
                if (!localBlockTree.contains(block)){ //if localblocktree non contiene il block
                    localBlockTree.add(block);//lo aggiungiamo
                    alreadySeenBlocks.put(block.getHash(), block);//e lo mettiamo tra i block già visti
                    if (localBlockTree.getLocalBlock(block).isConnectedToGenesis) {//check if what is connected to genesis?
                        //if the block is connected to genesis, and has never been seen is a block
                        //that we can append to the chain
                        this.processNewBlock(block);//if it is not connected to genesis process a new block
                        SortedSet<B> newBlocks = new TreeSet<>(localBlockTree.getAllSuccessors(block));
                        //We even get all successors and process them
                        for (B newBlock:newBlocks){ //for all the blocks which are the successors of the
                                                    //just evaluated block we process the new block
                            this.processNewBlock(newBlock);
                        }
                    } else {
                        //if the block is not connected to genesis, it means that it belong to a different
                        //chain, so at some moment we will get a fork
                        //so what the algo does is get the parents until we reach a block that is connected to
                        //genesis, which is possibly a fork
                        for (B parent: block.getParents()){ //we get all the parents.
                            this.networkInterface.addToUpLinkQueue(
                                    new Packet(this, packet.getFrom(),
                                            new RequestDataMessage(parent.getHash())
                                    )
                            );
                        }
                    }
                }
            } else if (data instanceof Tx) { //if the data is a transaction
                T tx = (T) data; //cast the transaction
                if (!alreadySeenTxs.containsValue(tx)){ //if the transaction is not in the already seen
                    alreadySeenTxs.put(tx.getHash(), tx);
                    this.processNewTx(tx, packet.getFrom()); //call processNewTx
                }
            }
        } else if (message instanceof InvMessage) { //if the messagge is an InvMessage
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
//these functions are implemented but just abstract, so where is the logic defined?
    protected abstract void processNewTx(T tx, Node from);
    protected abstract void processNewBlock(B block);
    protected abstract void processNewVote(Vote vote);
    protected abstract void processNewQuery(Query query);
    public AbstractDAGBasedConsensus<B, T> getConsensusAlgorithm() {
        return this.consensusAlgorithm;
    }

    public int numberOfAlreadySeenBlocks() {
        return alreadySeenBlocks.size();
    }
}
