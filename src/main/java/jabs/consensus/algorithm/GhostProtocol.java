package jabs.consensus.algorithm;

import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.Tx;
import jabs.simulator.Simulator;
import jabs.simulator.event.BlockConfirmationEvent;
import jabs.simulator.event.K_ConfirmationBlockEvent;

import java.util.HashMap;
import java.util.HashSet;

public class GhostProtocol<B extends SingleParentBlock<B>, T extends Tx<T>>
        extends AbstractChainBasedConsensus<B, T> {
    private final HashMap<B, Integer> totalWeights = new HashMap<>();
    public static int DEFAULT_GHOST_WEIGHT = 1;
    private int CONFIRMATION_DEPHT = 6;
    protected B originOfGhost;
    public double averageBlockMiningInterval;

    public GhostProtocol(LocalBlockTree<B> localBlockTree, GhostProtocolConfig ghostProtocolConfig) {
        super(localBlockTree);
        this.originOfGhost = localBlockTree.getGenesisBlock();
        this.newIncomingBlock(localBlockTree.getGenesisBlock());
        this.averageBlockMiningInterval = ghostProtocolConfig.averageBlockMiningInterval();
    }

    @Override
    public void newIncomingBlock(B block) { //here is what happens when we receive a new ethereum block
        //System.err.println(this.peerBlockchainNode);
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
        }
    }
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
                    if (totalWeights.get(child)!=null && totalWeights.get(child) > maxWeight) {
                        maxWeight = totalWeights.get(child);
                        block = child;
                    }
                }
            }
        }
    }

    @Override
    protected void updateChain() {
        //this.confirmedBlock is the real confirmed chain, which gets updated after
        //each new block is added
        this.confirmedBlocks = this.localBlockTree.getAllAncestors(this.currentMainChainHead);
        if(this.peerBlockchainNode!=null){
            //System.err.println("Siiiiiiiiiiii");
            Simulator simulator = this.peerBlockchainNode.getSimulator();
            double currentTime = simulator.getSimulationTime();
            simulator.putEvent(new BlockConfirmationEvent(currentTime, this.peerDLTNode, this.currentMainChainHead),0);
        //In this case a block is confirmed, but in case of fork we are not sure this
            //block cannot be reversed
        }

        //Each time a new Block arrives, there is the possibility that the main
        //chain has grown, so an existing Block may have reached the requested confirmation depth
        if(currentMainChainHead.getHeight()>CONFIRMATION_DEPHT){
        B block_pending = this.localBlockTree.getAncestorOfHeight(currentMainChainHead, this.currentMainChainHead.getHeight()-CONFIRMATION_DEPHT);

        //check if(! already_K_confirmed.contains(block_pending) ){}
        Simulator simulator = this.peerBlockchainNode.getSimulator();
        double currentTime = simulator.getSimulationTime();
        simulator.putEvent(new K_ConfirmationBlockEvent(currentTime,this.peerBlockchainNode, block_pending), 0);
    }}




        //quindi in pratica il consensus protocol conferma il blocco???


}

