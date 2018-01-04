package theory;
import net.sf.tweety.arg.dung.syntax.Argument;
import theory.datastructure.TheoryGeneration;
import theory.generator.TheoryGenerator;
import theory.generator.config.GenerationConfig;

import java.util.Arrays;
import java.util.List;

public class theory {
    public static void main(String argv[])
    {
        try {
        List<String> args = Arrays.asList(argv);
        System.out.println(args);
        if(args.contains("stats"))
        {
            args.remove("stats");
            args.remove("-t");
            TheoryGenerator generator = new TheoryGenerator();
            TheoryGeneration generation = generator.parseFromFile(args.get(0));

        }


            TheoryGenerator generator = new TheoryGenerator();
            generator.getGenerationConfig().loadConfigFromFile(GenerationConfig.generationConfigFile);
            generator.getGenerationConfig().setCoherent();
            TheoryGeneration g = generator.generate();
            //g.writeToFile("test.theory");

            System.out.println(g.getStats());
//            CafGenerator cg = new CafGenerator();
//            cg.setGenerationConfig(generator.getGenerationConfig());
//            cg.setTheoryGeneration(g);
//            CafGeneration cafGeneration = cg.generate();
//            System.out.println(cafGeneration.getCaf1());
//            System.out.println(cafGeneration.getCaf2());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
