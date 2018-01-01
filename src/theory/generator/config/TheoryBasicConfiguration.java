package theory.generator.config;


import math.ComplexeInterval;
import math.SimpleInterval;

public class TheoryBasicConfiguration {

    private int nbEpistimicArguments;
    private int nbPracticalArguments;
    private int nbAttacks;
    private int nbControlArguments;

    public TheoryBasicConfiguration() {
    }

    public TheoryBasicConfiguration(int nbEpistimicArguments, int nbPracticalArguments,
                                    int nbAttacks, int nbControlArguments) {


        this.nbEpistimicArguments = nbEpistimicArguments;
        this.nbPracticalArguments = nbPracticalArguments;
        this.nbAttacks = nbAttacks;
        this.nbControlArguments = nbControlArguments;


    }

    public TheoryBasicConfiguration(TheoryBasicConfiguration model)
    {
        this.nbEpistimicArguments = model.nbEpistimicArguments;
        this.nbPracticalArguments = model.nbPracticalArguments;
        this.nbAttacks = model.nbAttacks;
        this.nbControlArguments = model.nbControlArguments;
    }

    public boolean minimalCoherenceCheck()
    {
        return  nbEpistimicArguments>=0 &&
                nbPracticalArguments >=0&&
                nbAttacks >=0 &&
                nbControlArguments >=0;
    }
    public boolean totalCoherenceCheck()
    {
        return  nbEpistimicArguments>=0 &&
                nbPracticalArguments >=0&&
                nbAttacks >=0&&
                nbControlArguments >=0 &&
                nbControlArguments<=nbEpistimicArguments &&
                nbAttacks<= getMaxNbAttacks();
    }


    public int getMaxNbAttacks()
    {
        return nbControlArguments*(nbControlArguments -1)+
                (nbEpistimicArguments - nbControlArguments)*(nbEpistimicArguments - nbControlArguments -1) +
                nbControlArguments * (nbEpistimicArguments - nbControlArguments) +
                nbEpistimicArguments * nbPracticalArguments +
                nbPracticalArguments * (nbPracticalArguments - 1);

    }

    public int getMaxNbAttacksWithoutControlArguments()
    {
        return  nbEpistimicArguments*(nbEpistimicArguments -1) +
                nbPracticalArguments*(nbPracticalArguments - 1) +
                nbEpistimicArguments * nbPracticalArguments;
    }

    public double getGlobalMinimumOfNbAttacks()
    {
        return -1 * Math.pow(getNbEpistimicArguments(), 2)/4
                + getNbEpistimicArguments()*
                (getNbEpistimicArguments() - 1) +
                getNbPracticalArguments() * (getNbPracticalArguments() -1) +
                getNbEpistimicArguments() * getNbPracticalArguments();

    }

    public ComplexeInterval getNBControlArgumentsCoherenceSolution()
    {
        ComplexeInterval solution = new ComplexeInterval();
        if(getMaxNbAttacksWithoutControlArguments() < getNbAttacks()){
            solution.setEmpty();

        }


        else if(getGlobalMinimumOfNbAttacks() >= getNbAttacks()){
            SimpleInterval simpleInterval = new SimpleInterval();
            simpleInterval.setLower(0);
            simpleInterval.setUpper(getNbEpistimicArguments());
            solution.addUnion(simpleInterval);
        }
        else {

            int solution1 =  (int)Math.floor(
                    -1 * Math.sqrt(getNbAttacks()
                            - getGlobalMinimumOfNbAttacks()) +
                            (double)getNbEpistimicArguments()/2
            );
            int solution2 = (int)Math.ceil(
                    Math.sqrt(getNbAttacks()
                            - getGlobalMinimumOfNbAttacks()) +
                            (double)getNbEpistimicArguments()/2
            );

            solution.addUnion(new SimpleInterval(0, solution1));
            solution.addUnion(new SimpleInterval(solution2, getNbEpistimicArguments()));
        }
        return solution;
    }

    public int getNbEpistimicArguments() {
        return nbEpistimicArguments;
    }

    public void setNbEpistimicArguments(int nbEpistimicArguments) {
        this.nbEpistimicArguments = nbEpistimicArguments;
    }

    public int getNbPracticalArguments() {
        return nbPracticalArguments;
    }

    public void setNbPracticalArguments(int nbPracticalArguments) {
        this.nbPracticalArguments = nbPracticalArguments;
    }

    public int getNbAttacks() {
        return nbAttacks;
    }

    public void setNbAttacks(int nbAttacks) {
        this.nbAttacks = nbAttacks;
    }

    public int getNbControlArguments() {
        return nbControlArguments;
    }

    public void setNbControlArguments(int nbControlArguments) {
        this.nbControlArguments = nbControlArguments;
    }
}
