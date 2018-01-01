package theory.generator.config;


import math.ComplexeInterval;
import math.SimpleInterval;


public class TheoryGenerationConfig {

    private TheoryBasicConfiguration T1;
    private TheoryBasicConfiguration T2;

    private TheoryBasicConfiguration sharedTheory;



    public TheoryBasicConfiguration getCoherentSharedTheory() throws Exception{
        if(!T1.totalCoherenceCheck()) {
            throw new Exception("first theory config incoherent");
        }


        if(!T2.totalCoherenceCheck())
        {
            throw new Exception("second theory config incoherent");
        }

        if(sharedTheory.getNbAttacks() > sharedTheory.getMaxNbAttacksWithoutControlArguments()
                || ! sharedTheory.minimalCoherenceCheck())
            throw new Exception("shared thoery config incoherent");

        ComplexeInterval solution = sharedTheory.getNBControlArgumentsCoherenceSolution();

        solution.addIntersect(__getCoherentSolution(T1));
        solution.addIntersect(__getCoherentSolution(T2));
        TheoryBasicConfiguration prefferedSharedTheoryConfig = new TheoryBasicConfiguration(sharedTheory);
//        prefferedSharedTheoryConfig.setNbControlArguments(
//                solution.getClosestNumberTo(
//                        sharedTheory.getNbControlArguments()).intValue()
//        );

        return sharedTheory;
    }
    public void setT1Density(double density){
        T1.setNbAttacks(
                new Double(T1.getMaxNbAttacksWithoutControlArguments() * density)
        .intValue());
    }
    public void setT2Density(double density){
        T2.setNbAttacks(
                new Double(T2.getMaxNbAttacksWithoutControlArguments() * density)
                        .intValue());
    }
    private ComplexeInterval __getCoherentSolution(TheoryBasicConfiguration t)
    {

        int lowerBound, upperBound;
        lowerBound = t.getNbControlArguments() -
                (t.getNbEpistimicArguments()-sharedTheory.getNbEpistimicArguments());
        if (lowerBound< 0)
            lowerBound = 0;
        upperBound = t.getNbControlArguments();
        if (upperBound > sharedTheory.getNbEpistimicArguments())
            upperBound = sharedTheory.getNbEpistimicArguments();

        return new ComplexeInterval(new SimpleInterval(lowerBound, upperBound));

    }

    public TheoryBasicConfiguration getT1() {
        return T1;
    }

    public void setT1(TheoryBasicConfiguration t1) {
        T1 = t1;
    }

    public TheoryBasicConfiguration getT2() {
        return T2;
    }

    public void setT2(TheoryBasicConfiguration t2) {
        T2 = t2;
    }

    public TheoryBasicConfiguration getSharedTheory() {
        return sharedTheory;
    }

    public void setSharedTheory(TheoryBasicConfiguration sharedTheory) {
        this.sharedTheory = sharedTheory;
    }

    public static void main(String argv[])
    {
        TheoryGenerationConfig generationConfig = new TheoryGenerationConfig();
        TheoryBasicConfiguration t = new TheoryBasicConfiguration();
        t.setNbEpistimicArguments(4);
        t.setNbPracticalArguments(2);
        t.setNbControlArguments(3);
        t.setNbAttacks(16);
        generationConfig.setT1(t);
        generationConfig.setT2(t);
        t = new TheoryBasicConfiguration();
        t.setNbEpistimicArguments(3);
        t.setNbPracticalArguments(2);
        t.setNbControlArguments(2);
        t.setNbAttacks(12);
        generationConfig.setSharedTheory(t);
        //System.out.println(t.getNBControlArgumentsCoherenceSolution().getClosestNumberTo(1.2));
        try{
            t = generationConfig.getCoherentSharedTheory();
        }catch (Exception e)
        {
            e.printStackTrace();
        }


    }
}
