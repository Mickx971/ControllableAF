package Communication.datastructure;

public class Argument {

    String name;

    public Argument() {
    }

    public Argument(String name) {
        this.name = name;
    }

    public Argument(caf.datastructure.Argument argument) {
        this.name = argument.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
