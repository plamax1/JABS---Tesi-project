package jabs.ledgerdata;

public abstract class Data extends BasicData {
    final Hash hash;
    //we have hash more than basicdata, and we have DataType, which could be of type block or tx
    public enum DataType {
        //we have data of two types: hash and transaction
        BLOCK,
        TX,
    }

    protected Data(int size, int hashSize) {
        super(size);
        this.hash = new Hash(hashSize, this);
    }

    public Hash getHash() {
        return hash;
    }
}
