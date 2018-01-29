package negotiation;

import Agents.NegotiationAgent;
import Communication.datastructure.Argument;
import Communication.datastructure.Attack;
import caf.datastructure.Caf;
import caf.generator.CafGenerator;
import com.google.common.collect.Streams;
import javafx.util.Pair;
import net.sf.tweety.arg.dung.semantics.Extension;
import theory.datastructure.Theory;
import theory.datastructure.Offer;
import theory.datastructure.TheoryGeneration;
import theory.generator.TheoryGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
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

        if(caf.argumentIsCredulouslyAcceptedWithoutControl(practicalArgument)) {
            theory.removeOfferSupport(offer, practicalArgument);
            communicator.sendMessage(proposition);
        }
        else {
            Collection<caf.datastructure.Argument> potentSet = caf.computePSA(practicalArgument);
            if(potentSet != null && !potentSet.isEmpty()) {
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
            theory.removeOffer(offer);
            communicator.sendGiveToken();
        }
    }

    public boolean decideUponOffer(NegotiationMessage message) throws Exception {
        if(message.getJustificationArguments()!= null &&
                !message.getJustificationArguments().isEmpty()) {
            theory.update(message.getJustificationArguments(), message.getJustificationAttacks());
            update(message.getJustificationArguments(), message.getJustificationAttacks());
        }

        String argName = message.getPracticalArgument().getName();
        if(theory.argumentIsCredulouslyAccepted(message.getPracticalArgument())) {
            communicator.sendAccept(message.getOffer());
            return true;
        }
        else {
            Pair<Extension, Set<net.sf.tweety.arg.dung.syntax.Attack>> reason = theory.getNextExtensionAttackingArgument(argName);
            Collection<Argument> arguments = reason.getKey().stream().map(arg -> new Argument(arg))
                    .collect(Collectors.toSet());
            Collection<Attack> attacks = reason.getValue().stream().map(att -> new Attack(att))
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
        update(message.getJustificationArguments(), message.getJustificationAttacks());
    }

    public Theory getTheory() {
        return theory;
    }

    public void update(Collection<Argument> justificationArguments, Collection<Attack> justificationAttacks) throws Exception {
        Set<Argument> arguments = Streams.concat(
                justificationArguments.stream(),
                justificationAttacks.stream().map(Attack::getSource),
                justificationAttacks.stream().map(Attack::getTarget)
        ).collect(Collectors.toSet());

        for(Argument arg : arguments) {
            if(caf.hasArgument(arg.getName()))
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
        return theory.hasOffer();
    }
}
