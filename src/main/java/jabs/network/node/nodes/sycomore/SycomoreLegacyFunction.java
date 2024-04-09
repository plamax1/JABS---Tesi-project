package jabs.network.node.nodes.sycomore;

import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.ledgerdata.sycomore.SycomoreBlockUtils;
import jabs.ledgerdata.sycomore.SycomoreBlockWithTx;
import jabs.ledgerdata.sycomore.SycomoreTx;
import jabs.network.message.DataMessage;
import jabs.network.message.Packet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
/*
public class SycomoreLegacyFunction {

    public void generateNewBlocklegacy() {
        //What do we do:
        //1 - Create the header of the block:
        //1.1 - Find all the leaves of the chain
        int height;

        Set<SycomoreBlock> leafBlocks = this.localBlockTree.getChildlessBlocks();

        //1.2 - Compute the header of the new block
        //In the case of split chain (So if the block is splittable), we have to
        //Split the block in 2 parts, so 2 entries will appear in the header.


        BlockHeader header = new BlockHeader();
        for (SycomoreBlock block : leafBlocks) {
            //Among the leaf blocks we check is there is some block splittable
            if (block.isSplittable()){

                //in case block is splittable we add 2 references to Header
                String tmp_next_label = block.getLabel().toString() + '0';
                BlockHeaderEntry headerEntry = new BlockHeaderEntry(block.getHash(),tmp_next_label,compute_m());
                header.add(headerEntry);
                tmp_next_label = block.getLabel().toString() + '1';
                headerEntry = new BlockHeaderEntry(block.getHash(),tmp_next_label,compute_m());
                header.add(headerEntry);
            }
            if(block.isMergeable()){
                //We have to check if the next block is mergeable
                SycomoreBlock near;
                for (SycomoreBlock near_block : leafBlocks){
                    if (SycomoreBlockUtils.binaryDistance(block.getLabel(), near_block.getLabel())==1){
                        near = near_block;
                        if(near.isMergeable()){
                            BlockHeaderEntry headerEntry =  new BlockHeaderEntry(block.getHash(),block.getLabel().substring(0, block.getLabel().length()-1),compute_m());
                            header.add(headerEntry);
                        }
                        break;
                    }
                }

            }
            else{
                //the block is a normal block
                BlockHeaderEntry headerEntry = new BlockHeaderEntry(block.getHash(),block.getLabel(),compute_m());
                header.add(headerEntry);

            }
        }
        //once the header is constructed we use POW to find the nonce v...
        //1.3 once we constructed the header we find the nonce v
        int nonce = find_nonce( header, 10);
        //here we pass the header to this function and the function will return us the
        //choosen label
        //1.4 Now we have the block to which append the new block
        LinkedList<SycomoreBlock> parents = find_predecessors(header, (short) nonce);
        //TODO we have to get the parents from the labels
        //TODO implement multiple blocks... in case of mergeable/splittble blocks

        //1.5 Add transactions in the block

        Set<SycomoreTx> blockTxs = new HashSet<>();
        long totalGas = 0;
        for (SycomoreTx sycomoreTx:memPool) { //for each transaction in the mempool
            if ((totalGas + sycomoreTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                break;
            }
            blockTxs.add(sycomoreTx);
            totalGas += sycomoreTx.getGas();
        }

        //1.6 Create the new Block
        //The height in sycomoreblock is the legth of the label?
        SycomoreBlockWithTx sycomoreBlockWithTx = new SycomoreBlockWithTx(header, parents.getFirst().getHeight()+1, simulator.getSimulationTime(),
                this, parents,null, blockTxs, 0,0);
        System.err.println("New Syco Block Created");
        System.err.println("Label: "+ sycomoreBlockWithTx.getLabel()+ "* from miner: " + sycomoreBlockWithTx.getCreator() + "height: " + sycomoreBlockWithTx.getHeight());
        //1.7 process the new block
        this.processIncomingPacket(
                new Packet(
                        this, this, new DataMessage(sycomoreBlockWithTx)
                )
        );
    }

}
        */