package theory.datastructure;

import caf.datastructure.Caf;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.DungTheory;
import net.sf.tweety.arg.dung.syntax.Attack;
import theory.datastructure.Offer;
import theory.generator.TheoryGenerator;

import java.util.*;

public class Theory{

    private DungTheory dungTheory;
    private Map<Offer, Set<String>> offers;
    private Set<net.sf.tweety.arg.dung.syntax.Argument> controlArguments;
    private Set<net.sf.tweety.arg.dung.syntax.Argument> epistemicArguments;
    private Set<net.sf.tweety.arg.dung.syntax.Argument> practicalArguments;

    enum ArgType{
        EPISTEMIC, PRACTICAL, CONTROL
    }


    public Theory() {
        dungTheory = new DungTheory();
        controlArguments = new HashSet<>();
        epistemicArguments = new HashSet<>();
        practicalArguments = new HashSet<>();
    }

    public Theory(Theory model){
        dungTheory = new DungTheory();
        controlArguments = new HashSet<>();
        epistemicArguments = new HashSet<>();
        practicalArguments = new HashSet<>();
        model.controlArguments.forEach(t-> addControlArgument(t));
        model.epistemicArguments.forEach(t -> addEpistemicArgument(t));
        model.practicalArguments.forEach(t -> addPracticalArgument(t));
    }

    public void addControlArgument(Argument arg)
    {
        dungTheory.add(arg);
        controlArguments.add(arg);
    }

    public void addEpistemicArgument(Argument arg)
    {
        dungTheory.add(arg);
        epistemicArguments.add(arg);
    }

    public void addPracticalArgument(Argument arg)
    {
        dungTheory.add(arg);
        practicalArguments.add(arg);
    }


    public void addControlArgument(String argName)
    {
        Argument argument = new Argument(argName);
        dungTheory.add(argument);
        controlArguments.add(argument);
    }

    public void addEpistemicArgument(String argName)
    {
        Argument argument = new Argument(argName);
        dungTheory.add(argument);
        epistemicArguments.add(argument);
    }
    public void addAttack(Attack attack)
    {
        dungTheory.add(attack);
    }
    public void addPracticalArgument(String argName)
    {
        Argument argument = new Argument(argName);
        dungTheory.add(argument);
        practicalArguments.add(argument);
    }

    public ArgType getArgType(Argument arg)
    {
        if(epistemicArguments.contains(arg))
            return ArgType.EPISTEMIC;
        if(controlArguments.contains(arg))
            return ArgType.CONTROL;
        if(practicalArguments.contains(arg))
            return ArgType.PRACTICAL;
        return null;
    }

    public void removeOfferSupport(Offer offerName, String argumentName) {
        offers.get(offerName).remove(argumentName);
        // retirer de la theory (argument + attaques)
    }

    public boolean hasSupportForOffer(Offer offerName) {
        return !offers.get(offerName).isEmpty();
    }

    public String getSupportForOffer(Offer offer) {
        if(hasSupportForOffer(offer)) {
            return offers.get(offer).stream().findFirst().get();
        }
        return null;
    }

    public void removeOffer(Offer offer) {

    }

    public void removeOfferSupports(Offer offer) {
        for(String support : offers.get(offer)) {
            // retirer de la theory (argument + attaques)
        }
        offers.remove(offer);
    }

    public Offer getNextOffer() {
        return null;
    }

    public boolean argumentIsCredulouslyAccepted(Communication.datastructure.Argument practicalArgument) {
        return false;
    }

    public void update(Collection<Communication.datastructure.Argument> justificationArguments, Collection<Communication.datastructure.Attack> justificationAttacks) {

    }

    public Set<Argument> getControlArguments() {
        return controlArguments;
    }

    public Set<Argument> getEpistemicArguments() {
        return epistemicArguments;
    }

    public Set<Argument> getPracticalArguments() {
        return practicalArguments;
    }

    public DungTheory getDungTheory() {
        return dungTheory;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        epistemicArguments.forEach(
                t -> sb.append(
                        TheoryGenerator.TheoryTag.e_arg.name()
                ).append("(").append(t.getName()).append(").\n")
        );
        practicalArguments.forEach(
                t -> sb.append(
                        TheoryGenerator.TheoryTag.p_arg.name()
                ).append("(").append(t.getName()).append(").\n")
        );
        controlArguments.forEach(
                t -> sb.append(
                        TheoryGenerator.TheoryTag.c_arg.name()
                ).append("(").append(t.getName()).append(").\n")
        );

        dungTheory.getAttacks().forEach(
                t -> sb.append(TheoryGenerator.TheoryTag.att.name())
                        .append("(").append(t.getAttacker().getName())
                        .append(", ").append(t.getAttacked().getName())
                        .append(").\n")
        );




        return sb.toString();
    }
}
