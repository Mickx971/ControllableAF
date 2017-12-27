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
        FIXE,
        CONTROL,
        UNCERTAIN
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
        return type != Type.UNCERTAIN;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
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
                if (att.getType() != Attack.Type.UNDIRECTED && att.getTarget() == this)
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
                if (att.getType() != Attack.Type.UNDIRECTED && att.getSource() == this)
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
        if(type == Type.CONTROL)
            return CafGenerator.CafTag.c_arg.name() + "(" + name + ").";
        return CafGenerator.CafTag.f_arg.name() + "(" + name + ").";
    }

    public boolean equals(Object other) {
        if(other == null)
            return false;
        if(!(other instanceof Argument))
            return false;
        return name.equals(((Argument)other).name);
    }

    public int hashCode() {
        return name.hashCode() * 31;
    }
}
