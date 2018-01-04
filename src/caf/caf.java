package caf;


import theory.datastructure.CafGeneration;
import theory.datastructure.Theory;
import theory.datastructure.TheoryGeneration;
import theory.generator.CafGenerator;
import theory.generator.TheoryGenerator;
import theory.generator.config.GenerationConfig;

public class caf {

    public static void main(String argv[])
    {
        try {
//            TheoryGenerator tg = new TheoryGenerator();
//            TheoryGeneration tGen = tg.parseFromFile("test.theory");
//            CafGenerator g = new CafGenerator();
//            g.setConfigFile(GenerationConfig.generationConfigFile);
//            g.setTheoryGeneration(tGen);
//            CafGeneration cafGen = g.generate();
//            cafGen.writeToFiles("test.caf");

            CafGenerator g = new CafGenerator();
            CafGeneration generation = g.parseFiles("test.caf");
            System.out.println(generation.getStats());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
