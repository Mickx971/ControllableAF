package negotiation;

import Communication.datastructure.Argument;
import Communication.datastructure.Attack;
import caf.datastructure.Caf;
import theory.datastructure.Theory;
import theory.datastructure.Offer;

import java.util.Collection;
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

        if(caf.argumentIsCredulouslyAccepted(practicalArgument)) {
            theory.removeOfferSupport(offer, practicalArgument);
            communicator.sendMessage(proposition);
        }
        else {
            Set<caf.datastructure.Argument> potentSet = caf.getNextPSA(practicalArgument);
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

    public void decideUponOffer(NegotiationMessage message) throws Exception {
        if(!message.getJustificationArguments().isEmpty()) {
            theory.update(message.getJustificationArguments(), message.getJustificationAttacks());
            update(message.getJustificationArguments(), message.getJustificationAttacks());
        }

        String argName = message.getPracticalArgument().getName();
        if(theory.argumentIsCredulouslyAccepted(message.getPracticalArgument())) {
            communicator.sendAccept(message.getOffer());
        }
        else {
//            Set<Argument> ext = theory.getNextExtensionAttackingArgument(argName);
//            Set<Attack> extAtt = theory.getExtensionOutCertainAttacks(ext);
//            extAtt.removeAll(att -> !att.getTarget().getName().equals(argName));
//            Collection<Argument> attackSources = extAtt.stream().map(att -> att.getSource());

            NegotiationMessage reject = new NegotiationMessage();
//            reject.setOffer(message.getOffer());
//            reject.setPracticalArgument(message.getPracticalArgument());
//            reject.setType(NegotiationMessage.MessageType.REJECT);
//            reject.setJustificationArguments(attackSources);
//            reject.setJustificationAttacks(extAtt);

            communicator.sendMessage(reject);
        }
    }

    public void updateOnRejection(NegotiationMessage message) throws Exception {
        update(message.getJustificationArguments(), message.getJustificationAttacks());
    }

    public void update(Collection<Argument> justificationArguments, Collection<Attack> justificationAttacks) throws Exception {
        for(Argument arg : justificationArguments) {
            caf.setArgumentCertain(arg.getName());
        }
        for(Attack att : justificationAttacks) {
            Optional<caf.datastructure.Attack> uAtt = caf.getUncertainAttack(att.getSource().getName(), att.getTarget().getName());

            if(uAtt.isPresent())
                uAtt.get().setCertain();

            Optional<caf.datastructure.Attack> udAtt = caf.getUndirectedAttack(att.getSource().getName(), att.getTarget().getName());
            if(udAtt.isPresent()) {
                caf.removeAttack(udAtt.get());
                caf.addAttack(att.getSource().getName(), att.getTarget().getName());
            }
        }
    }

    public boolean hasOffer() {
        return false;
    }
}
