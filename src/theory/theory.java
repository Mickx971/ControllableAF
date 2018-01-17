package theory;
import net.sf.tweety.arg.dung.syntax.Argument;
import org.apache.commons.cli.*;
import theory.datastructure.TheoryGeneration;
import theory.generator.TheoryGenerator;
import theory.generator.config.GenerationConfig;


public class theory {
    public static void main(String argv[])
    {
        try {

            Options options = new Options();
            Option generate = new Option("g", "generate", false,
                    "if the command must generate a theory.");
            generate.setRequired(true);

            Option seed = new Option("s", "seed", true, "seed");
            seed.setRequired(false);


            Option output = new Option("o", "output", true, "output file");
            output.setRequired(false);

            Option details = new Option("d", "details",
                    true, "print stats of a theory.");
            details.setRequired(true);


            OptionGroup exclusiveOptions = new OptionGroup();
            exclusiveOptions.addOption(generate);
            exclusiveOptions.addOption(details);
            options.addOptionGroup(exclusiveOptions);
            options.addOption(seed);
            options.addOption(output);




            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd;
            try {
                cmd = parser.parse(options, argv);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("theory", options);

                System.exit(1);
                return;
            }
            TheoryGenerator generator = new TheoryGenerator();
            if(cmd.hasOption('g'))
            {

                generator.getGenerationConfig().loadConfigFromFile(GenerationConfig.generationConfigFile);
                generator.getGenerationConfig().setCoherent();
                if(cmd.hasOption('s'))
                {
                    generator.setSeed(Long.parseLong(cmd.getOptionValue("s")));
                }
                TheoryGeneration g = generator.generate();
                String outputFile = "theory.theory";
                if(cmd.hasOption('o'))
                {
                    outputFile = cmd.getOptionValue("o");
                }
                g.writeToFile(outputFile);

            }
            else if(cmd.hasOption("d"))
            {
                TheoryGeneration g = generator.parseFromFile(cmd.getOptionValue('d'));
                System.out.println(g.getStats());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
