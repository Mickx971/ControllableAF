package Agents;

import jade.core.AID;
import negotiation.NegotiationBehaviour;

public class NegotiationAgent extends jade.core.Agent{

    private AID opponent;
    private boolean startsNegotiation;

    public NegotiationAgent() {
        opponent = new AID();
        opponent.setName("rma@172.17.0.1:8888/JADE");
        startsNegotiation = false;
    }


    public NegotiationAgent(AID opponent, boolean startsNegotiation) {
        this.opponent = opponent;
        this.startsNegotiation = startsNegotiation;
    }

    @Override
    protected void setup() {
        Object arguemnts[] = getArguments();
        this.opponent = (AID)(arguemnts[0]);
        this.startsNegotiation = (Boolean)(arguemnts[1]);
        addBehaviour(new NegotiationBehaviour(this));
    }

    public AID getOpponent() {
        return opponent;
    }

    public void setOpponent(AID opponent) {
        this.opponent = opponent;
    }

    public boolean isStartsNegotiation() {
        return startsNegotiation;
    }

    public void setStartsNegotiation(boolean startsNegotiation) {
        this.startsNegotiation = startsNegotiation;
    }
}
