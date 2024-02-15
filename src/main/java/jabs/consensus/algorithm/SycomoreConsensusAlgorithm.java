package jabs.consensus.algorithm;

import jabs.consensus.blockchain.LocalBlockDAG;
import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.Tx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreTx;

import java.util.HashMap;
import java.util.HashSet;

public class SycomoreConsensusAlgorithm extends AbstractDAGBasedConsensus<SycomoreBlock, SycomoreTx>
         {
   //private final HashMap<, Integer> totalWeights = new HashMap<>();
    public static int DEFAULT_GHOST_WEIGHT = 1;
    protected SycomoreBlock originOfGhost;
    private final double averageBlockMiningInterval;

    public SycomoreConsensusAlgorithm(LocalBlockDAG<SycomoreBlock> localBlockTree, SycomoreProtocolConfig sycomoreProtocolConfig) {
        super(localBlockTree);
        this.originOfGhost = localBlockTree.getGenesisBlock();
        this.newIncomingBlock(localBlockTree.getGenesisBlock());
        this.averageBlockMiningInterval = sycomoreProtocolConfig.averageBlockMiningInterval();
    }

    @Override
    public void newIncomingBlock(SycomoreBlock block) {
        //here there is what happens for every new incoming block
        System.out.println("new block arrived"
        ); /*
        totalWeights.put(block, DEFAULT_GHOST_WEIGHT);
        if (this.localBlockTree.getLocalBlock(block).isConnectedToGenesis) {
            for (B ancestor:this.localBlockTree.getAllAncestors(block)) {
                if (!totalWeights.containsKey(ancestor)) {
                    totalWeights.put(ancestor, DEFAULT_GHOST_WEIGHT);
                }
                totalWeights.put(ancestor, totalWeights.get(ancestor) + DEFAULT_GHOST_WEIGHT);
            }
        }
        B ghostMainChainHead = this.ghost();
        if (this.currentMainChainHead != ghostMainChainHead) {
            this.currentMainChainHead = ghostMainChainHead;
            updateChain();
        }*/
    }
/*
    public B ghost() {
        B block = this.originOfGhost;

        while (true) {
            if (totalWeights.get(block) == 1) {
                return block;
            }

            int maxWeight = 0;
            HashSet<B> children = this.localBlockTree.getChildren(block);
            for (B child: children) {
                if (localBlockTree.getLocalBlock(child).isConnectedToGenesis) {
                    if (totalWeights.get(child) > maxWeight) {
                        maxWeight = totalWeights.get(child);
                        block = child;
                    }
                }
            }
        }
    }

    @Override
    protected void updateChain() {
        this.confirmedBlocks = this.localBlockTree.getAllAncestors(this.currentMainChainHead);
    }*/

         }