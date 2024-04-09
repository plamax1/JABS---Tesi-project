package jabs.ledgerdata;

public abstract class BasicData {
    //This is the basic ledger data... Only data
    protected int size;

    protected BasicData(int size) {
        this.size = size;
    }

    public int getSize() { return size; };
}
