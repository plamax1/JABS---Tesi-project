package jabs.ledgerdata.sycomore;

import jabs.ledgerdata.Tx;

public class SycomoreTx extends Tx<SycomoreTx> {
    final long gas;

    public SycomoreTx(int size, long gas) {
        super(size, 0); // Ethereum does not use transaction hashes in network communication
        //but what about sycomore
        this.gas = gas;
    }

    public long getGas() {
        return gas;
    }
    //this is the gas of each transaction...
}
