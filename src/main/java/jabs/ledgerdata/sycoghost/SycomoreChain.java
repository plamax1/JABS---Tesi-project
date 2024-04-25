package jabs.ledgerdata.sycoghost;

import java.util.LinkedList;

//All the blocks of a chain have the label of the first block of the chain they belong to.
public class SycomoreChain {
    private int chainLabel ;
    private int chainHeight;
    private SycoGhostBlock chainFirstBlock;
    private LinkedList<SycoGhostBlock>  chainBlocks;

    public SycomoreChain(int chainLabel){
                this.chainHeight = 0;
                this.chainBlocks = new LinkedList<>();
                this.chainHeight=0;
            }

    public LinkedList<SycoGhostBlock> getChainBlocks(){
                return this.chainBlocks;
    }
    public int getChainLabel () {
                return this.chainLabel;
    }
    public int getChainHeight (){
                return this.chainHeight;
    }
    public void addBlock (SycoGhostBlock block) {
        chainBlocks.add(block);
        chainHeight++;
    }

    public SycoGhostBlock getLeaf () {return this.chainBlocks.getLast();}

    public SycoGhostBlock getChainFirstBlock() {
        return chainFirstBlock;
    }
}