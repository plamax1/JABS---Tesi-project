package jabs.network.node.nodes.sycoghost;

import java.util.ArrayList;

public class SGBlockHeader {
    private final ArrayList<BlockHeaderEntry> headers;

    public SGBlockHeader() {
        this.headers = new ArrayList<>();
    }

    public void add(BlockHeaderEntry header) {
        //Add element at the end of the list
        headers.add(header);
    }

    public BlockHeaderEntry get(int index) {
        return headers.get(index);
    }

    public ArrayList getHeadersList() {
        return headers;
    }
    public BlockHeaderEntry removeFirstElement() {

        if (headers.isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return headers.remove(0); // Remove the element at index 0 and return it
    }


    public int size() {
        return headers.size();
    }

    // Additional methods like remove, contains, etc. can be added as needed.
}
