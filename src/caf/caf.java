package caf;


import org.apache.commons.cli.*;
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

            Options options = new Options();
            Option generate = new Option("g", "generate", false,
                    "if the command must generate a theory.");
            generate.setRequired(true);

            Option input = new Option("i", "input", true,
                    "theory input file.");
            input.setRequired(false);

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
            options.addOption(input);




            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd;
            CafGenerator cafGenerator = new CafGenerator();
            try {
                cmd = parser.parse(options, argv);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("caf", options);

                System.exit(1);
                return;
            }

            if(cmd.hasOption('g'))
            {
                TheoryGenerator theoryGenerator = new TheoryGenerator();
                if(!cmd.hasOption('i'))
                {
                    System.out.println("option i is required.");
                    formatter.printHelp("caf", options);
                    System.exit(1);
                }

                TheoryGeneration theoryGeneration =
                        theoryGenerator.parseFromFile(cmd.getOptionValue('i'));

                cafGenerator.setConfigFile(GenerationConfig.generationConfigFile);

                cafGenerator.setTheoryGeneration(theoryGeneration);
                if(cmd.hasOption('s'))
                {
                    cafGenerator.setSeed(Long.parseLong(cmd.getOptionValue("s")));
                }
                CafGeneration cafGeneration = cafGenerator.generate();
                String outputFile = "output.caf";
                if(cmd.hasOption('o'))
                {
                    outputFile = cmd.getOptionValue("o");
                }
                cafGeneration.writeToFiles(outputFile);

            }
            else
            {
                CafGeneration g = cafGenerator.parseFiles(cmd.getOptionValue('d'));
                System.out.println(g.getStats());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
