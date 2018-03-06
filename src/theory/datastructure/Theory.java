package theory.datastructure;

import net.sf.tweety.arg.dung.StableReasoner;
import net.sf.tweety.arg.dung.semantics.Extension;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.DungTheory;
import net.sf.tweety.arg.dung.syntax.Attack;
import net.sf.tweety.commons.util.Pair;
import theory.generator.TheoryGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class Theory{

    private DungTheory dungTheory;
    private SortedMap<Offer, Set<String>> offers;
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
        offers = new TreeMap<>();
    }

    public Theory(Theory model){
        this();
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

    public void addAttack(String fromArg, String toArg)
    {
        dungTheory.add(new Attack(new Argument(fromArg), new Argument(toArg)));
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

    public void removeOfferSupport(Offer offer, String argumentName) throws Exception {
        if(offers.containsKey(offer)) {
            offers.get(offer).remove(argumentName);
            Argument arg = new Argument(argumentName);
            epistemicArguments.remove(arg);
            controlArguments.remove(arg);
            practicalArguments.remove(arg);
            dungTheory.remove(arg);
        }
        else throw new Exception("Unknown offer in theory: " + offer.getName());
    }

    public boolean hasSupportForOffer(Offer offer) {
        return offers.containsKey(offer) && !offers.get(offer).isEmpty();
    }

    public String getSupportForOffer(Offer offer) {
        if(hasSupportForOffer(offer)) {
            return offers.get(offer).stream().findFirst().get();
        }
        return null;
    }

    public void removeOffer(Offer offer) {
        for(String support : offers.get(offer)) {
            Argument practicalArgument = new Argument(support);
            dungTheory.remove(practicalArgument);
            practicalArguments.remove(practicalArgument);
        }
        offers.remove(offer);
    }

    public SortedSet<Offer> getAcceptableOffers() {
        StableReasoner stableReasoner = new StableReasoner(dungTheory);
        Set<String> acceptableArgs = stableReasoner.getExtensions().stream()
                .flatMap(ext -> {
                    ext.retainAll(practicalArguments);
                    return ext.stream();
                }).map(arg -> arg.getName()).collect(Collectors.toSet());

        SortedSet<Offer> acceptableOffers = new TreeSet<>(getOffersComparator());

        for(Map.Entry<Offer, Set<String>> entry: offers.entrySet()) {
            Set<String> supports = entry.getValue();
            supports.retainAll(acceptableArgs);
            if(!supports.isEmpty()) {
                acceptableOffers.add(entry.getKey());
            }
        }

        return acceptableOffers;
    }

    public boolean argumentIsCredulouslyAccepted(Communication.datastructure.Argument practicalArgument) {
        StableReasoner stableReasoner = new StableReasoner(dungTheory);
        Argument dungArg = new Argument(practicalArgument.getName());
        System.out.println(stableReasoner.getExtensions().stream().findFirst().get());
        return stableReasoner.getExtensions().stream().anyMatch(ext -> ext.contains(dungArg));
    }

    // TODO update w.r.t on/off
    public void update(Collection<Communication.datastructure.Argument> justificationArguments, Collection<Communication.datastructure.Attack> justificationAttacks) {
        Collection<Argument> arguments = justificationArguments.stream().map(arg -> new Argument(arg.getName())).collect(Collectors.toSet());
        dungTheory.addAll(arguments);
        justificationAttacks.stream().forEach(att -> addAttack(att.getSource().getName(), att.getTarget().getName()));
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

        for(Map.Entry<Offer, Set<String>> e : offers.entrySet()) {
            for(String practicalArgument: e.getValue())
            {
                sb.append(TheoryGenerator.TheoryTag.support.name())
                        .append("(")
                        .append(practicalArgument)
                        .append(",")
                        .append(e.getKey().getName())
                        .append(").\n");
            }

        }
        return sb.toString();
    }

    public int getMaxNbOfAttacks(){
        return controlArguments.size() * (controlArguments.size() -1)
                + epistemicArguments.size()*(epistemicArguments.size()-1)
                + practicalArguments.size()*(practicalArguments.size()-1)
                + controlArguments.size()*epistemicArguments.size()
                + (controlArguments.size()+epistemicArguments.size())
                * practicalArguments.size();
    }

    public String getStats() {
        return "NumberOfEpistemicArguments: " + (epistemicArguments.size()
                + controlArguments.size())
                + "\nNumberOfControlArguments: " + controlArguments.size()
                + "\nNumberOfPracticalArguments: " + practicalArguments.size()
                + "\nRateOfControlArguments: " + ((double)controlArguments.size()
                /(controlArguments.size() + epistemicArguments.size()))
                + "\nNumberOfAttacks: " + dungTheory.getAttacks().size()
                + "\nAttackDensity: " + ((double)dungTheory.getAttacks().size()
                /getMaxNbOfAttacks()
                +"\nNumberOfOffers:" + offers.size());
    }

    public Pair<Extension, Set<Attack>> getNextExtensionAttackingArgument(String argName) throws Exception {
        StableReasoner stableReasoner = new StableReasoner(dungTheory);
        Argument theta = new Argument(argName);
        Optional<Extension> optEx = stableReasoner.getExtensions().stream().filter(x -> dungTheory.isAttacked(theta, x)).findAny();
        if(optEx.isPresent()) {
            Extension ext = optEx.get();
            Set<Argument> attakers = dungTheory.getAttackers(theta);
            ext.retainAll(attakers);
            Set<Attack> attacks = dungTheory.getAttacks().stream().filter(att -> att.getAttacked().equals(theta)).collect(Collectors.toSet());
            attacks.removeIf(att -> !ext.contains(att.getAttacker()));
            return new Pair<>(ext, attacks);
        }
        else {
            throw new Exception("Invalid state");
        }
    }

    public Map<Offer, Set<String>> getOffers() {
        return offers;
    }

    public void setOffers(SortedMap<Offer, Set<String>> offers) {
        this.offers = offers;
    }

    public void addOfferSupport(String practicalArgument, String offer) {
        Offer copy = new Offer(offer);
        if(offers.get(copy) == null)
            offers.put(copy, new HashSet());
        offers.get(copy).add(practicalArgument);
    }

    public Comparator<Offer> getOffersComparator() {
        return (o1, o2) -> {
            for(Offer offer: offers.keySet()) {
                if(o1.equals(offer))
                    return 1;
                if(o2.equals(offer))
                    return -1;
            }
            return 1;
        };
    }

    public boolean contains(String arg) {
        return dungTheory.contains(new Argument(arg));
    }

    public void addTheory(Theory t) {
        for(Argument ea: t.getEpistemicArguments())
            addEpistemicArgument(ea.getName());

        for(Argument ca: t.getControlArguments())
            addControlArgument(ca.getName());

        for(Argument pa: t.getPracticalArguments())
            addPracticalArgument(pa.getName());

        for(Attack att: t.getDungTheory().getAttacks())
            addAttack(att.getAttacker().getName(), att.getAttacked().getName());
    }
}
