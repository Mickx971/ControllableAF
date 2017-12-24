package Agents;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import negociation.NegociationBehaviour;

public class NegociationAgent extends jade.core.Agent{

    private AID opponent;
    private boolean startsNegotiation;

    public NegociationAgent() {
        opponent = new AID();
        opponent.setName("rma@172.17.0.1:8888/JADE");
        startsNegotiation = false;
    }


    public NegociationAgent(AID opponent, boolean startsNegotiation) {
        this.opponent = opponent;
        this.startsNegotiation = startsNegotiation;
    }

    @Override
    protected void setup() {
        Object arguemnts[] = getArguments();
        this.opponent = (AID)(arguemnts[0]);
        this.startsNegotiation = (Boolean)(arguemnts[1]);
        addBehaviour(new NegociationBehaviour(this));
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
