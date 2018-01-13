package theory.generator.config;


import caf.datastructure.Caf;
import math.ComplexeInterval;
import math.SimpleInterval;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import theory.datastructure.Offer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;


public class GenerationConfig {

    private TheoryBasicConfiguration T1;
    private TheoryBasicConfiguration T2;
    private TheoryBasicConfiguration sharedTheory;
    private CafConfig caf1;
    private CafConfig caf2;
    private List<Double> offers;
    public final static String generationConfigFile = "generation.config";


    public boolean isCoherent()
    {
        try{
            return !getControlArgumentsCoherenceInterval().isEmpty();
        }
        catch(Exception e){
            return false;
        }
    }

    private ComplexeInterval __getCoherentSolution(TheoryBasicConfiguration t)
    {

        int lowerBound, upperBound;
        lowerBound = t.getNbControlArguments() -
                (t.getNbEpistemicArguments()-sharedTheory.getNbEpistemicArguments());
        if (lowerBound< 0)
            lowerBound = 0;
        upperBound = t.getNbControlArguments();
        if (upperBound > sharedTheory.getNbEpistemicArguments())
            upperBound = sharedTheory.getNbEpistemicArguments();

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

    public CafConfig getCaf1() {
        return caf1;
    }

    public void setCaf1(CafConfig caf1) {
        this.caf1 = caf1;
    }

    public CafConfig getCaf2() {
        return caf2;
    }

    public void setCaf2(CafConfig caf2) {
        this.caf2 = caf2;
    }

    private ComplexeInterval getControlArgumentsCoherenceInterval() throws Exception{
        if(!T1.totalCoherenceCheck()) {
            throw new Exception("T1 theory config incoherent");
        }


        if(!T2.totalCoherenceCheck())
        {
            throw new Exception("T2 theory config incoherent");
        }

        if(sharedTheory.getNbAttacks() > sharedTheory.getMaxNbAttacksWithoutControlArguments()
                || ! sharedTheory.minimalCoherenceCheck())
            throw new Exception("shared thoery config incoherent");

        ComplexeInterval solution = sharedTheory.getNBControlArgumentsCoherenceSolution();

        solution.addIntersect(__getCoherentSolution(T1));
        solution.addIntersect(__getCoherentSolution(T2));
        return solution;
    }

    public int getClosestCoherentNumberOfControlArguments() throws Exception
    {
        ComplexeInterval solution = getControlArgumentsCoherenceInterval();
        if(solution.isEmpty())
        {
            throw new Exception("error, the thoeries are incoherent");
        }


        return solution.getClosestNumberTo(
                sharedTheory.getNbControlArguments()).intValue();

    }

    public void setCoherent()throws Exception{
        int coherentNumberOfControlArguments = getClosestCoherentNumberOfControlArguments();
        if(coherentNumberOfControlArguments != sharedTheory.getNbControlArguments())
            System.out.println("Warning, the number of shared control arguments is " +
                    "set to " + coherentNumberOfControlArguments +
                    ", the value " + sharedTheory.getNbControlArguments() +
                    " is incoherent with the rest"
            );
        sharedTheory.setNbControlArguments(coherentNumberOfControlArguments);
    }

    public void loadConfigFromFile(String fileName) throws Exception{
        Properties prop = new Properties();
        InputStream is = new FileInputStream(fileName);
        prop.load(is);
        TheoryBasicConfiguration t = new TheoryBasicConfiguration();
        t.setNbEpistemicArguments(
                Integer.parseInt(prop.getProperty("T1.nbEpistemicArguments"))
        );
        t.setNbPracticalArguments(
                Integer.parseInt(prop.getProperty("T1.nbPracticalArguments"))
        );

        t.setNbControlArguments(
                new Double(
                        Double.parseDouble(
                                prop.getProperty("T1.rateOfControlArguments"))
                                *t.getNbEpistemicArguments()
                ).intValue()
        );
        t.setNbAttacks(
                new Double(
                        Double.parseDouble(prop.getProperty("T1.density"))
                                *t.getMaxNbAttacksWithoutControlArguments()
                ).intValue()
        );
        setT1(t);

        t = new TheoryBasicConfiguration();
        t.setNbEpistemicArguments(
                Integer.parseInt(prop.getProperty("T2.nbEpistemicArguments"))
        );
        t.setNbPracticalArguments(
                Integer.parseInt(prop.getProperty("T2.nbPracticalArguments"))
        );

        t.setNbControlArguments(
                new Double(
                        Double.parseDouble(
                                prop.getProperty("T2.rateOfControlArguments"))
                                *t.getNbEpistemicArguments()
                ).intValue()
        );
        t.setNbAttacks(
                new Double(
                        Double.parseDouble(prop.getProperty("T2.density"))
                                *t.getMaxNbAttacksWithoutControlArguments()
                ).intValue()
        );
        setT2(t);

        t = new TheoryBasicConfiguration();
        t.setNbEpistemicArguments(
                Integer.parseInt(prop.getProperty("sharedTheory.nbEpistemicArguments"))
        );
        t.setNbPracticalArguments(
                Integer.parseInt(prop.getProperty("sharedTheory.nbPracticalArguments"))
        );

        t.setNbControlArguments(
                new Double(
                        Double.parseDouble(
                                prop.getProperty("sharedTheory.preferredRateOfControlArguments"))
                                *t.getNbEpistemicArguments()
                ).intValue()
        );
        t.setNbAttacks(
                new Double(
                        Double.parseDouble(prop.getProperty("sharedTheory.rateOfAttacks"))
                                *getT1().getNbAttacks()
                ).intValue()
        );

        setSharedTheory(t);

        CafConfig c = new CafConfig();
        c.setRateOfFixedArguments(
                Double.parseDouble(
                        prop.getProperty("caf1.rateOfFixedArguments")
                )
        );
        c.setRateOfControlArguments(
                Double.parseDouble(
                        prop.getProperty("caf1.rateOfControlArguments")
                )
        );
        c.setRateOfCertainAttacks(
                Double.parseDouble(
                        prop.getProperty("caf1.rateOfCertainAttacks")
                )
        );
        c.setRateOfUncertainAttacks(
                Double.parseDouble(
                        prop.getProperty("caf1.rateOfUncertainAttacks")
                )
        );
        c.setRateOfUndirectedAttacks(
                Double.parseDouble(
                        prop.getProperty("caf1.rateOfUndirectedAttacks")
                )
        );

        caf1 = c;
        c = new CafConfig();
        c.setRateOfFixedArguments(
                Double.parseDouble(
                        prop.getProperty("caf2.rateOfFixedArguments")
                )
        );
        c.setRateOfControlArguments(
                Double.parseDouble(
                        prop.getProperty("caf2.rateOfControlArguments")
                )
        );
        c.setRateOfCertainAttacks(
                Double.parseDouble(
                        prop.getProperty("caf2.rateOfCertainAttacks")
                )
        );
        c.setRateOfUncertainAttacks(
                Double.parseDouble(
                        prop.getProperty("caf2.rateOfUncertainAttacks")
                )
        );
        c.setRateOfUndirectedAttacks(
                Double.parseDouble(
                        prop.getProperty("caf2.rateOfUndirectedAttacks")
                )
        );

        caf2 = c;

        String stringOffers = prop.getProperty("offers");
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        offers = objectMapper.readValue(
                stringOffers, typeFactory.constructCollectionType(List.class, Double.class)
        );
        for (Double o : offers) {
            if(o < 0)
                throw new Exception("Error, Offers must be positive");
        }

        if(offers.stream().mapToDouble(d->d).sum() != 1)
        {
            throw new Exception("Error, Sum of offers list must be equal to 1");
        }

    }

    @Override
    public String toString() {
        return "GenerationConfig{\n" +
                "T1=" + T1 +
                ",\nT2=" + T2 +
                ",\nsharedTheory=" + sharedTheory +
                ",\noffers=" + offers +
                "\n}";
    }

    public static void main(String args[])
    {
        try {
            GenerationConfig g = new GenerationConfig();
            g.loadConfigFromFile(generationConfigFile);
            System.out.println(g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
