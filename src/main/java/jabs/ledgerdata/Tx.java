package jabs.ledgerdata;

public abstract class Tx<T extends Tx<T>> extends Data { //Transaction Data
    protected Tx(int size, int hashSize) {
        super(size, hashSize);
    }
}
