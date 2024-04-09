package jabs.ledgerdata.ethereum;

import jabs.ledgerdata.BlockWithTx;
import jabs.network.node.nodes.ethereum.EthereumMinerNode;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HEADER_SIZE;
import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HASH_SIZE;

import java.util.HashSet;
import java.util.Set;

public class EthereumBlockWithTx extends EthereumBlock implements BlockWithTx<EthereumTx> {
    //this is an ethereum block with transactions and we have a set of transactions...
    private final Set<EthereumTx> Txs;
    private final long totalGas; //and the total gas

    public EthereumBlockWithTx(int height, double creationTime, EthereumMinerNode creator, EthereumBlock parent,
                               Set<EthereumBlock> uncles, Set<EthereumTx> txs, long difficulty, double weight) {
        super(0, height, creationTime, creator, parent, uncles, difficulty, weight);
        Txs = txs;

        int totalSize = ETHEREUM_BLOCK_HEADER_SIZE; //start with size 0
        for (EthereumTx tx:txs) {
            totalSize += tx.getSize();
        }

        this.size = totalSize + (uncles.size() * ETHEREUM_BLOCK_HASH_SIZE);

        long totalGasTemp = 0;
        for (EthereumTx tx:txs) {
            totalGasTemp += tx.getGas();
        }

        totalGas = totalGasTemp;
    }
//Ok so we get the total size of the block, and the total gas
    public static EthereumBlockWithTx generateGenesisBlock(long difficulty) {
        return new EthereumBlockWithTx(0, 0, null, null, new HashSet<>(),
                new HashSet<>(), difficulty, 0);
    }
//We use this static function to simply generate genesis block
    @Override
    public Set<EthereumTx> getTxs() { return new HashSet<>(this.Txs); }

    public long getTotalGas() {
        return totalGas;
    }
}
