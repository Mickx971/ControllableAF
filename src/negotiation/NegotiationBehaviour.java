package negotiation;

import Agents.NegotiationAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import theory.datastructure.Offer;

public class NegotiationBehaviour extends OneShotBehaviour{

    private static final long TIMEOUT = 1000000;
    private NegotiationAgent agent;
    private NegotiationEngine negotiationEngine;

    public NegotiationBehaviour(NegotiationAgent agent) throws Exception{
        super(agent);
        this.agent = agent;
        negotiationEngine = new NegotiationEngine(this);
    }

    @Override
    public void action() {
        try {
            if(agent.isStartsNegotiation()){
                negotiationEngine.chooseBestOffer();
            }
            END: while (true) {
                NegotiationMessage message = getMessage(TIMEOUT);

                if(message == null)
                    throw new Exception("Message received is null");

                message.print();

                switch (message.getType()) {
                    case REJECT:
                        negotiationEngine.updateOnRejection(message);
                        negotiationEngine.defendOffer(message);
                        break;
                    case GIVE_TOKEN:
                        negotiationEngine.chooseBestOffer();
                        break;
                    case OFFER:
                        negotiationEngine.decideUponOffer(message);
                        break;
                    case NOTHING: {
                        if (negotiationEngine.hasOffer()) {
                            negotiationEngine.chooseBestOffer();
                            break;
                        } else {
                            sendNothingToo();
                            break END;
                        }
                    }
                    case ACCEPT:
                    case NOTHING_TOO:
                        break END;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NegotiationMessage getMessage(long timeout) throws Exception {
        ACLMessage message = agent.blockingReceive(timeout);
        if(message == null)
            return null;
        return NegotiationMessage.getNegotiationMessage(message);
    }

    public void sendMessage(NegotiationMessage message) throws Exception {
        ACLMessage aclMessage = message.toACLMessage();
        aclMessage.addReceiver(agent.getOpponent());
        agent.send(aclMessage);
    }

    public void sendNothingToo() throws Exception {
        NegotiationMessage answer = new NegotiationMessage();
        answer.setType(NegotiationMessage.MessageType.NOTHING_TOO);
        sendMessage(answer);
    }

    public void sendGiveToken() throws Exception {
        NegotiationMessage answer = new NegotiationMessage();
        answer.setType(NegotiationMessage.MessageType.GIVE_TOKEN);
        sendMessage(answer);
    }

    public void sendNothing() throws Exception {
        NegotiationMessage answer = new NegotiationMessage();
        answer.setType(NegotiationMessage.MessageType.NOTHING);
        sendMessage(answer);
    }

    public void sendAccept(Offer offer) throws Exception {
        NegotiationMessage answer = new NegotiationMessage();
        answer.setType(NegotiationMessage.MessageType.ACCEPT);
        answer.setOffer(offer);
        sendMessage(answer);
    }

    @Override
    public NegotiationAgent getAgent() {
        return agent;
    }
}
