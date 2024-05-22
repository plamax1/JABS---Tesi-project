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
import java.util.Iterator;
import java.util.Map;

public class SycoGhostProtocol extends AbstractDAGBasedConsensus<SycoGhostBlock, SycoGhostTx>{
    //in this case we have more than one chain, and each chain has a different label
    private final HashMap<SycoGhostBlock, Integer> totalWeights = new HashMap<>();
    public static int DEFAULT_GHOST_WEIGHT = 1;
    private Map<String, SycoGhostBlock> chainHeadSet;

    private int CONFIRMATION_DEPTH = 6;
    protected SycoGhostBlock originOfGhost;
    public double averageBlockMiningInterval;
    private HashSet<SycoGhostBlock> already_K_confirmed_blocks;
    private int numberofseenblocks = 0;

    public SycoGhostProtocol(LocalBlockDAG<SycoGhostBlock> localBlockDAG, SycoGhostProtocolConfig sycoGhostProtocolConfig) {
        super(localBlockDAG);
        chainHeadSet = new HashMap<String,SycoGhostBlock>();
        chainHeadSet.put("ε", localBlockDAG.getGenesisBlock());
        this.originOfGhost = localBlockDAG.getGenesisBlock();
        this.newIncomingBlock(localBlockDAG.getGenesisBlock());
        this.averageBlockMiningInterval = sycoGhostProtocolConfig.averageBlockMiningInterval();
        this.already_K_confirmed_blocks = new HashSet<SycoGhostBlock>();

    }

    @Override
    public void newIncomingBlock(SycoGhostBlock block) { //here is what happens when we receive a new block
        //this should be triggered when we see block that is not in the localBlockDAG
        numberofseenblocks++;
        if(this.peerDLTNode!=null) {
            //System.err.println("Node id: " + this.peerDLTNode.nodeID + "Number of seen blocks: " + numberofseenblocks);
        }
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
        }
        updateChain();
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
        }

        //HERE WE trigger the confirmation event for the blocks that have reached the confirmation depth
        k_confirmed_block_events();

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

    // Method to get all SycoGhostBlock from chainHeadSet
    public HashSet<SycoGhostBlock> getChainHeads() {
        return new HashSet<>(chainHeadSet.values());
    }

    private void k_confirmed_block_events() {
        //In questa funzione noi abbiamo una vista del DAG, e dobbiamo confermare tutti i
        //blocchi che abbiano raggiunto una certa profondità.
        //System.err.println("K confirmed block events called");

        ///DEF: for all keys in chainHeadSet, we have to confirm all the blocks which have an height of
        //at least chainHeadSet.get(key).getTotalHeight()-CONFIRMATION_DEPTH
        HashSet<SycoGhostBlock> blockToConfirm = new HashSet<>();
        chainHeadSet.forEach((key, block)->{
            //System.err.println("Block: " + block.getTotalHeight() + " Key: " + key );
            blockToConfirm.addAll(getBlocksWithHeight(block, block.getTotalHeight()-CONFIRMATION_DEPTH));
            if(!blockToConfirm.isEmpty()){
                //System.err.println("Block to confirm: " + blockToConfirm.iterator().next().getTotalHeight());
            }
             });
        //System.err.println("444Block to confirm: " + blockToConfirm.size());
        blockToConfirm.forEach((block)-> {
            if(!already_K_confirmed_blocks.contains(block)){
                //System.err.println("CONFIRMING BLOCK: " + block.getTotalHeight());
                Simulator simulator = this.peerDLTNode.getSimulator();
                double currentTime = simulator.getSimulationTime();
                simulator.putEvent(new K_ConfirmationBlockEvent(currentTime,this.peerDLTNode, block), 0);
                already_K_confirmed_blocks.add(block);
            }
        });

    }


    private HashSet<SycoGhostBlock> getBlocksWithHeight(SycoGhostBlock block, int height) {
        //System.out.println("GBWH, getting block with height: " + height + " from block: " + block.getTotalHeight() + " with label: " + block.getLabel());
        //Height is height of the block we want to reach
        HashSet<SycoGhostBlock> blockSet = new HashSet<>();
        HashSet<SycoGhostBlock> blockSetWithHeight = new HashSet<>();
        if (!block.getParents().isEmpty()) {
            blockSet.addAll(block.getParents());
        }
        while (!blockSet.isEmpty()) {
            Iterator<SycoGhostBlock> iterator = blockSet.iterator(); // Create an iterator
            SycoGhostBlock extractedElement = iterator.next(); // Get the next element
            blockSet.remove(extractedElement); // Remove the element from the set
            //System.err.println("Block set size: " + blockSet.size());

            //System.err.println("Current element height: " + extractedElement.getTotalHeight()+ " Target Height: " + height);
            if (extractedElement.getTotalHeight() == height) {
                blockSetWithHeight.add(extractedElement);
            } else {
                if (!extractedElement.getParents().isEmpty() && extractedElement.getTotalHeight() > height) {
                    //System.err.println("Adding parents");
                    blockSet.addAll(extractedElement.getParents());
                }
            }



        }

        return blockSetWithHeight;
    }

}

