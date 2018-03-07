package caf.datastructure;

import caf.generator.CafGenerator;
import solver.QuantomConnector;
import theory.datastructure.Offer;

import java.util.*;
import java.util.stream.Collectors;

public class Caf {

    private final QuantomConnector qConnector;
    private Map<String, Argument> args;
    private Set<Attack> attacks;
    private Map<Offer, Set<Argument>> offers = new HashMap<>();

    public Caf() {
        args = new HashMap<>();
        attacks = new HashSet<>();
        qConnector = new QuantomConnector();
    }

    public void addFixedArgument(String argName) {
        args.put(argName, new Argument(argName, Argument.Type.FIXE));
    }

    public void addControlArgument(String argName) {
        args.put(argName, new Argument(argName, Argument.Type.CONTROL));
    }

    public void addUncertainArgument(String argName) {
        args.put(argName, new Argument(argName, Argument.Type.UNCERTAIN));
    }

    public boolean hasArgument(String argName) {
        return args.containsKey(argName);
    }

    public void removeArgument(Argument arg) {
        this.removeArgument(arg.getName());
    }

    public void removeArgument(String argName) {
        Argument arg = args.remove(argName);
        arg.getAllAttacks().forEach(att -> {
            try {
                att.getOther(arg).removeAttack(att);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        attacks.removeAll(arg.getAllAttacks());
    }

    private Collection<Argument> getTypedArguments(Argument.Type type) {
        return args.values().stream().filter(arg -> arg.getType() == type).collect(Collectors.toSet());
    }

    public Collection<Argument> getUncertainArguments() {
        return getTypedArguments(Argument.Type.UNCERTAIN);
    }

    public Collection<Argument> getFixedArguments() {
        return getTypedArguments(Argument.Type.FIXE);
    }

    public Collection<Argument> getControlArguments() {
        return getTypedArguments(Argument.Type.CONTROL);
    }

    public Collection<Argument> getArguments() {
        return args.values();
    }

    public Argument getArgument(String name) {
        return args.get(name);
    }

    public void setArgumentCertain(String name) throws Exception {
        Argument arg = getArgument(name);
        if(arg == null)
            throw new Exception("Unknown argument: " + name);
        arg.setType(Argument.Type.FIXE);
    }

    public boolean isControlArgument(String name) {
        return getControlArguments().stream().anyMatch(arg -> arg.getName().equals(name));
    }

    public void addAttack(String fromArg, String toArg) {
        Attack attack = new Attack(args.get(fromArg), args.get(toArg), Attack.Type.CERTAIN);
        attacks.add(attack);
    }

    public void addUncertainAttack(String fromArg, String toArg) {
        Attack attack = new Attack(args.get(fromArg), args.get(toArg), Attack.Type.UNCERTAIN);
        attacks.add(attack);
    }

    public void addUndirectedAttack(String arg1, String arg2) {
        Attack attack = new Attack(args.get(arg1), args.get(arg2), Attack.Type.UNDIRECTED);
        attacks.add(attack);
    }

    public void removeAttack(Attack att) {
        if(!attacks.remove(att)) {
            System.err.println("Error: Unknown attack " + att.toString());
        }
        Argument[] arguments = att.getArguments();
        arguments[0].removeAttack(att);
        arguments[1].removeAttack(att);
    }

    public Collection<Attack> getAllArgAttack(String argName) {
        Argument arg = args.get(argName);
        return arg.getAllAttacks();
    }

    public Set<Attack> getAttacks() {
        return attacks;
    }

    private Collection<Attack> getTypedAttacks(Attack.Type type) {
        return attacks.stream().filter(att -> att.getType() == type).collect(Collectors.toSet());
    }

    public Collection<Attack> getCertainAttacks() {
        return getTypedAttacks(Attack.Type.CERTAIN);
    }

    public Collection<Attack> getUncertainAttacks() {
        return getTypedAttacks(Attack.Type.UNCERTAIN);
    }

    public Collection<Attack> getUndirectedAttacks() {
        return getTypedAttacks(Attack.Type.UNDIRECTED);
    }

    public Optional<Attack> getAttack(String source, String target) {
        return getCertainAttacks().stream().filter(
            a -> a.getSource().getName().equals(source) && a.getTarget().getName().equals(target)
        ).findFirst();
    }

    public Optional<Attack> getUncertainAttack(String source, String target) {
        return getUncertainAttacks().stream().filter(
            a -> a.getSource().getName().equals(source) && a.getTarget().getName().equals(target)
        ).findFirst();
    }

    public Optional<Attack> getUndirectedAttack(String arg1, String arg2) {
        return getUndirectedAttacks().stream().filter(att -> {
            Argument[] arguments = att.getArguments();
            return arguments[0].getName().equals(arg1) && arguments[1].getName().equals(arg2) ||
                    arguments[1].getName().equals(arg1) && arguments[0].getName().equals(arg2);
        }).findFirst();
    }

    // Les attaques undirected ne font pas parties des outAttaques
    public Set<Attack> getOutAttacksFor(Collection<Argument> potentSet) {
        Set<Attack> attacks = new HashSet<>();
        for(Argument arg : potentSet) {
            attacks.addAll(arg.getOutAttacks());
        }
        return attacks;
    }

    public Collection<Argument> computePSA(String practicalArgument) throws Exception {
        Caf tempCaf = createTempCaf(true);
        if(tempCaf.hasArgument(practicalArgument))
            return qConnector.isCredulouslyAcceptedWithControl(tempCaf, practicalArgument)
                .stream().map(arg -> getArgument(arg.getName())).collect(Collectors.toSet());
        else
            return new HashSet<>();
    }

    public boolean argumentIsCredulouslyAcceptedWithoutControl(String argName) throws Exception {
        Caf tempCaf = createTempCaf(false);
        if(tempCaf.hasArgument(argName))
            return qConnector.isCredulouslyAcceptedWithoutControl(tempCaf, argName);
        else
            return false;
    }

    private Caf createTempCaf(boolean withControl) {
        Caf tempCaf = new Caf();
        getFixedArguments().stream().forEach(arg -> tempCaf.addFixedArgument(arg.getName()));
        getUncertainArguments().stream().forEach(arg -> tempCaf.addUncertainArgument(arg.getName()));

        if(withControl) {
            getControlArguments().stream().forEach(arg -> tempCaf.addControlArgument(arg.getName()));
        }

        for(Attack att: getAttacks()) {

            String arg1, arg2;
            if(att.getType() == Attack.Type.UNDIRECTED) {
                Argument[] arguments = att.getArguments();
                arg1 = arguments[0].getName();
                arg2 = arguments[1].getName();
            }
            else {
                arg1 = att.getSource().getName();
                arg2 = att.getTarget().getName();
            }

            if(tempCaf.hasArgument(arg1) && tempCaf.hasArgument(arg2)) {
                switch (att.getType()) {
                    case UNCERTAIN:
                        tempCaf.addUncertainAttack(arg1, arg2);
                        break;
                    case CERTAIN:
                        tempCaf.addAttack(arg1, arg2);
                        break;
                    case UNDIRECTED:
                        tempCaf.addUndirectedAttack(arg1, arg2);
                        break;
                }
            }
        }
        return tempCaf;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        List<Argument> arguments = new LinkedList<>(getFixedArguments());
        Collections.sort(arguments, Comparator.comparing(Argument::getName));
        for(Argument arg : arguments) {
            sb.append(arg.toString()).append("\n");
        }

        arguments = new LinkedList<>(getControlArguments());
        Collections.sort(arguments, Comparator.comparing(Argument::getName));
        for(Argument arg : arguments) {
            sb.append(arg.toString()).append("\n");
        }

        arguments = new LinkedList<>(getUncertainArguments());
        Collections.sort(arguments, Comparator.comparing(Argument::getName));
        for(Argument arg : arguments) {
            sb.append(arg.toString()).append("\n");
        }

        List<Attack> atts = new LinkedList<>(getCertainAttacks());
        Collections.sort(atts, Comparator.comparing(Attack::toString));
        for (Attack att: atts) {
            sb.append(att.toString()).append("\n");
        }

        atts = new LinkedList<>(getUncertainAttacks());
        Collections.sort(atts, Comparator.comparing(Attack::toString));
        for (Attack att: atts) {
            sb.append(att.toString()).append("\n");
        }

        atts = new LinkedList<>(getUndirectedAttacks());
        Collections.sort(atts, Comparator.comparing(Attack::toString));
        for (Attack att: atts) {
            sb.append(att.toString()).append("\n");
        }

        for(Offer key: offers.keySet())
        {
            for(Argument supportingArgument: offers.get(key))
            {
                sb.append(CafGenerator.CafTag.support.name())
                        .append("(")
                        .append(supportingArgument.getName())
                        .append(",")
                        .append(key.getName())
                        .append(").\n");
            }
        }

        return sb.toString();
    }

    public void setAgentName(String agentName) {
        qConnector.setAgentName(agentName);
    }

    public void addOffer(Offer offer) {
        if(!offers.containsKey(offer)) {
            offers.put(offer, new HashSet<>());
        }
    }

    public void addOffer(String offer) {
        addOffer(new Offer(offer));
    }

    public void addOfferSupport(Offer offer, Argument support) {
        addOffer(offer);
        offers.get(offer).add(support);
    }

    public void addOfferSupport(String offer, String supportArgument) {
        addOfferSupport(new Offer(offer), getArgument(supportArgument));
    }

    public void addOfferSupporters(String offer, Collection<String> supportingArguments) {
        supportingArguments.forEach(arg -> addOfferSupport(offer, arg));
    }

    public boolean hasSupportForOffer(Offer offer) {
        return getSupportForOffer(offer) != null;
    }

    public Argument getSupportForOffer(Offer offer) {
        Set<Argument> supports = offers.get(offer);
        if(supports != null) {
            return supports.stream().findFirst().get();
        }
        return null;
    }

    public void removeOfferSupport(Offer offer, String practicalArgument) throws Exception  {
        if(offers.containsKey(offer)) {
            offers.get(offer).remove(practicalArgument);
            removeArgument(practicalArgument);
        }
        else throw new Exception("Unknown offer in caf: " + offer.getName());
    }

    public void removeOffer(Offer offer) {
        for(Argument support : offers.get(offer)) {
            removeArgument(support);
        }
        offers.remove(offer);
    }
}
