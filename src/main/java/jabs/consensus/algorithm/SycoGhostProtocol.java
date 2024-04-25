package jabs.consensus.algorithm;

import jabs.consensus.blockchain.LocalBlockDAG;
import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycoGhostProtocolConfig;
import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.Tx;
import jabs.ledgerdata.sycoghost.SycoGhostBlock;
import jabs.ledgerdata.sycoghost.SycoGhostTx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreTx;
import jabs.simulator.Simulator;
import jabs.simulator.event.BlockConfirmationEvent;
import jabs.simulator.event.K_ConfirmationBlockEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SycoGhostProtocol extends AbstractDAGBasedConsensus<SycoGhostBlock, SycoGhostTx>{
    //in this case we have more than one chain, and each chain has a different label
    private final HashMap<SycoGhostBlock, Integer> totalWeights = new HashMap<>();
    public static int DEFAULT_GHOST_WEIGHT = 1;
    private Map<String, SycoGhostBlock> chainHeadSet;

    private int CONFIRMATION_DEPHT = 6;
    protected SycoGhostBlock originOfGhost;
    public double averageBlockMiningInterval;

    public SycoGhostProtocol(LocalBlockDAG<SycoGhostBlock> localBlockDAG, SycoGhostProtocolConfig sycoGhostProtocolConfig) {
        super(localBlockDAG);
        this.originOfGhost = localBlockDAG.getGenesisBlock();
        this.newIncomingBlock(localBlockDAG.getGenesisBlock());
        chainHeadSet.put("ε", localBlockDAG.getGenesisBlock());
        this.averageBlockMiningInterval = sycoGhostProtocolConfig.averageBlockMiningInterval();
    }

    @Override
    public void newIncomingBlock(SycoGhostBlock block) { //here is what happens when we receive a new ethereum block
        //System.err.println(this.peerBlockchainNode);
        totalWeights.put(block, DEFAULT_GHOST_WEIGHT);
        if (this.localBlockDAG.getLocalBlock(block).isConnectedToGenesis) {
            for (SycoGhostBlock ancestor:this.localBlockDAG.getAllAncestors(block)) {
                if (!totalWeights.containsKey(ancestor)) {
                    totalWeights.put(ancestor, DEFAULT_GHOST_WEIGHT);
                }
                totalWeights.put(ancestor, totalWeights.get(ancestor) + DEFAULT_GHOST_WEIGHT);
            }
        }
        SycoGhostBlock ghostCurrentLabelMainChainHead = this.ghost();
        if (this.chainHeadSet.get(block.getLabel()) != ghostCurrentLabelMainChainHead) {
            this.chainHeadSet.put(block.getLabel(), ghostCurrentLabelMainChainHead);
            updateChain();
        }
    }
    public SycoGhostBlock ghost() {
        SycoGhostBlock block = this.originOfGhost;

        while (true) {
            if (totalWeights.get(block) == 1) {
                return block;
            }

            int maxWeight = 0;
            HashSet<SycoGhostBlock> children = this.localBlockDAG.getChildren(block);
            for (SycoGhostBlock child: children) {
                if (localBlockDAG.getLocalBlock(child).isConnectedToGenesis) {
                    if (totalWeights.get(child)!=null && totalWeights.get(child) > maxWeight) {
                        maxWeight = totalWeights.get(child);
                        block = child;
                    }
                }
            }
        }
    }

    protected void updateChain() {
        //this.confirmedBlock is the real confirmed chain, which gets updated after
        //each new block is added
        //this.confirmedBlocks = this.localBlockDAG.getAllAncestors(this.currentMainChainHead);
        for (SycoGhostBlock block: this.chainHeadSet.values()) {
            this.confirmedBlocks.addAll(this.localBlockDAG.getAllAncestors(block));
        }/*
        if(this.peerDLTNode!=null){
            //System.err.println("Siiiiiiiiiiii");
            Simulator simulator = this.peerDLTNode.getSimulator();
            double currentTime = simulator.getSimulationTime();
            simulator.putEvent(new BlockConfirmationEvent(currentTime, this.peerDLTNode, this.currentMainChainHead),0);
        //In this case a block is confirmed, but in case of fork we are not sure this
            //block cannot be reversed
        }*/

        //Each time a new Block arrives, there is the possibility that the main
        //chain has grown, so an existing Block may have reached the requested confirmation depth
        /*
        if(currentMainChainHead.getHeight()>CONFIRMATION_DEPHT){
        B block_pending = this.localBlockDAG.getAncestorOfHeight(currentMainChainHead, this.currentMainChainHead.getHeight()-CONFIRMATION_DEPHT);

        //check if(! already_K_confirmed.contains(block_pending) ){}
        Simulator simulator = this.peerDLTNode.getSimulator();
        double currentTime = simulator.getSimulationTime();
        simulator.putEvent(new K_ConfirmationBlockEvent(currentTime,this.peerDLTNode, block_pending), 0);
    }
    */
    }

    public void setAverageBlockMiningInterval(double averageBlockMiningInterval) {
        this.averageBlockMiningInterval = averageBlockMiningInterval;
    }
    public int getWeight (SycoGhostBlock block){
        return totalWeights.get(block);
    }


        //quindi in pratica il consensus protocol conferma il blocco???


}

