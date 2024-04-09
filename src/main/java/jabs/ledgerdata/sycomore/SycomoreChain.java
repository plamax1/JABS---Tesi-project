package jabs.ledgerdata.sycomore;

import java.util.LinkedList;

//All the blocks of a chain have the label of the first block of the chain they belong to.
public class SycomoreChain {
    private int chainLabel ;
    private int chainHeight;
    private SycomoreBlock chainFirstBlock;
    private LinkedList<SycomoreBlock>  chainBlocks;

    public SycomoreChain(int chainLabel){
                this.chainHeight = 0;
                this.chainBlocks = new LinkedList<>();
                this.chainHeight=0;
            }

    public LinkedList<SycomoreBlock> getChainBlocks(){
                return this.chainBlocks;
    }
    public int getChainLabel () {
                return this.chainLabel;
    }
    public int getChainHeight (){
                return this.chainHeight;
    }
    public void addBlock (SycomoreBlock block) {
        chainBlocks.add(block);
        chainHeight++;
    }

    public SycomoreBlock getLeaf () {return this.chainBlocks.getLast();}

    public SycomoreBlock getChainFirstBlock() {
        return chainFirstBlock;
    }
}