package negotiation;

import Communication.datastructure.Argument;
import Communication.datastructure.Attack;
import caf.datastructure.Caf;
import javafx.util.Pair;
import theory.Theory;
import theory.datastructure.Offer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NegotiationEngine {

    private final NegotiationBehaviour communicator;
    private Caf caf;
    private Theory theory;

    public NegotiationEngine(NegotiationBehaviour communicator) {
        this.communicator = communicator;
    }

    public void chooseBestOffer() throws Exception {
        Offer offer = theory.getNextOffer();
        if(offer != null) {
            chooseSupportArg(offer);
        }
        else {
            communicator.sendNothing();
        }
    }

    public void defendOffer(NegotiationMessage message) throws Exception {
        String practicalArgument = message.getPracticalArgument().getName();
        Offer offer = message.getOffer();
        defendOffer(offer, practicalArgument);
    }

    public void defendOffer(Offer offer, String practicalArgument) throws Exception {
        NegotiationMessage proposition = new NegotiationMessage();
        proposition.setOffer(offer);
        proposition.setPracticalArgument(new Argument(practicalArgument));
        proposition.setType(NegotiationMessage.MessageType.OFFER);

        if(theory.argumentIsCredulouslyAccepted(practicalArgument)) {
            theory.removeOfferSupport(offer, practicalArgument);
            communicator.sendMessage(proposition);
        }
        else {
            Set<caf.datastructure.Argument> potentSet = caf.computePSA(practicalArgument);
            if(potentSet != null) {
                proposition.setJustificationArguments(
                    potentSet.stream().map(
                            arg -> new Argument(arg.getName())
                    ).collect(Collectors.toSet())
                );
                proposition.setJustificationAttacks(
                    caf.getFUAttacksFor(potentSet).stream()
                        .map(a -> new Attack(a)).collect(Collectors.toSet())
                );
                communicator.sendMessage(proposition);
            }
            else {
                theory.removeOfferSupport(offer, practicalArgument);
                chooseSupportArg(offer);
            }
        }
    }

    private void chooseSupportArg(Offer offer) throws Exception {
        String support = theory.getSupportForOffer(offer);
        if(support != null) {
            defendOffer(offer, support);
        }
        else {
            theory.removeOfferSupports(offer);
            theory.removeOffer(offer);
            communicator.sendGiveToken();
        }
    }

    public void decideUponOffer() {

    }

    public void updateOnRejection(NegotiationMessage message) throws Exception {
        for(Argument arg : message.getJustificationArguments()) {
            caf.setArgumentCertain(arg.getName());
        }
        for(Attack att : message.getJustificationAttacks()) {
            Optional<caf.datastructure.Attack> uAtt = caf.getUndirectedAttack(att.getSource().getName(), att.getTarget().getName());

            if(uAtt.isPresent())
                uAtt.get().setCertain();

            Optional<caf.datastructure.Attack> udAtt = caf.getUndirectedAttack(att.getSource().getName(), att.getTarget().getName());
            if(udAtt.isPresent())
                caf.removeAttack(udAtt.get());
        }
    }

    public boolean hasOffer() {
        return false;
    }

    public static<T> Pair<Set<T>, Set<T>> addOperator(Set<T> s1, Set<T> s2, Set<T> s3) {
        Set<T> set2 = new HashSet<>(s1);
        set2.retainAll(s3);

        Set<T> set1 = new HashSet<>(s1);
        set1.removeAll(set2);

        set2.addAll(s2);

        return  new Pair<>(set1, set2);
    }

    public static void main(String args[]) {
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
