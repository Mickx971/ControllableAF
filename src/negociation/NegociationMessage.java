package negociation;

import Communication.datastructure.Argument;
import Communication.datastructure.Attack;
import jade.lang.acl.ACLMessage;
import org.codehaus.jackson.map.ObjectMapper;
import scala.Array;
import theory.datastructure.Offer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NegociationMessage {

    private MessageType type;
    private Offer offre;
    private Argument practicalArgument;
    private Collection<Argument> supportingArguments;
    private Collection<Attack> supportingAttacks;



    public enum MessageType{
        ACCEPT, REJECT, NOTHING, NOTHING_TOO, GIVE_TOKEN, OFFER
    }

    public NegociationMessage() { }

    public NegociationMessage(MessageType type, Offer offre, Argument practicalArgument,
                              Collection<Argument> supportingArguments,
                              Collection<Attack> supportingAttacks) {
        this.type = type;
        this.offre = offre;
        this.practicalArgument = practicalArgument;
        this.supportingArguments = supportingArguments;
        this.supportingAttacks = supportingAttacks;
    }

    public static NegociationMessage getNegociationMessage(ACLMessage message) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(message.getContent(), NegociationMessage.class);

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

    public static void main(String args[])
    {
        NegociationMessage negociationMessage = new NegociationMessage();
        negociationMessage.setType(MessageType.ACCEPT);
        negociationMessage.setOffre(new Offer("offre"));
        negociationMessage.setPracticalArgument(new Argument("practicalArgument"));

        Argument[] arguments = {new Argument("arg1"), new Argument("arg2")};
        negociationMessage.setSupportingArguments(Arrays.asList(arguments));

        //Attack[] attacks = {new Attack(arguments[0], arguments[1])};
        //negociationMessage.setSupportingAttacks(Arrays.asList(attacks));

        ACLMessage aclMessage = null;
        try {
            aclMessage = negociationMessage.toACLMessage();
            negociationMessage = NegociationMessage.getNegociationMessage(aclMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(aclMessage.getContent());

        System.out.println(negociationMessage.getOffre().getName());
        System.out.println(negociationMessage.getSupportingAttacks());

    }
    public void setType(MessageType type) {
        this.type = type;
    }

    public Offer getOffre() {
        return offre;
    }

    public void setOffre(Offer offre) {
        this.offre = offre;
    }

    public Argument getPracticalArgument() {
        return practicalArgument;
    }

    public void setPracticalArgument(Argument practicalArgument) {
        this.practicalArgument = practicalArgument;
    }

    public Collection<Argument> getSupportingArguments() {
        return supportingArguments;
    }

    public void setSupportingArguments(Collection<Argument> supportingArguments) {
        this.supportingArguments = supportingArguments;
    }

    public Collection<Attack> getSupportingAttacks() {
        return supportingAttacks;
    }

    public void setSupportingAttacks(Collection<Attack> supportingAttacks) {
        this.supportingAttacks = supportingAttacks;
    }
}
