package math.interval;

import java.util.ArrayList;
import java.util.List;

public class SimpleInterval extends Interval {
    private double lower;
    private double upper;

    public SimpleInterval() {
    }

    public SimpleInterval(SimpleInterval model)
    {
        lower = model.lower;
        upper = model.upper;
    }
    public SimpleInterval(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public SimpleInterval simpleIntersect (SimpleInterval other) { ;
        SimpleInterval i = new SimpleInterval(1, 0);
        if (isEmpty() || other.isEmpty())
            return i;

        i.setLower(Math.max(lower, other.lower));
        i.setUpper(Math.min(upper, other.upper));
        return i;
    }

    public Interval intersect(Interval interval)
    {

        if(interval instanceof SimpleInterval) {
            return simpleIntersect((SimpleInterval) interval);
        }
        else if(interval instanceof ComplexeInterval){

            ComplexeInterval complexeInterval = (ComplexeInterval) interval;
            ComplexeInterval intersection = new ComplexeInterval();
            for (SimpleInterval i: complexeInterval.getIntervals()) {
                intersection.addUnion(i.simpleIntersect(this));
            }
            intersection.updateIntervals();
            return intersection;
        }

        return null;

    }

    public Interval simpleUnion(SimpleInterval interval)
    {
        if(simpleIntersect(interval).isEmpty()) {
            List<SimpleInterval> union = new ArrayList<>();
            union.add(this);
            union.add(interval);
            return new ComplexeInterval(union);
        }
        else
            return new SimpleInterval(Math.min(lower, interval.lower),
                    Math.max(upper, interval.upper));
    }
    public Interval union(Interval interval)
    {
        if(interval instanceof SimpleInterval)
            return simpleUnion((SimpleInterval) interval);
        else if (interval instanceof ComplexeInterval){
            ComplexeInterval complexeInterval = (ComplexeInterval)interval;
            ComplexeInterval union = new ComplexeInterval(complexeInterval);
            union.addUnion(this);
            union.updateIntervals();
            return complexeInterval;

        }

        return null ;//ici
    }

    public Double getClosestNumberTo(double number) {
        if(isEmpty())
            return null;

        if(number >= lower && number <= upper)
            return number;

        if(Math.abs(lower - number) < Math.abs(upper - number))
        {
            return lower;
        }
        else
            return upper;
    }


    public Double getDistanceTo(double number){
        if(isEmpty())
            return null;
        return Math.abs(getClosestNumberTo(number) - number);

    }
    @Override
    public void setEmpty() {
        lower = 1;
        upper = 0;
    }

    public double getLower() {
        return lower;
    }

    public void setLower(double lower) {
        this.lower = lower;
    }

    public double getUpper() {
        return upper;
    }

    public void setUpper(double upper) {
        this.upper = upper;
    }

    public boolean isEmpty()
    {
        return lower > upper ;
    }

    @Override
    public String toString() {
        if(isEmpty())
            return "[]";
        else
            return "[" + lower + ", " + upper + "]";
    }
}
