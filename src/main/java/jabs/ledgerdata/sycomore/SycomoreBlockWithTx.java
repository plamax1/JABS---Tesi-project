package jabs.ledgerdata.sycomore;

import jabs.ledgerdata.BlockWithTx;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.node.nodes.ethereum.EthereumMinerNode;
import jabs.network.node.nodes.sycomore.SycomoreMinerNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HASH_SIZE;
import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HEADER_SIZE;

public class SycomoreBlockWithTx extends SycomoreBlock implements BlockWithTx<SycomoreTx> {
    //we have a set of transactions and the total gas
    private final Set<SycomoreTx> Txs;
    private final long totalGas;

    public SycomoreBlockWithTx(int height, double creationTime, SycomoreMinerNode creator, List<SycomoreBlock> parents,
                               Set<SycomoreBlock> uncles, Set<SycomoreTx> txs, long difficulty, double weight) {
        super(0, height, creationTime, creator, parents, uncles, difficulty, weight);
        Txs = txs;

        int totalSize = ETHEREUM_BLOCK_HEADER_SIZE;
        for (SycomoreTx tx:txs) {
            totalSize += tx.getSize();
        }

        this.size = totalSize + (uncles.size() * ETHEREUM_BLOCK_HASH_SIZE);

        long totalGasTemp = 0;
        for (SycomoreTx tx:txs) {
            totalGasTemp += tx.getGas();
        }

        totalGas = totalGasTemp;
    }

    public static SycomoreBlockWithTx generateGenesisBlock(long difficulty) {
        return new SycomoreBlockWithTx(0, 0, null, null, new HashSet<>(),
                new HashSet<>(), difficulty, 0);
    }

    @Override
    public Set<SycomoreTx> getTxs() { return new HashSet<>(this.Txs); }

    public long getTotalGas() {
        return totalGas;
    }
}
