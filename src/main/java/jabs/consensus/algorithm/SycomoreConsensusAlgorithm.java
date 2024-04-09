package jabs.consensus.algorithm;

import jabs.consensus.blockchain.LocalBlockDAG;
import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.Tx;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreBlockUtils;
import jabs.ledgerdata.sycomore.SycomoreChain;
import jabs.ledgerdata.sycomore.SycomoreTx;
import jabs.network.node.nodes.sycomore.BlockHeaderEntry;
import jabs.simulator.Simulator;
import jabs.simulator.event.K_ConfirmationBlockEvent;

import java.util.*;

public class SycomoreConsensusAlgorithm extends AbstractDAGBasedConsensus<SycomoreBlock, SycomoreTx>
         {
   //private final HashMap<, Integer> totalWeights = new HashMap<>();
    public static int DEFAULT_GHOST_WEIGHT = 1;
    protected SycomoreBlock originOfGhost;
    private final double averageBlockMiningInterval;

    private final int CONFIRMATION_DEPTH = 6;

    //LocalBlockDAG<SycomoreBlock> localBlockTree;
    Set<SycomoreBlock> orphanSet;
    private Map<Integer, SycomoreChain> chainSet;

    //With Sycomore we have more than one chain, and each chain has a different label
    // but for each chain we need to keep the head for each chain.
    private Map<Integer, SycomoreBlock> chainHeadSet;


    public SycomoreConsensusAlgorithm(LocalBlockDAG<SycomoreBlock> localBlockDAG, SycomoreProtocolConfig sycomoreProtocolConfig) {
        super(localBlockDAG);
        this.originOfGhost = localBlockDAG.getGenesisBlock();
        this.newIncomingBlock(localBlockDAG.getGenesisBlock());
        this.averageBlockMiningInterval = sycomoreProtocolConfig.averageBlockMiningInterval();
        this.chainHeadSet = new HashMap<>();
        //this.chainSet = new HashMap<>();
        //chainSet.put(1 , new SycomoreChain(1));
        //chainSet.get(1).addBlock(localBlockDAG.getGenesisBlock());
        chainHeadSet.put(1, localBlockDAG.getGenesisBlock());
        //Ok, so we have created the chainSet and added the first block
                 }

    @Override
    public void newIncomingBlock(SycomoreBlock block) {
        Set<SycomoreBlock> allblocks = this.localBlockDAG.getAllBlocks();
        int incomingBlockChainLabel = block.getChainLabel();
        String incomingBlockLabel = block.getLabel();
        int totalIncomingBlockHeight = block.getTotalHeight();
        int incomingBlockHeightInChain = 0;

        //We receive a new block, and we know that this block is connected to genesis
        //We have to check if this block is a new Block or it is a fork

        SycomoreBlock winnerBlock;
        //check if this block is already present in the chain...
        for (SycomoreBlock blockEntry: allblocks){
            if(totalIncomingBlockHeight == blockEntry.getHeight() &&
                    incomingBlockLabel.equals(blockEntry.getLabel()) &&
                    incomingBlockChainLabel == blockEntry.getChainLabel()
                    && blockEntry.getParents().equals(block.getParents())){
                //We have to find which between the two blocks has the highest
                //confirmation level
                HashSet<SycomoreBlock> EntrySuccessors = this.localBlockDAG.getAllSuccessors(blockEntry);
                HashSet<SycomoreBlock> IncomingBlockSuccessors = this.localBlockDAG.getAllSuccessors(block);
                if(findMaxHeight(EntrySuccessors)>findMaxHeight(IncomingBlockSuccessors))
                    winnerBlock=blockEntry;
                else
                    winnerBlock=block;
                chainHeadSet.put(winnerBlock.getChainLabel(), winnerBlock);

                updateChain(); //Dobbiamo aggiornare la situazione dei blocchi confermati
                //In this case we have a fork!!
                //How do we handle forks??
                //TODO
            }
            //In the case we don't have forks
            //We simpy add the block to the chain
            //TODO

        }
        if(nofork){
            //se non si tratta di un fork, ma di un nuovo blocco.
            //e dobbiamo gestire la cosa nel caso si tratti di nuova chain creata e chain eliminata
            if(incomingBlockHeightInChain>chainHeadSet.get(incomingBlockChainLabel).getHeightInChain()){
                chainHeadSet.put(incomingBlockChainLabel, block);
                updateChain();
            }
        }



        //Che facciamo quando riceviamo un nuovo blocco???
        //Forks are due to concurrency
        //How do we resolve forks... a fork happen when we receive a block that has the same label and points
        //to the same predecessor of a block already present in the DAG.
        //Fork rule: keep the syc-dag for which the confirmation level of the genesis block is the largest.
        //Confirmation level: Longest path that commits the presence of
        if(!allblocks.contains(block)){

        }
    }



    //@Override
    protected void updateChain() {
    //This is what happens with ghost protocol
        //this.confirmedBlocks = this.localBlockTree.getAllAncestors(this.currentMainChainHead);
    //In Sycomore case we have more than currentMainChainHeads
    this.confirmedBlocks = new HashSet<SycomoreBlock>();
    //In this case we have more than one chainHead, and for each chainHead we get all the
        //ancestor to put in the confirmedBlock, which is the current good chain.
    chainSet.forEach((key,chain)->{
        SycomoreBlock chainHead = chain.getLeaf();
        this.confirmedBlocks.addAll(this.localBlockDAG.getAllAncestors(chainHead));
    });
    k_confirmed_block_events();
    }


    private void k_confirmed_block_events() {
            //Find the leaf with the maxHeight
        int maxTotalHeight = Integer.MIN_VALUE;

        // Iterate over the values of the map
        for (SycomoreBlock block : chainHeadSet.values()) {
            // Get the total height of the current block
            int totalHeight = block.getTotalHeight();

            // Update maxTotalHeight if necessary
            if (totalHeight > maxTotalHeight) {
                maxTotalHeight = totalHeight;
            }
        }

        int confirmedHeight =  maxTotalHeight-CONFIRMATION_DEPTH;
        HashSet<SycomoreBlock> blockToConfirm = new HashSet<>();
        chainHeadSet.forEach((key, block)->{
            blockToConfirm.addAll(localBlockDAG.getAncestorsWithHeight(,confirmedHeight));
        });


        blockToConfirm.forEach((block)-> {
            Simulator simulator = this.peerDLTNode.getSimulator();
            double currentTime = simulator.getSimulationTime();
            simulator.putEvent(new K_ConfirmationBlockEvent(currentTime,this.peerDLTNode, block), 0);

        });

             }

             private int findMaxHeight(HashSet<SycomoreBlock> blockSet) {
                int maxHeight = 0;
                for (SycomoreBlock block : blockSet){
                    if(block.getHeight()>maxHeight)
                        maxHeight = block.getHeight();
                }
                return maxHeight;
            }
         }