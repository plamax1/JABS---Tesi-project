package jabs;

import jabs.ledgerdata.bitcoin.BitcoinBlockWithoutTx;
import jabs.log.*;
import jabs.scenario.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 *
 */
public class Main {
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        AbstractScenario scenario;
/*
        // Simulate one day in the life of Bitcoin network
        // Nakamoto protocol with block every 600 seconds
        // Around 8000 nodes with 30 miners
        scenario = new BitcoinGlobalNetworkScenario("One day in the life of Bitcoin", 1,
                86400, 600, 6);
        scenario.AddNewLogger(new BlockConfirmationLogger(Paths.get("output/bitcoin-confirmations-log.csv")));
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/bitcoin-50-propagation-delay-log.csv"),0.5));
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/bitcoin-90-propagation-delay-log.csv"),0.9));
        scenario.AddNewLogger(new BlockchainReorgLogger<BitcoinBlockWithoutTx>(
                Paths.get("output/bitcoin-reorgs-log.csv")));
        scenario.run();

        // Simulate 1 hour in the life of Ethereum network
        // Ghost protocol with blocks every 14 seconds on average
        // Around 6000 nodes with 37 miners
        scenario = new NormalEthereumNetworkScenario("One hour in the life of Ethereum", 1,
                3600, 13.3);
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/ethereum-50-propagation-delay-log.csv"), 0.5));
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/ethereum-90-propagation-delay-log.csv"), 0.9));
        scenario.AddNewLogger(new FinalUncleBlocksLogger(
                Paths.get("output/ethereum-uncle-rate.csv")));
        scenario.run();

        // Simulate PBFT Lan network of 40 nodes for 1 hour
        scenario = new PBFTLANScenario("One hour of a PBFT lan Network", 1,
                40, 3600);
        scenario.AddNewLogger(new PBFTCSVLogger(Paths.get("output/pbft-simulation-log.csv")));
        scenario.run();
*/
       /* // Simulate Snow LAN network of 40 nodes for 1 hour
        scenario = new SnowLANScenario("One hour of a Snow lan Network", 1, 40,
                3600);
        scenario.AddNewLogger(new SnowCSVLogger(Paths.get("output/snow-simulation-log.csv")));
        scenario.run();
*/
        // Simulate Snow LAN network of 40 nodes for 1 hour
        scenario = new SycomoreScenario("One hour in the life of Sycomore", 1,
              30000, 600);
        //scenario = new SycoGhostScenario(("One hour in the life of SycoGhost"), 1,
          //      1800, 20);
        //scenario = new NormalEthereumNetworkScenario("One hour in the life of Ethereum", 1,
          //      3600, 6);
        //scenario.AddNewLogger(new SnowCSVLogger(Paths.get("output/snow-simulation-log.csv")));
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/ethereum-90-propagation-delay-log.csv"), 0.9));
        //scenario.AddNewLogger(new FinalUncleBlocksLogger(
          //      Paths.get("output/ethereum-uncle-rate.csv")));
        scenario.AddNewLogger(new BlockConfirmationLogger(Paths.get("output/eth-block-confirmation.csv")));
        //scenario.AddNewLogger(new LiveUncleBlocksLogger(Paths.get("output/eth-liveUncleLog.csv")));
        scenario.AddNewLogger(new K_ConfirmationBlockLogger(Paths.get("output/K_Level_Block_Confirmation.csv"),"6"));
        //scenario.AddNewLogger(new LiveForkLogger(Paths.get("output/Live_Fork_Log.txt")));
        scenario.run();
        // IOTA
    }
}