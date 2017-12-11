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

    public Collection<Argument> getUncertainArgument() {
        return args.values().stream().filter(arg -> arg.getType() == Argument.Type.UNCERTAIN).collect(Collectors.toSet());
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
        Argument arg = args.remove(argName);
        return arg.getAllAttacks();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Argument arg: args.values()) {
            sb.append(arg.toString()).append("\n");
        }
        for (Attack att: attacks) {
            sb.append(att.toString()).append("\n");
        }
        return sb.toString();
    }

    public Caf transform(CafConfiguration conf) {
        Caf caf = new Caf();
        LinkedList<Argument> allArg = new LinkedList<>(args.values());

        int fixedArgPart = (int)((conf.FixedPartRate/100) * args.size());
        int uncertainArgPart = (int)((conf.UncertainPartRate/100) * args.size());

        int unknownArgPart = (int)((conf.UnknownArgumentRate/100) * args.size());
        Collections.shuffle(allArg);
        Set<Argument> unknownArgs = new HashSet<>();
        for(int i = 0; i < unknownArgPart; i++) {
            unknownArgs.add(allArg.removeFirst());
        }

        Collections.shuffle(allArg);
        for(int i = 0; i < fixedArgPart; i++) {
            caf.addFixedArgument(allArg.removeFirst().getName());
        }

        Collections.shuffle(allArg);
        for(int i = 0; i < uncertainArgPart; i++) {
            caf.addUncertainArgument(allArg.removeFirst().getName());
        }

        for(Argument a : allArg) {
            caf.addControlArgument(a.getName());
        }

        LinkedList<Attack> allUAttack =  new LinkedList<>();
        for(Argument arg : caf.getUncertainArgument()) {
            allUAttack.addAll(getAllArgAttack(arg.getName()).stream().filter(
                    att -> {
                        Argument[] arguments = att.getArguments();
                        return !unknownArgs.contains(arguments[0]) && !unknownArgs.contains(arguments[1]);
                    }
            ).collect(Collectors.toSet()));
        }

        int undirectedAttackPart = (int)((conf.UndirectedAttackRate/100) * allUAttack.size());
        int uncertainAttackPart = (int)((conf.UncertainAttackRate/100) * allUAttack.size());

        Collections.shuffle(allUAttack);
        for(int i = 0; i < undirectedAttackPart; i++) {
            Argument[] arguments = allUAttack.removeFirst().getArguments();
            caf.addUndirectedAttack(arguments[0].getName(), arguments[1].getName());
        }

        Collections.shuffle(allUAttack);
        for(int i = 0; i < uncertainAttackPart; i++) {
            Argument[] arguments = allUAttack.removeFirst().getArguments();
            caf.addUncertainAttack(arguments[0].getName(), arguments[1].getName());
        }

        for(Attack att : allUAttack) {
            Argument[] arguments = att.getArguments();
            caf.addAttack(arguments[0].getName(), arguments[1].getName());
        }

        return caf;
    }
}
