package jabs.consensus.config;

import jabs.ledgerdata.Block;

public abstract class GenericConsensusAlgorithmConfig implements ConsensusAlgorithmConfig{

    public abstract double averageBlockMiningInterval();

    public abstract Block getGenesisBlock();
}
