package jabs.ledgerdata;

import jabs.network.node.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class Block<B extends Block<B>> extends Data implements Comparable<Block<B>> {
    //the block height is the number of the block in the blockchain
    private final int height;
    private final double creationTime;
    //we have a list of parents, because in a dag the parents can be more than one
    private final List<B> parents;
    //the creator, is a the node who has created the block
    private final Node creator;
//this is the constructor
    protected Block(int size, int height, double creationTime, Node creator, List<B> parents, int hashSize) {
        super(size, hashSize);
        this.height = height;
        this.creationTime = creationTime;
        this.creator = creator;
        this.parents = parents;
    }

    public int getHeight() {
        return this.height;
    }

    public double getCreationTime() {
        return this.creationTime;
    }

    public Node getCreator() {
        return this.creator;
    }

//getparents returns the list of the parents of the block, which in a dag can be more than one
    public List<B> getParents() {
        return this.parents;
    }
//the compare to method defines the order between blocks, based on height
    public int compareTo(Block<B> b) {
        return Integer.compare(this.height, b.getHeight());
    }
}
