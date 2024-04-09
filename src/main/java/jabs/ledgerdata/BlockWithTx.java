package jabs.ledgerdata;

import java.util.Set;

public interface BlockWithTx<T extends Tx<T>> {
    //Extends block adding transactions
    Set<T> getTxs();
}
