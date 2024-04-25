package jabs.consensus.config;

import jabs.ledgerdata.Block;

/**
 */
public class SycoGhostProtocolConfig<B extends Block<B>> extends GenericConsensusAlgorithmConfig
        implements ConsensusAlgorithmConfig{

    private final double averageBlockMiningInterval;
     private final B genesisBlock;

     /**
      * @param averageBlockMiningInterval
      * @param genesisBlock
      */
    public SycoGhostProtocolConfig(B genesisBlock, double averageBlockMiningInterval) {
        this.averageBlockMiningInterval = averageBlockMiningInterval;
        this.genesisBlock = genesisBlock;
    }

        public double averageBlockMiningInterval() {
            return averageBlockMiningInterval;
        }

        public B getGenesisBlock() {
            return this.genesisBlock;
        }
}
