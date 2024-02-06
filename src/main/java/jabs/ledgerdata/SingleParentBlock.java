package jabs.ledgerdata;

import jabs.network.node.nodes.Node;

import java.util.ArrayList;

public class SingleParentBlock<B extends SingleParentBlock<B>> extends Block<B> {
    //this is simply the singleParentblock, it takes as input one parent B, and creates the general block
    //adding it to the list, nothing else.
    protected SingleParentBlock(int size, int height, double creationTime, Node creator, B parent, int hashSize) {
        super(size, height, creationTime, creator, new ArrayList<>(), hashSize);

        this.getParents().add(parent);
    }
    //here we redefine the function getparent, returning only the first parent of the list
    public <B extends SingleParentBlock<B>> B getParent() {
        return (B) this.getParents().get(0);
    }
}
