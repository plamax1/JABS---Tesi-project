package jabs.ledgerdata.sycomore;
//All the blocks of a chain have the label of the first block of the chain they belong to.
public class Label {
    private static final int MAX_LABEL_LENGTH = 20;
    private String label;
    private int chainHeight;
    private SycomoreBlock block;

            public Label (SycomoreBlock block, int chainHeight, String label){
                this.block=block;
                this.label=label;
                this.chainHeight=chainHeight;
            }

    public SycomoreBlock getBlock(){
                return this.block;
    }
    public String getLabelValue () {
                return this.label;

    }
    public int getChainHeight (){
                return this.chainHeight;
    }
}