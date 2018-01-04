package theory.generator.config;

public class CafConfig {
    private double rateOfFixedArguments ;
    private double rateOfControlArguments;
    private double rateOfCertainAttacks;
    private double rateOfUncertainAttacks;
    private double rateOfUndirectedAttacks;




    public void testCoherence() throws Exception
    {
        if(rateOfFixedArguments<0 || rateOfFixedArguments > 1)
        {
            throw new Exception("rateOfFixedArguments must be in [0,1]");
        }

        if(rateOfControlArguments<0 || rateOfControlArguments > 1)
        {
            throw new Exception("rateOfControlArguments must be in [0,1]");
        }
        if(rateOfCertainAttacks<0 || rateOfCertainAttacks > 1)
        {
            throw new Exception("rateOfCertainAttacks must be in [0,1]");
        }
        if(rateOfUncertainAttacks<0 || rateOfUncertainAttacks > 1)
        {
            throw new Exception("rateOfUncertainAttacks must be in [0,1]");
        }
        if(rateOfUndirectedAttacks<0 || rateOfUndirectedAttacks > 1)
        {
            throw new Exception("rateOfUndirectedAttacks must be in [0,1]");
        }

        if(rateOfCertainAttacks + rateOfUndirectedAttacks + rateOfUncertainAttacks > 1)
        {
            throw new Exception("the sum of attacks rates is superior than 1");
        }



    }

    public double getRateOfFixedArguments() {
        return rateOfFixedArguments;
    }

    public void setRateOfFixedArguments(double rateOfFixedArguments) {
        this.rateOfFixedArguments = rateOfFixedArguments;
    }

    public double getRateOfControlArguments() {
        return rateOfControlArguments;
    }

    public void setRateOfControlArguments(double rateOfControlArguments) {
        this.rateOfControlArguments = rateOfControlArguments;
    }

    public double getRateOfCertainAttacks() {
        return rateOfCertainAttacks;
    }

    public void setRateOfCertainAttacks(double rateOfCertainAttacks) {
        this.rateOfCertainAttacks = rateOfCertainAttacks;
    }

    public double getRateOfUncertainAttacks() {
        return rateOfUncertainAttacks;
    }

    public void setRateOfUncertainAttacks(double rateOfUncertainAttacks) {
        this.rateOfUncertainAttacks = rateOfUncertainAttacks;
    }

    public double getRateOfUndirectedAttacks() {
        return rateOfUndirectedAttacks;
    }

    public void setRateOfUndirectedAttacks(double rateOfUndirectedAttacks) {
        this.rateOfUndirectedAttacks = rateOfUndirectedAttacks;
    }
}
