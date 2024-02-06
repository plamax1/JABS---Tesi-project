package jabs.ledgerdata.sycomore;

import jabs.ledgerdata.Block;
import jabs.ledgerdata.ProofOfWorkBlock;
import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.SingleParentPoWBlock;
import jabs.ledgerdata.bitcoin.BitcoinBlockWithoutTx;
import jabs.network.node.nodes.ethereum.EthereumMinerNode;
import jabs.network.node.nodes.sycomore.SycomoreMinerNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HASH_SIZE;

public class SycomoreBlock extends Block<SycomoreBlock> implements ProofOfWorkBlock {
    //This is the class of the SycomoreBlock
    private final Set<SycomoreBlock> uncles;
    private final double difficulty;
    private final double weight;

    public SycomoreBlock(int size, int height, double creationTime, SycomoreMinerNode creator, List<SycomoreBlock> parents,
                         Set<SycomoreBlock> uncles, double difficulty, double weight) {
        super(size, height, creationTime, creator, parents, ETHEREUM_BLOCK_HASH_SIZE);
        this.uncles = uncles;
        this.weight = weight;
        this.difficulty = difficulty;

        long unclesDifficultySum = 0;
        for (SycomoreBlock uncle:uncles) {
            unclesDifficultySum += uncle.getDifficulty();
        }
    }

    public Set<SycomoreBlock> getUncles() {
        return this.uncles;
    }

    public double getDifficulty() {
        return this.difficulty;
    }

    public static SycomoreBlock generateGenesisBlock(double difficulty) {
        return new SycomoreBlock(0, 0, 0, null, null, new HashSet<>(), difficulty, 0);
    }

    /**
     * @return
     */
    @Override
    public double getWeight() {
        return this.weight;
    }
}
