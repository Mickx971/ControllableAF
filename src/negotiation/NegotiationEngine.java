package negotiation;

import Agents.NegotiationAgent;
import Communication.datastructure.Argument;
import Communication.datastructure.Attack;
import caf.datastructure.Caf;
import caf.generator.CafGenerator;
import com.google.common.collect.Streams;
import net.sf.tweety.arg.dung.semantics.Extension;
import net.sf.tweety.commons.util.Pair;
import theory.datastructure.Theory;
import theory.datastructure.Offer;
import theory.datastructure.TheoryGeneration;
import theory.generator.TheoryGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class NegotiationEngine {

    private final NegotiationBehaviour communicator;
    private final NegotiationAgent agent;
    private Caf caf;
    private Theory theory;

    public NegotiationEngine(NegotiationBehaviour communicator, NegotiationAgent agent) throws Exception{
        this.communicator = communicator;
        this.agent = agent;
        TheoryGenerator g = new TheoryGenerator();
        TheoryGeneration generation = g.parseFromFile(NegotiationAgent.theoryFileName);
        if(communicator.getAgent().getId() == 1)
            theory = generation.getT1();
        else
            theory = generation.getT2();

        CafGenerator cg = new CafGenerator();
        caf = cg.parseCAF(NegotiationAgent.cafFileNamePrefix + communicator.getAgent().getId() + ".caf");
        caf.setAgentName(agent.getLocalName());
    }

    public void chooseBestOffer() throws Exception {
        Offer offer = computeNextOffer();
        if(offer != null) {
            chooseSupportArg(offer);
        }
        else {
            communicator.sendNothing();
        }
    }

    private Offer computeNextOffer() {
        SortedSet<Offer> offers = theory.getAcceptableOffers();
        for(Offer offer: offers) {
            if(caf.hasSupportForOffer(offer))
                return offer;
        }
        return null;
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

        System.out.println("\n");
        System.out.println("# " + agent.getLocalName() + " wants to propose " + offer.getName() + " with the argument " + practicalArgument + ".");
        System.out.println("# " + agent.getLocalName() + " is checking if " + practicalArgument + " is accepted without control.");

        if(caf.argumentIsCredulouslyAcceptedWithoutControl(practicalArgument)) {
            removeOfferSupport(offer, practicalArgument);
            communicator.sendMessage(proposition);
        }
        else {

            System.out.println("# " + practicalArgument + " is not accepted without control.");
            System.out.println("# " + agent.getLocalName() + " is searching a potent set to defend " + practicalArgument + ".");

            Collection<caf.datastructure.Argument> potentSet = caf.computePSA(practicalArgument);
            if(potentSet != null && !potentSet.isEmpty()) {

                System.out.println("# Potent set found");

                proposition.setJustificationArguments(
                    potentSet.stream().map(
                            arg -> new Argument(arg.getName())
                    ).collect(Collectors.toSet())
                );
                proposition.setJustificationAttacks(
                    caf.getOutAttacksFor(potentSet).stream()
                        .map(a -> new Attack(a)).collect(Collectors.toSet())
                );

                communicator.sendMessage(proposition);
            }
            else {
                System.out.println("# Potent set not found");
                removeOfferSupport(offer, practicalArgument);
                chooseSupportArg(offer);
            }
        }
    }

    private void removeOfferSupport(Offer offer, String practicalArgument) throws Exception {
        theory.removeOfferSupport(offer, practicalArgument);
        caf.removeOfferSupport(offer, practicalArgument);
    }

    private void chooseSupportArg(Offer offer) throws Exception {
        caf.datastructure.Argument support = caf.getSupportForOffer(offer);
        if(support != null) {
            defendOffer(offer, support.getName());
        }
        else {
            removeOffer(offer);
            communicator.sendGiveToken();
        }
    }

    private void removeOffer(Offer offer) {
        theory.removeOffer(offer);
        caf.removeOffer(offer);
    }

    // TODO compute all the path in the resons
    public boolean decideUponOffer(NegotiationMessage message) throws Exception {
        if(message.getJustificationArguments()!= null &&
                !message.getJustificationArguments().isEmpty()) {
            updateTheory(message.getJustificationArguments(), message.getJustificationAttacks());
            updateCaf(message.getJustificationArguments(), message.getJustificationAttacks());
        }

        String argName = message.getPracticalArgument().getName();
        if(theory.argumentIsCredulouslyAccepted(message.getPracticalArgument())) {
            communicator.sendAccept(message.getOffer());
            return true;
        }
        else {
            Pair<Extension, Set<net.sf.tweety.arg.dung.syntax.Attack>> reason = theory.getNextExtensionAttackingArgument(argName);
            Collection<Argument> arguments = reason.getFirst().stream().map(arg -> new Argument(arg))
                    .collect(Collectors.toSet());
            Collection<Attack> attacks = reason.getSecond().stream().map(att -> new Attack(att))
                    .collect(Collectors.toSet());

            NegotiationMessage reject = new NegotiationMessage();
            reject.setOffer(message.getOffer());
            reject.setPracticalArgument(message.getPracticalArgument());
            reject.setType(NegotiationMessage.MessageType.REJECT);
            reject.setJustificationArguments(arguments);
            reject.setJustificationAttacks(attacks);

            communicator.sendMessage(reject);
        }
        return false;
    }

    public void updateOnRejection(NegotiationMessage message) throws Exception {
        updateCaf(message.getJustificationArguments(), message.getJustificationAttacks());
    }

    public Theory getTheory() {
        return theory;
    }

    public void updateTheory(Collection<Argument> justificationArguments, Collection<Attack> justificationAttacks) throws Exception {
        Set<Argument> arguments = Streams.concat(
                justificationArguments.stream(),
                justificationAttacks.stream().map(Attack::getSource),
                justificationAttacks.stream().map(Attack::getTarget)
        ).collect(Collectors.toSet());
        theory.update(arguments, justificationAttacks);
    }

    public void updateCaf(Collection<Argument> justificationArguments, Collection<Attack> justificationAttacks) throws Exception {
        Set<Argument> arguments = Streams.concat(
                justificationArguments.stream(),
                justificationAttacks.stream().map(Attack::getSource),
                justificationAttacks.stream().map(Attack::getTarget)
        ).collect(Collectors.toSet());

        for(Argument arg : arguments) {
            if(caf.hasArgument(arg.getName())) // on a préféré gérer le cas où l'argument n'existe pas dans le caf. Si ce cas n'est pas géré, pour une utilisation future, cela pourrait générer un bug.
                caf.setArgumentCertain(arg.getName());
            else
                caf.addFixedArgument(arg.getName());
        }

        for(Attack att : justificationAttacks) {
            Optional<caf.datastructure.Attack> uAtt = caf.getUncertainAttack(att.getSource().getName(), att.getTarget().getName());
            if(uAtt.isPresent()) {
                uAtt.get().setCertain();
                continue;
            }

            Optional<caf.datastructure.Attack> udAtt = caf.getUndirectedAttack(att.getSource().getName(), att.getTarget().getName());
            if(udAtt.isPresent()) {
                caf.removeAttack(udAtt.get());
                caf.addAttack(att.getSource().getName(), att.getTarget().getName());
                continue;
            }

            Optional<caf.datastructure.Attack> a = caf.getAttack(att.getSource().getName(), att.getTarget().getName());
            if(!a.isPresent()) {
                caf.addAttack(att.getSource().getName(), att.getTarget().getName());
            }
        }
    }

    public boolean hasOffer() {
        return computeNextOffer() != null;
    }
}
