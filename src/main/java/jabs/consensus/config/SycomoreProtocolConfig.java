package jabs.consensus.config;

import jabs.ledgerdata.SingleParentBlock;

/**
 */
public class SycomoreProtocolConfig<B extends SingleParentBlock<B>> extends ChainBasedConsensusConfig<B> {
    /**
     * @param averageBlockMiningInterval
     */
    public SycomoreProtocolConfig(B genesisBlock, double averageBlockMiningInterval) {
        super(genesisBlock, averageBlockMiningInterval);
    }
}
