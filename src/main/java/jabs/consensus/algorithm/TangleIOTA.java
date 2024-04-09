package jabs.consensus.algorithm;

import jabs.consensus.blockchain.LocalBlockDAG;
import jabs.consensus.config.TangleIOTAConsensusConfig;
import jabs.ledgerdata.tangle.TangleBlock;
import jabs.ledgerdata.tangle.TangleTx;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 */
public class TangleIOTA extends AbstractDAGBasedConsensus<TangleBlock, TangleTx> {
    //this is how IOTA consensus works...
    //
    //Ok, now we have to check how the consensus algo is implemented, and implement what to do
    //when a new block arrives in the case of sycomore
    protected final HashMap<TangleBlock, Double> blockAccWeights = new HashMap<>();

    /**
     * Creates a Tangle Consensus Algorithm
     *
     * @param localBlockDAG local block tree in the node's memory
     */
    public TangleIOTA(LocalBlockDAG<TangleBlock> localBlockDAG, TangleIOTAConsensusConfig tangleIOTAConsensusConfig) {
        super(localBlockDAG);
        this.newIncomingBlock(localBlockDAG.getGenesisBlock());
    }
    //Ok, so the constructor, called when the consensus algorithm is created takes as parameter the localblockdag,
    //which is node's local view of the dag.
    //when the constructor is called, we can simply treat as newincomingblock the genesis block

    /** If a new block that is connected to the genesis block is received
     * this function will be called. Thus, any input block is both totally
     * new and 100% connected to genesis block with all ancestors known to
     * the node. This function will update the block acc accumulative
     * weights in the consensus state of the node.
     *
     * @param block the newly received block
     */
    @Override
    public void newIncomingBlock(TangleBlock block) {
        //this function will be called when a new block is received
        blockAccWeights.put(block, block.getWeight());
        //what we do in the case of IOTA is to get all ancestors and compute the accumulated weight, but
        //what we have to do in the case of sycomore is to handle a new clock so compute the hash ecc.
        //we need to know all the leaf and compute the header of the block.
        //Where do we do this? Let's see what happens is the node
        HashSet<TangleBlock> ancestors = localBlockDAG.getAllAncestors(block);
        //here we get all the ancestors because we need them to compute the weight
        for (TangleBlock ancestor:ancestors) {
            blockAccWeights.put(ancestor, blockAccWeights.get(ancestor) + block.getWeight());
        }
    }
}
