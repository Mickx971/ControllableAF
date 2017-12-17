package caf.datastructure;

import java.util.*;
import java.util.stream.Collectors;

public class Caf {

    public Map<String, Argument> args;
    public Set<Attack> attacks;

    public Caf() {
        args = new HashMap<>();
        attacks = new HashSet<>();
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

        return sb.toString();
    }
}
