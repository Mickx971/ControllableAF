package theory.generator.config;


import math.ComplexeInterval;
import math.SimpleInterval;

public class TheoryBasicConfiguration {

    private int nbEpistemicArguments;
    private int nbPracticalArguments;
    private int nbAttacks;
    private int nbControlArguments;

    public TheoryBasicConfiguration() {
    }


    public boolean minimalCoherenceCheck()
    {
        return  nbEpistemicArguments>=0 &&
                nbPracticalArguments >=0&&
                nbAttacks >=0 &&
                nbControlArguments >=0;
    }
    public boolean totalCoherenceCheck()
    {
        return  nbEpistemicArguments>=0 &&
                nbPracticalArguments >=0&&
                nbAttacks >=0&&
                nbControlArguments >=0 &&
                nbControlArguments<=nbEpistemicArguments &&
                nbAttacks<= getMaxNbAttacks();
    }


    public int getMaxNbAttacks()
    {
        return nbControlArguments*(nbControlArguments -1)+
                (nbEpistemicArguments - nbControlArguments)*
                (nbEpistemicArguments - nbControlArguments -1) +
                nbControlArguments * (nbEpistemicArguments - nbControlArguments) +
                nbEpistemicArguments * nbPracticalArguments +
                nbPracticalArguments * (nbPracticalArguments - 1);

    }

    public int getMaxNbAttacksWithoutControlArguments()
    {
        return  nbEpistemicArguments*(nbEpistemicArguments -1) +
                nbPracticalArguments*(nbPracticalArguments - 1) +
                nbEpistemicArguments * nbPracticalArguments;
    }

    public double getGlobalMinimumOfNbAttacks()
    {
        return -1 * Math.pow(getNbEpistemicArguments(), 2)/4
                + getNbEpistemicArguments()*
                (getNbEpistemicArguments() - 1) +
                getNbPracticalArguments() * (getNbPracticalArguments() -1) +
                getNbEpistemicArguments() * getNbPracticalArguments();

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
            simpleInterval.setUpper(getNbEpistemicArguments());
            solution.addUnion(simpleInterval);
        }
        else {

            int solution1 =  (int)Math.floor(
                    -1 * Math.sqrt(getNbAttacks()
                            - getGlobalMinimumOfNbAttacks()) +
                            (double)getNbEpistemicArguments()/2
            );
            int solution2 = (int)Math.ceil(
                    Math.sqrt(getNbAttacks()
                            - getGlobalMinimumOfNbAttacks()) +
                            (double)getNbEpistemicArguments()/2
            );

            solution.addUnion(new SimpleInterval(0, solution1));
            solution.addUnion(new SimpleInterval(solution2, getNbEpistemicArguments()));
        }
        return solution;
    }

    public int getNbEpistemicArguments() {
        return nbEpistemicArguments;
    }

    public void setNbEpistemicArguments(int nbEpistemicArguments) {

        this.nbEpistemicArguments = nbEpistemicArguments;
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

    public int getTotalNumberOfArguments()
    {
        return nbEpistemicArguments + nbPracticalArguments;
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

    @Override
    public String toString() {
        return "TheoryBasicConfiguration{" +
                "nbEpistemicArguments=" + nbEpistemicArguments +
                ", nbPracticalArguments=" + nbPracticalArguments +
                ", nbAttacks=" + nbAttacks +
                ", nbControlArguments=" + nbControlArguments +
                '}';
    }
}
