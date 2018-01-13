package theory.datastructure;

public class Offer {
    String name;

    public Offer() {}

    public Offer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof String) {
            return o.equals(name);
        }
        if(o instanceof Offer) {
            return ((Offer) o).name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
