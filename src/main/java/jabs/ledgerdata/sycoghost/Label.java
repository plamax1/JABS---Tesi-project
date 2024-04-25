package jabs.ledgerdata.sycoghost;

//All the blocks of a chain have the label of the first block of the chain they belong to.
public class Label {
    private static final int MAX_LABEL_LENGTH = 20;
    private String label;
    private int chainHeight;
    private SycoGhostBlock block;

            public Label (SycoGhostBlock block, String label){
                this.block=block;
                this.label=label;
            }

    public SycoGhostBlock getBlock(){
                return this.block;
    }
    public String getLabelValue () {
                return this.label;

    }
    public int getChainHeight (){
                return this.chainHeight;
    }
}