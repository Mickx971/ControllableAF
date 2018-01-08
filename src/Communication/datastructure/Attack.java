package Communication.datastructure;

public class Attack {

    private Argument source;
    private Argument target;


    public Attack() {}

    public Attack(Argument source, Argument target) {
        this.source = source;
        this.target = target;
    }

    public Attack(caf.datastructure.Attack attack) {
        this.source = new Argument(attack.getSource());
        this.target = new Argument(attack.getSource());
    }

    public Attack(net.sf.tweety.arg.dung.syntax.Attack att) {
        this.source = new Argument(att.getAttacker());
        this.target = new Argument(att.getAttacked());
    }

    public Argument getSource() {
        return source;
    }

    public void setSource(Argument source) {
        this.source = source;
    }

    public Argument getTarget() {
        return target;
    }

    public void setTarget(Argument target) {
        this.target = target;
    }
}
