package negotiation;

import Agents.NegotiationAgent;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import theory.datastructure.Offer;

public class NegotiationBehaviour extends OneShotBehaviour{

    private static final long TIMEOUT = 60000;
    private NegotiationAgent agent;
    private NegotiationEngine negotiationEngine;

    public NegotiationBehaviour(NegotiationAgent agent) throws Exception{
        super(agent);
        this.agent = agent;
        negotiationEngine = new NegotiationEngine(this, agent);
    }

    @Override
    public void action() {
        try {
            if(agent.isStartsNegotiation()){
                System.out.println("Negotiation starter: " + agent.getLocalName());
                agent.doWait(1000);
                negotiationEngine.chooseBestOffer();
            }
            else {
                System.out.println("# " + agent.getLocalName() + " waits for offer.");
            }
            END: while (true) {
                NegotiationMessage message = getMessage();


                if(message == null) {
                    agent.doWait(100);
                    continue;
                }

                message.print();
                System.out.println("# " + agent.getLocalName() + " is now reasoning.\n");

                switch (message.getType()) {
                    case REJECT:
                        negotiationEngine.updateOnRejection(message);
                        negotiationEngine.defendOffer(message);
                        break;
                    case GIVE_TOKEN:
                        negotiationEngine.chooseBestOffer();
                        break;
                    case OFFER: {
                        if(negotiationEngine.decideUponOffer(message))
                            break END;
                        break;
                    }
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
                        sendShutdownPlatformMessage();
                        break END;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("# Agent " + agent.getLocalName() + " shutdown.");
    }

    public NegotiationMessage getMessage() throws Exception {
        ACLMessage message;
        do {
            message = agent.receive();
            if (message == null)
                return null;
        } while (!message.getSender().getName().equals(agent.getOpponent().getName()));
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

    public void sendShutdownPlatformMessage()
    {
        ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
        Codec codec = new SLCodec();
        myAgent.getContentManager().registerLanguage(codec);
        myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
        shutdownMessage.addReceiver(myAgent.getAMS());
        shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
        try {
            myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
            myAgent.send(shutdownMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NegotiationAgent getAgent() {
        return agent;
    }
}
