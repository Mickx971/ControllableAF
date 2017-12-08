package caf.datastructure;

import caf.generator.CafGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Argument {

    private Type type;
    private final String name;
    private Set<Attack> attacks;

    public enum Type {
        CERTAIN,
        UNCERTAIN
    }

    public Argument(String argName) {
        this(argName, Type.CERTAIN);
    }

    public Argument(String argName, Type type) {
        this.name = argName;
        this.type = type;
        attacks = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public boolean isCertain() {
        return type == Type.CERTAIN;
    }

    public void setCertain() {
        type = Type.CERTAIN;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void addAttack(Attack att) {
        attacks.add(att);
    }

    public void removeAttack(Attack att) {
        if(!attacks.remove(att))
            System.err.println("Error: Unknown attack " + att.toString());
    }

    public Set<Attack> getAllAttacks() {
        return new HashSet<>(attacks);
    }

    public Set<Attack> getInAttacks() {
        return attacks.stream().filter(att -> {
            try {
                if (att.getType() != Attack.Type.UNDIRECTED && att.getSource() == this)
                    return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }).collect(Collectors.toSet());
    }

    public Set<Attack> getOutAttacks() {
        return attacks.stream().filter(att -> {
            try {
                if (att.getType() != Attack.Type.UNDIRECTED && att.getTarget() == this)
                    return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }).collect(Collectors.toSet());
    }

    public Set<Attack> getUndirectedAttacks() {
        return attacks.stream().filter(att -> att.getType() == Attack.Type.UNDIRECTED).collect(Collectors.toSet());
    }

    public String toString() {
        if(type == Type.UNCERTAIN)
            return CafGenerator.CafTag.u_arg.name() + "(" + name + ").";
        return CafGenerator.CafTag.arg.name() + "(" + name + ").";
    }
}
