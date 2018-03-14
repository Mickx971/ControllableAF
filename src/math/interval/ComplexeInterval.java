package math.interval;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComplexeInterval extends Interval{
    private List<SimpleInterval> intervals;

    public ComplexeInterval() {
        intervals = new ArrayList<>();

    }

    public ComplexeInterval(List<SimpleInterval> intervals) {
        this.intervals = intervals;
    }
    public ComplexeInterval(Interval model) {
        intervals = new ArrayList<>();
        if(model instanceof SimpleInterval)
        {
            intervals.add((SimpleInterval) model);
        }
        else if (model instanceof ComplexeInterval){
            ComplexeInterval complexeInterval = (ComplexeInterval)model;
            for (SimpleInterval i: complexeInterval.getIntervals()) {
                intervals.add(i);
            }
            updateIntervals();
        }

    }


    public List<SimpleInterval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<SimpleInterval> intervals) {
        this.intervals = intervals;
    }

    @Override
    public boolean isEmpty() {
        updateIntervals();
        return intervals.isEmpty();
    }


    public Interval intersect(Interval interval) {
        ComplexeInterval complexeInterval = new ComplexeInterval(this);
        complexeInterval.addIntersect(interval);
        return complexeInterval;
    }


    public Interval union(Interval interval) {

        ComplexeInterval union = new ComplexeInterval(this);
        union.addUnion(interval);
        union.updateIntervals();
        return union;
    }

    @Override
    public void setEmpty() {
        intervals = new ArrayList<>();
    }

    public void addUnion(Interval interval)
    {
        if(interval instanceof SimpleInterval)
            intervals.add((SimpleInterval) interval);
        else if (interval instanceof ComplexeInterval){
            intervals.addAll(((ComplexeInterval)interval).intervals);
        }
    }

    public void addIntersect(Interval interval)
    {
        if(interval instanceof SimpleInterval)
        {
            SimpleInterval simpleInterval = (SimpleInterval) interval;
            for(int i = 0; i<intervals.size(); i++)
            {
                intervals.set(i, intervals.get(i).simpleIntersect(simpleInterval));
            }
        }
        else if(interval instanceof ComplexeInterval)
        {
            ComplexeInterval complexeInterval = (ComplexeInterval)interval;
            List<SimpleInterval> copy = intervals;
            intervals = new ArrayList<>();
            for (SimpleInterval i: copy) {
                for(SimpleInterval j : complexeInterval.getIntervals()){
                    addUnion(j.simpleIntersect(i));
                }
            }

        }
    }

    public void updateIntervals()
    {
        intervals.removeIf(t -> t.isEmpty());
        int listSize = intervals.size();
        int i = 0;
        int j;
        SimpleInterval intersection;
        while( i < listSize - 1)
        {
            for( j = i+1; j < listSize; j++)
            {
                intersection = intervals.get(i).simpleIntersect(intervals.get(j));
                if( !intersection.isEmpty() )
                {
                    intervals.set(i, (SimpleInterval) intervals.get(i).simpleUnion(intervals.get(j)));
                    intervals.remove(j);
                    listSize--;
                    break;
                }
            }

            if(j >= listSize)
            {
                i++;
            }

        }
    }

    @Override
    public String toString() {
        if(isEmpty())
            return "[]";
        else{
            String outString = "{";
            for (int i =0; i<intervals.size() - 1; i++) {

                outString += intervals.get(i).toString() + ", ";
            }

            outString += intervals.get(intervals.size() -1) + "}";
            return outString;
        }
    }
    public Double getClosestNumberTo(double number) {
        if(isEmpty())
            return null;
        SimpleInterval minDistanceInterval = intervals.stream().min(new Comparator<SimpleInterval>() {
            @Override
            public int compare(SimpleInterval o1, SimpleInterval o2) {
                return (int)(o1.getDistanceTo(number) - o2.getDistanceTo(number));
            }
        }).get();

        return minDistanceInterval.getClosestNumberTo(number);

    }

    public Double getDistanceTo(double number){

        if(isEmpty())
            return null;
        return Math.abs(getClosestNumberTo(number) - number);

    }

    public static void main(String argv[])
    {
        List<SimpleInterval> intervals = new ArrayList<>();
        intervals.add(new SimpleInterval(0,1));
        intervals.add(new SimpleInterval(1, 1.5));
        intervals.add(new SimpleInterval(2,3));
        ComplexeInterval complexeInterval = new ComplexeInterval(intervals);

        intervals = new ArrayList<>();
        intervals.add(new SimpleInterval(0,3));
        intervals.add(new SimpleInterval(4.5,4));
        complexeInterval.addIntersect(new ComplexeInterval(intervals));
        complexeInterval.updateIntervals();
        System.out.println(complexeInterval);
    }


}
