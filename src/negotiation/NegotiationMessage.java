package negotiation;

import Communication.datastructure.Argument;
import Communication.datastructure.Attack;
import jade.lang.acl.ACLMessage;
import org.codehaus.jackson.map.ObjectMapper;
import theory.datastructure.Offer;

import java.util.Arrays;
import java.util.Collection;

public class NegotiationMessage {

    private MessageType type;
    private Offer offer;
    private Argument practicalArgument;
    private Collection<Argument> justificationArguments;
    private Collection<Attack> justificationAttacks;
    private String sender;


    public enum MessageType {
        ACCEPT, REJECT, NOTHING, NOTHING_TOO, GIVE_TOKEN, OFFER
    }

    public NegotiationMessage() {}

    public NegotiationMessage(MessageType type, Offer offer, Argument practicalArgument,
                              Collection<Argument> justificationArguments,
                              Collection<Attack> justificationAttacks) {
        this.type = type;
        this.offer = offer;
        this.practicalArgument = practicalArgument;
        this.justificationArguments = justificationArguments;
        this.justificationAttacks = justificationAttacks;
    }

    public static NegotiationMessage getNegotiationMessage(ACLMessage message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        NegotiationMessage negMess = objectMapper.readValue(message.getContent(), NegotiationMessage.class);
        negMess.setSender(message.getSender().getLocalName());
        return negMess;
    }

    public ACLMessage toACLMessage() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent(objectMapper.writeValueAsString(this));
        return message;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Argument getPracticalArgument() {
        return practicalArgument;
    }

    public void setPracticalArgument(Argument practicalArgument) {
        this.practicalArgument = practicalArgument;
    }

    public Collection<Argument> getJustificationArguments() {
        return justificationArguments;
    }

    public void setJustificationArguments(Collection<Argument> args) {
        this.justificationArguments = args;
    }

    public Collection<Attack> getJustificationAttacks() {
        return justificationAttacks;
    }

    public void setJustificationAttacks(Collection<Attack> attacks) {
        this.justificationAttacks = attacks;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void print() throws Exception {
        System.out.println(toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message: \n");
        sb.append("\tSender:\t" + sender + "\n");
        sb.append("\tType:\t" + type + "\n");
        sb.append("\tOffer:\t" + offer + "\n");
        sb.append("\targ:\t" + (practicalArgument==null ? null:practicalArgument.getName()) + "\n");
        if(justificationArguments != null && !justificationArguments.isEmpty()) {
            sb.append("\treason arg :\n");
            for (Argument arg : justificationArguments) {
                sb.append("\t\t" + arg.getName() + "\n");
            }
        }
        if(justificationAttacks != null && !justificationAttacks.isEmpty()) {
            sb.append("\treason att :\n");
            for (Attack att : justificationAttacks) {
                sb.append("\t\t(" + att.getSource().getName() +", "+ att.getTarget().getName() + ")\n");
            }
        }
        return sb.toString();
    }

    public static void main(String args[])
    {
        NegotiationMessage negotiationMessage = new NegotiationMessage();
        negotiationMessage.setType(MessageType.ACCEPT);
        negotiationMessage.setOffer(new Offer("offer"));
        negotiationMessage.setPracticalArgument(new Argument("practicalArgument"));

        Argument[] arguments = {new Argument("arg1"), new Argument("arg2")};
        negotiationMessage.setJustificationArguments(Arrays.asList(arguments));

        //Attack[] attacks = {new Attack(arguments[0], arguments[1])};
        //negotiationMessage.setSupportingAttacks(Arrays.asList(attacks));

        ACLMessage aclMessage = null;
        try {
            aclMessage = negotiationMessage.toACLMessage();
            negotiationMessage = NegotiationMessage.getNegotiationMessage(aclMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(aclMessage.getContent());

        System.out.println(negotiationMessage.getOffer().getName());
        System.out.println(negotiationMessage.getJustificationArguments());

    }
}
