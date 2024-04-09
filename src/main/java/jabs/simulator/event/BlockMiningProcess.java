package jabs.simulator.event;

import jabs.network.node.nodes.MinerNode;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

public class BlockMiningProcess extends AbstractPoissonProcess {
    //what is the BlockMiningProcess?
    protected final MinerNode miner; //we have the minerNode, which is the miner

    public BlockMiningProcess(Simulator simulator, RandomnessEngine randomnessEngine, double averageTimeBetweenBlocks,
                              MinerNode miner) {
        //block mining process
        super(simulator, randomnessEngine, averageTimeBetweenBlocks);
        this.miner = miner;
    }

    @Override
    public void generate() {
        miner.generateNewBlock();
    }//and the method generate to generate a new block

    public MinerNode getMiner() {
        return this.miner;
    }
}
