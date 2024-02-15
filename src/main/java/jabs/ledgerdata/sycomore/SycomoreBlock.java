package jabs.ledgerdata.sycomore;

import jabs.ledgerdata.Block;
import jabs.ledgerdata.ProofOfWorkBlock;
import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.SingleParentPoWBlock;
import jabs.ledgerdata.bitcoin.BitcoinBlockWithoutTx;
import jabs.network.node.nodes.ethereum.EthereumMinerNode;
import jabs.network.node.nodes.sycomore.SycomoreMinerNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_BLOCK_HASH_SIZE;

public class SycomoreBlock extends Block<SycomoreBlock> implements ProofOfWorkBlock {
    //Try sycomoreblock, let's implement
    //This is the class of the SycomoreBlock
    private final Set<SycomoreBlock> uncles; //? later not needed
    private final double difficulty;
    private static final int MAX_LABEL_LENGTH = 20;
    private static final int C_MIN = 10;
    private final byte[] label = new byte[MAX_LABEL_LENGTH];
    //amd we have to add the hash
    private int load ;
    //the load of a block;
    private final int SPLIT_THRESHOLD = 10;
    private final int MERGE_THRESHOLD = 10;
    private final double weight;
    private String hash;
    public SycomoreBlock(int size, int height, double creationTime, SycomoreMinerNode creator, List<SycomoreBlock> parents,
                         Set<SycomoreBlock> uncles, double difficulty, double weight) {
        super(size, height, creationTime, creator, parents, ETHEREUM_BLOCK_HASH_SIZE);
        this.uncles = uncles;
        this.weight = weight;
        this.difficulty = difficulty;

        long unclesDifficultySum = 0;
        for (SycomoreBlock uncle:uncles) {
            unclesDifficultySum += uncle.getDifficulty();
        }
    }

    public Set<SycomoreBlock> getUncles() {
        return this.uncles;
    }

    public double getDifficulty() {
        return this.difficulty;
    }

    public static SycomoreBlock generateGenesisBlock(double difficulty) {
        return new SycomoreBlock(0, 0, 0, null, null, new HashSet<>(), difficulty, 0);
    }

    //return the load of a block
    public int getLoad () {

        return load;
    }
    private double cmp_spl_mrg() {
        //how do we compute the load of a block?
        //dobbiamo esaminare i blocchi da b_c (current block) a b_c-c_min+1
        int i = this.getHeight(); //this is bc
        int tmp_sum = 0;
        SycomoreBlock cur_block = this;
        while(i >= (i-C_MIN+1)){
            tmp_sum+=cur_block.getLoad();
            cur_block = this.getParent();
            i++;}
        return (double) tmp_sum / C_MIN;

    }

    ////////////COMPUTE HASH PROBABLY SHOUD NOT STAY HERE

    public boolean isSplittable (){
        return cmp_spl_mrg()>SPLIT_THRESHOLD;

    }
    public boolean isMergeable(){
        return cmp_spl_mrg()>MERGE_THRESHOLD;

    }
    public byte[] getLabel(){
        return label;
    }
    public int distance (SycomoreBlock b){
        // Result array
        byte[] result = new byte[this.getLabel().length];

        // Perform XOR operation
        for (int i = 0; i < this.getLabel().length; i++) {
            result[i] = (byte) (this.getLabel()[i] ^ b.getLabel()[i]);
        }
        // Initialize distance
        int distance = 0;

        // Iterate through the byte array
        for (int i = 0; i < result.length; i++) {
            // Multiply each byte by 2 raised to the power of its index
            distance += (result[i] & 0xFF) * Math.pow(2, i); // '&' is used to convert byte to unsigned int
        }
        return distance;
    }
    /**
     * @return
     */
    @Override
    public double getWeight() {
        return this.weight;
    }

    public SycomoreBlock getParent () {
        return this.getParents().get(0);
    }

}
