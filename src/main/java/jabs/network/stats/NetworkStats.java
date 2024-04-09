package jabs.network.stats;

public interface NetworkStats<R extends Enum<R>> { //R represents the region?
    //so this is the network stats that refers to a single region

    double getLatency(R fromPosition, R toPosition);

    long sampleDownloadBandwidth(R position);

    long sampleUploadBandwidth(R position);
}
