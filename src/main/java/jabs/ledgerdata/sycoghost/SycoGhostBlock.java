package jabs.ledgerdata.sycoghost;

import jabs.ledgerdata.Block;
import jabs.ledgerdata.ProofOfWorkBlock;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.network.node.nodes.sycoghost.SGBlockHeader;
import jabs.network.node.nodes.sycoghost.SycoGhostMinerNode;
import jabs.network.node.nodes.sycomore.BlockHeader;
import jabs.network.node.nodes.sycomore.SycomoreMinerNode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HASH_SIZE;

public class SycoGhostBlock extends Block<SycoGhostBlock> implements ProofOfWorkBlock {
    //Try sycomoreblock, let's implement
    //This is the class of the SycomoreBlock
    private final Set<SycoGhostBlock> uncles; //? later not needed

    private final SGBlockHeader header;
    //The min length of a chain is?
    private static final int C_MIN = 5;

    //For the block we have to keep track of the Label of the block,
    //the chainLabel, which is the number of the chain,
    //the height in chain, of which we have to keep track to handle forks ecc.
    //the total height which we'll need to compute the maxPathLength


    private Label label;
    private int chainLabel;
    private int heightInChain;
    private int totalHeight;
    private int load ;
    //the load of a block, we use as load of the block its size
    private final double SPLIT_THRESHOLD = 1029441752;
    private final int MERGE_THRESHOLD = 10;
    private double difficulty;
    private int weight;
    private String hash;
    public SycoGhostBlock(SGBlockHeader header, String block_label, int heightInChain, int totalHeight, int size, double creationTime, SycoGhostMinerNode creator, List<SycoGhostBlock> parents,
                          Set<SycoGhostBlock> uncles, double difficulty, double weight) {
        super(size, totalHeight, creationTime, creator, parents, ETHEREUM_BLOCK_HASH_SIZE);
        this.uncles = uncles;
        this.header= header;
        this.label = new Label(this, block_label);
        //this.chainLabel = chainLabel;
        this.heightInChain = heightInChain;
        this.totalHeight = totalHeight;
        this.difficulty = difficulty;
        this.weight = (int) weight;
    }


    public double getDifficulty() {
        return this.difficulty;

    }

    public static SycoGhostBlock generateGenesisBlock(double difficulty) {
        return new SycoGhostBlock(new SGBlockHeader(),"ε", 0, 0, 0, 0,null, new LinkedList<SycoGhostBlock>()
        , new HashSet<>(), difficulty, 0);
    }

    //return the load of a block
    public int getLoad () {
        return this.getSize();
    }

    private double cmp_spl_mrg() {
        if(this.getParents()!=null){
        //how do we compute the load of a block?
        //dobbiamo esaminare i blocchi da b_c (current block) a b_c-c_min+1
        int i = this.getTotalHeight(); //this is bc
        int tmp_sum = 0;
        SycoGhostBlock cur_block = this;
        while(i >= (i-C_MIN+1)){
            tmp_sum+=cur_block.getLoad();
            cur_block = this.getParents().get(0);
            //We don't need to ensure that we reached the end of the chain
            //because the chain if of course longer than cmin
            i++;}
        //System.err.println("cmp_spl_mrg value: "+ String.valueOf(tmp_sum/C_MIN));
        return (double) tmp_sum / C_MIN;}
        else
            return 0;
    }

    ////////////COMPUTE HASH PROBABLY SHOULD NOT STAY HERE

    private int chainlength (){
        String chainLabel = this.getLabel();
        int length = 1;
        SycoGhostBlock curBlock = this;
        while (!curBlock.getParents().isEmpty() && curBlock.getParents().get(0).getLabel().equals(chainLabel)){
            curBlock= curBlock.getParents().get(0);
            length++;
        }
        return length;
    }

    public boolean isSplittable (){
        if(chainlength()<C_MIN){
            //System.err.println("Chain Length: " + String.valueOf(chainlength()));
            return false;
        }
        //System.err.println("Value of total Load: "+ String.valueOf(cmp_spl_mrg()));
        return cmp_spl_mrg()>SPLIT_THRESHOLD;
    }

    public boolean isMergeable(){
        if(chainlength()<C_MIN)
            return false;
        //System.err.println("Value of total Load: "+ String.valueOf(cmp_spl_mrg()));
        return cmp_spl_mrg()>MERGE_THRESHOLD;

    }
    public String getLabel(){
        return label.getLabelValue();
    }

    public Set<SycoGhostBlock> getUncles() {
        return this.uncles;
    }


    public int getTotalHeight() {return this.totalHeight; }

    public int getHeightInChain() {return this.heightInChain; }


    /**
     * @return
     */
    @Override
    public double getWeight() {
        return this.weight;

    }

    /*public SycomoreBlock getParent () {
        return this.getParents().get(0);
    }*/

    public SGBlockHeader getHeader() {
        return header;
    }
}
