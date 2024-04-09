package jabs.network.stats;

public interface MinerGlobalRegionDistribution<R extends Enum<R>> {
    //MinerGlobalRegionDistribution...
    R sampleMinerRegion();

    double sampleMinerHashPower();

    double shareOfMinersToAllNodes();

    int totalNumberOfMiners();
}
