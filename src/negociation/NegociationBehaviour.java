package negociation;

import Agents.NegociationAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import theory.datastructure.Offer;

public class NegociationBehaviour extends OneShotBehaviour{

    private static final long TIMEOUT = 1000000;
    private NegociationAgent agent;
    private NegociationEngine negociationEngine;

    public NegociationBehaviour(NegociationAgent agent) {
        super(agent);
        this.agent = agent;
        negociationEngine = new NegociationEngine(agent);
    }

    @Override
    public void action() {
        if(agent.isStartsNegotiation()){
            //call choose best offer
        }

        while (true)
        {
            try {
                NegociationMessage message = negociationEngine.getMessage(TIMEOUT);

                if(message == null)
                    return;

                switch (message.getType()){
                    case REJECT:
                        //some calculus before
                        System.out.println("offer rejected");
                        negociationEngine.defendOffer();
                        break;
                    case ACCEPT:
                        System.out.println("offer accepted");
                        return;
                    case NOTHING:
                        System.out.println("Nothing received");
                        if (negociationEngine.getBestOffer() != null) {
                            negociationEngine.chooseBestOffer();
                        }
                        else {
                            NegociationMessage answer = new NegociationMessage();
                            answer.setType(NegociationMessage.MessageType.NOTHING_TOO);
                            negociationEngine.sendMessage(answer);
                            return;
                        }
                        break;
                    case NOTHING_TOO:
                        System.out.println("nothing_too received");
                        return;
                    case GIVE_TOKEN:
                        System.out.println("give token received");
                        negociationEngine.chooseBestOffer();
                        break;
                    case OFFER:
                        System.out.println("offer received");
                        negociationEngine.decideUponOffer();
                        break;

                }

            }catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
        }


    }




}
