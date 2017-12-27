package caf.datastructure;

import caf.generator.CafGenerator;

import java.util.HashSet;
import java.util.Set;

public class Attack {

    private Argument arg1;
    private Argument arg2;
    private Type type;

    public enum Type {
        CERTAIN,
        UNCERTAIN,
        UNDIRECTED
    }

    public Attack(Argument arg1, Argument arg2, Type type) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg1.addAttack(this);
        this.arg2.addAttack(this);
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public Argument getSource() {
        if(type == Type.UNDIRECTED) {
            System.err.println("Error: Try to get source of " + this.toString());
            return null;
        }
        return arg1;
    }

    public Argument getTarget() {
        if (type == Type.UNDIRECTED) {
            System.err.println("Error: Try to get target of " + this.toString());
            return null;
        }
        return arg2;
    }

    public Argument getOther(Argument arg) throws Exception {
        if(arg != arg1 && arg != arg2) {
            throw new Exception("Error: Unknown argument" + arg.getName() + " in " + this.toString());
        }
        if(arg == arg1)
            return arg2;
        return arg1;
    }

    public Argument[] getArguments() {
        Argument[] args = new Argument[2];
        args[0] = arg1;
        args[1] = arg2;
        return args;
    }

    public void setSource(Argument arg) throws Exception {
        if (type != Type.UNDIRECTED)
            throw new Exception("Error: Try to set source to directed " + this.toString());
        if (arg != arg1 && arg != arg2)
            throw new Exception("Error: Unknown argument" + arg.getName() + " in " + this.toString());
        if (arg2 == arg) {
            arg2 = arg1;
            arg1 = arg;
            this.type = Type.CERTAIN;
        }
    }

    public void setCertain() throws Exception {
        if(type != Attack.Type.UNCERTAIN) {
            throw new Exception("Error: Try to make " + toString() + " certain.");
        }
        this.type = Type.CERTAIN;
    }

    public String toString() {
        String string = null;
        switch (type) {
            case CERTAIN: string = CafGenerator.CafTag.att.name() + "("; break;
            case UNCERTAIN: string = CafGenerator.CafTag.u_att.name() + "("; break;
            case UNDIRECTED: string = CafGenerator.CafTag.ud_att.name() + "("; break;
        }
        return string + arg1.getName() + "," + arg2.getName() + ").";
    }
}
