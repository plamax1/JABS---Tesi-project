package jabs.network.node.nodes;

public interface MinerNode {
    //This is just the interface MinerNode
    void generateNewBlock();

    void startMining();

    void stopMining();

    double getHashPower();
}
