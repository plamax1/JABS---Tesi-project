package jabs.network.stats;

public interface NodeGlobalRegionDistribution<R extends Enum<R>> {//what is this?
    //NodeGlobalRegionDistribution...
    R sampleRegion();

    int totalNumberOfNodes();
}
