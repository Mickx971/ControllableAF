package negociation;

import jade.lang.acl.ACLMessage;

public class NegociationMessage {
    //l'offre
    //l'argument Pratique
    //collection d'arguments de justification
    //liste des attaques

    private MessageType type;


    public enum MessageType{
        ACCEPT, REJECT, NOTHING, NOTHING_TOO, GIVEN_TOKEN, OFFER
    }

    public NegociationMessage(/*parametres*/) {

    }

    public NegociationMessage(ACLMessage message) {

    }

    public ACLMessage toACLMessage() {
        return null;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
