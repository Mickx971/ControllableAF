package Agents;

import jade.core.AID;
import negotiation.NegotiationBehaviour;

public class NegotiationAgent extends jade.core.Agent{

    private AID opponent;
    private boolean startsNegotiation;
    private int id;
    public static String theoryFileName = "theory.theory";
    public static String cafFileNamePrefix = "caf";
    private boolean useMaxQBF;


    @Override
    protected void setup() {
        try {
            Object arguments[] = getArguments();
            this.opponent = (AID)(arguments[0]);
            this.startsNegotiation = (Boolean)(arguments[1]);
            this.useMaxQBF = (boolean)(arguments[3]);
            id = (int)arguments[2];
            addBehaviour(new NegotiationBehaviour(this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isUseMaxQBF() {
        return useMaxQBF;
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

    public int getId() {
        return id;
    }
}
