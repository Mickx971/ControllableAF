package negociation;

import Agents.NegociationAgent;
import jade.lang.acl.ACLMessage;
import javafx.util.Pair;
import theory.datastructure.Offer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NegociationEngine {

    private NegociationAgent agent;

    public NegociationEngine(NegociationAgent agent) {
        this.agent = agent;
    }

    public void chooseBestOffer(){}

    public void defendOffer(){}

    public void decideUponOffer(){}
    public Offer getBestOffer(){return null;}

    public NegociationMessage getMessage(long timeout) throws Exception{

        ACLMessage message = agent.blockingReceive(timeout);
        if(message == null)
            return null;
        return NegociationMessage.getNegociationMessage(message);
    }

    public void sendMessage(NegociationMessage message) throws Exception{
        ACLMessage aclMessage = message.toACLMessage();
        aclMessage.addReceiver(agent.getOpponent());
        agent.send(aclMessage);

    }

    public static<T> Pair<Set<T>, Set<T>>
    addOperator(Set<T> s1, Set<T> s2, Set<T> s3) {
        Set<T> set2 = new HashSet<>(s1);
        set2.retainAll(s3);

        Set<T> set1 = new HashSet<>(s1);
        set1.removeAll(set2);

        set2.addAll(s2);

        return  new Pair<>(set1, set2);

    }
    public static void main(String args[])
    {
        Integer s1[] = {1,2,3,4};
        Integer s2[] = {1};
        Integer s3[] = {3,4,5,6};
        Pair<Set<Integer>, Set<Integer>> pair =
                addOperator(new HashSet<Integer>(Arrays.asList(s1)),
                        new HashSet<Integer>(Arrays.asList(s2)),
                        new HashSet<Integer>(Arrays.asList(s3)));

        for (Integer i: pair.getKey()) {
            System.out.println(i);
        }

        for (Integer i: pair.getValue()) {
            System.out.print(i);
        }
    }

}
