package jabs.consensus.config;

import jabs.ledgerdata.Block;
import jabs.ledgerdata.SingleParentBlock;

/**
 */
public class SycomoreProtocolConfig<B extends Block<B>> extends GenericConsensusAlgorithmConfig
        implements ConsensusAlgorithmConfig{

    private final double averageBlockMiningInterval;
     private final B genesisBlock;

     /**
      * @param averageBlockMiningInterval
      * @param genesisBlock
      */
    public SycomoreProtocolConfig(B genesisBlock, double averageBlockMiningInterval) {
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
