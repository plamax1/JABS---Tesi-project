package jabs.ledgerdata.sycoghost;

import jabs.ledgerdata.BlockWithTx;
import jabs.network.node.nodes.sycoghost.SGBlockHeader;
import jabs.network.node.nodes.sycoghost.SycoGhostMinerNode;
import jabs.network.node.nodes.sycomore.BlockHeader;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HEADER_SIZE;

public class SycoGhostBlockWithTx extends SycoGhostBlock implements BlockWithTx<SycoGhostTx> {
    //we have a set of transactions and the total gas
    private final Set<SycoGhostTx> Txs;
    private final long totalGas;

    public SycoGhostBlockWithTx(SGBlockHeader header, String block_label, int heightInChain, int totalHeight, double creationTime, SycoGhostMinerNode creator, LinkedList<SycoGhostBlock> parents,
                                Set<SycoGhostBlock> uncles, Set<SycoGhostTx> txs, long difficulty, double weight) {
        super(header,block_label,heightInChain, totalHeight,0, creationTime, creator, parents, uncles, difficulty, weight);
        Txs = txs;

        int totalSize = ETHEREUM_BLOCK_HEADER_SIZE;
        for (SycoGhostTx tx:txs) {
            totalSize += tx.getSize();
        }

        this.size = totalSize; //+ (uncles.size() * ETHEREUM_BLOCK_HASH_SIZE);
        //We update the size after the call of super

        long totalGasTemp = 0;
        for (SycoGhostTx tx:txs) {
            totalGasTemp += tx.getGas();
        }

        totalGas = totalGasTemp;
    }

    public static SycoGhostBlockWithTx generateGenesisBlock(long difficulty) {
        return new SycoGhostBlockWithTx(new SGBlockHeader(), "ε", 0, 0, 0,null, null,
                new HashSet<>(), new HashSet<>(), difficulty, 0);
    }

    @Override
    public Set<SycoGhostTx> getTxs() { return new HashSet<>(this.Txs); }

    public long getTotalGas() {
        return totalGas;
    }
}
