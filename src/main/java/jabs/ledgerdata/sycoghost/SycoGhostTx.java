package jabs.ledgerdata.sycoghost;

import jabs.ledgerdata.Tx;

public class SycoGhostTx extends Tx<SycoGhostTx> {
    final long gas;

    public SycoGhostTx(int size, long gas) {
        super(size, 32); // Ethereum does not use transaction hashes in network communication
        //but what about sycomore
        this.gas = gas;
    }

    public long getGas() {
        return gas;
    }
    //this is the gas of each transaction...
}
