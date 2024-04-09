package jabs.network.message;

import jabs.ledgerdata.Data;

public class DataMessage extends Message { //Data message, where Data can be a block or a Tx
    private final Data data;

    public DataMessage(Data data) {
        super(data.getSize());
        this.data = data;
    }

    public Data getData() {
        return data;
    }
}
