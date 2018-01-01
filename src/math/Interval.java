package math;

public abstract class Interval {
    public abstract boolean isEmpty();
    public abstract Interval intersect(Interval interval);
    public abstract Interval union(Interval interval);
    public abstract void setEmpty();
    public abstract Double getClosestNumberTo(double number);
    public abstract Double getDistanceTo(double number);


}
