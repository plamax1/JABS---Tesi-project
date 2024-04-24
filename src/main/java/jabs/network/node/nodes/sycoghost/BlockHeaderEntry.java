package jabs.network.node.nodes.sycoghost;

import jabs.ledgerdata.Hash;

public class BlockHeaderEntry {
        private final Hash hash;
        private final String label;

        private final String m;

        public BlockHeaderEntry(Hash hash, String label, String m) {
            this.label = label;
            this.hash = hash;
            this.m = m;
        }

        // Getter methods for each field
        public String getLabel() {
            return label;
        }

        public Hash getHash() {
            return hash;
        }

        public String getM() {
            return m;
        }

}
