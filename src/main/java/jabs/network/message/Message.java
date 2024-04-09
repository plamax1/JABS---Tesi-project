package jabs.network.message;

public abstract class Message { //message abstract class, containing only size
    private final int size;

    public int getSize(){ return this.size; }

    public Message(int size) {
        this.size = size;
    }
}
