package jabs.network.message;

import jabs.ledgerdata.Vote;

public class VoteMessage extends Message {//Vote message
    private final Vote vote;

    public VoteMessage(Vote vote) {
        super(vote.getSize());
        this.vote = vote;
    }

    public Vote getVote() {
        return vote;
    }
}
