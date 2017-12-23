package Communication.datastructure;

public class Attack {

    private Argument source;
    private Argument target;


    public Attack() {
    }

    public Attack(Argument source, Argument target) {
        this.source = source;
        this.target = target;
    }
    public Attack(caf.datastructure.Attack attack) throws Exception{
        this.source = new Argument(attack.getSource());
        this.target = new Argument(attack.getSource());

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
