package theory.generator;

import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.syntax.Attack;
import theory.datastructure.Offer;
import theory.datastructure.Theory;
import theory.generator.config.TheoryBasicConfiguration;
import theory.generator.config.GenerationConfig;
import theory.datastructure.TheoryGeneration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TheoryGenerator {

    private GenerationConfig generationConfig;

    private Pattern e_arg = Pattern.compile("e_arg\\((\\w+)\\)\\.");
    private Pattern c_arg = Pattern.compile("c_arg\\((\\w+)\\)\\.");
    private Pattern p_arg = Pattern.compile("p_arg\\((\\w+)\\)\\.");
    private Pattern att = Pattern.compile("att\\(\\s*(\\w+),\\s*(\\w+)\\)\\.");
    private Pattern blankLine = Pattern.compile("\\s*");
    private Pattern support = Pattern.compile("support\\(\\s*(\\w+),\\s*(\\w+)\\)\\.");

    private Random r = new Random();


    public final static String T1_ARG_NAME = "A";
    public final static String T2_ARG_NAME = "B";
    public final static String SHARED_ARG_NAME = "S";
    public final static String CONTROL_ARG_NAME = "C";
    public final static String EPISTEMIC_ARG_NAME = "E";
    public final static String PRACTICAL_ARG_NAME = "P";



    public enum TheoryTag{
        e_arg, p_arg, c_arg, att, support
    }

    public TheoryGenerator() {
        generationConfig = new GenerationConfig();
    }



    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public void setGenerationConfig(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    public TheoryGeneration generate()
    {
        if(generationConfig.isCoherent())
        {
            Theory sharedTheory = generateSharedTheory();
            Theory T1 = generateTheoryFromSharedTheory(
                    sharedTheory, generationConfig.getT1(), T1_ARG_NAME
            );
            Theory T2 = generateTheoryFromSharedTheory(
                    sharedTheory, generationConfig.getT2(), T2_ARG_NAME
            );

            Set<Argument> allPracticalArguments = new HashSet<>(T1.getPracticalArguments());
            allPracticalArguments.addAll(T2.getPracticalArguments());

            HashMap<Offer, Set<String>> offerSupports = generateOfferSupports(
                    generationConfig.getOffersRate(),allPracticalArguments);

            T1.setOffers(reorderRandomly(intersectWithTheoryPracticalArguments(T1, offerSupports)));
            T2.setOffers(reorderRandomly(intersectWithTheoryPracticalArguments(T2, offerSupports)));

            generateAttacksBetweenAllPracticalArguments(T1);
            generateAttacksBetweenAllPracticalArguments(T2);
            generateAttacksBetweenAllPracticalArguments(sharedTheory);

            return new TheoryGeneration(T1, T2, sharedTheory);
        }
        return null;
    }

    private void generateAttacksBetweenAllPracticalArguments(Theory theory) {
        List<Attack> attacksBetweenPracticalArguments = getAllPossibleAttacks(theory.getPracticalArguments(), theory.getPracticalArguments());
        theory.getDungTheory().addAllAttacks(attacksBetweenPracticalArguments);
    }

    public TheoryGeneration parseFromFile(String fileName) throws Exception{
        TheoryGeneration generation = new TheoryGeneration();
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        int lineNumber = 0;

        while(lineNumber<lines.size() && !lines.get(lineNumber).equals("#sharedTheory"))
            lineNumber++;
        int beginningLine = lineNumber;
        List<String> theory = new ArrayList<>();

        while(lineNumber<lines.size() && !lines.get(lineNumber).equals("#T1"))
        {
            theory.add(lines.get(lineNumber));
            lineNumber++;
        }

        generation.setSharedTheory(parseToTheory(theory, beginningLine));

        beginningLine = lineNumber;
        theory = new ArrayList<>();
        while(lineNumber<lines.size() && !lines.get(lineNumber).equals("#T2"))
        {
            theory.add(lines.get(lineNumber));
            lineNumber++;
        }

        Theory t = parseToTheory(theory, beginningLine);
        t.addTheory(generation.getSharedTheory());
        generation.setT1(t);

        beginningLine = lineNumber;
        theory = new ArrayList<>();
        while(lineNumber<lines.size())
        {
            theory.add(lines.get(lineNumber));
            lineNumber++;
        }
        t = parseToTheory(theory, beginningLine);
        t.addTheory(generation.getSharedTheory());
        generation.setT2(t);

        return generation;
    }

    private Theory parseToTheory(List<String> theoryLines, int beginnigLine) throws Exception{
        Theory theory = new Theory();
        Matcher m;
        for (int i = 0; i<theoryLines.size(); i++) {
            m = e_arg.matcher(theoryLines.get(i));
            if(m.matches())
            {
                theory.addEpistemicArgument(m.group(1));
                continue;
            }

            m = p_arg.matcher(theoryLines.get(i));
            if(m.matches())
            {
                theory.addPracticalArgument(m.group(1));
                continue;
            }

            m = c_arg.matcher(theoryLines.get(i));
            if(m.matches())
            {
                theory.addControlArgument(m.group(1));
                continue;
            }

            m = att.matcher(theoryLines.get(i));
            if(m.matches())
            {
                theory.addAttack(m.group(1), m.group(2));
                continue;
            }

            m = support.matcher(theoryLines.get(i));
            if(m.matches())
            {
                theory.addOfferSupport(m.group(1), m.group(2));
                continue;
            }

            m = blankLine.matcher(theoryLines.get(i));
            if(m.matches())
            {
                continue;
            }
            if(theoryLines.get(i).equals("#sharedTheory") ||
                    theoryLines.get(i).equals("#T1") ||
                    theoryLines.get(i).equals("#T2"))
                continue;

            throw new Exception("theory input file error at line: "  + (i + beginnigLine + 1));
        }
        return theory;
    }
    private Theory generateTheoryFromSharedTheory(Theory sharedTheory,
                                                  TheoryBasicConfiguration config,
                                                  String ownerName){
        Theory copy = new Theory(sharedTheory);

        for(int i=sharedTheory.getControlArguments().size();
                i< config.getNbControlArguments(); i++)
        {
            copy.addControlArgument(ownerName + CONTROL_ARG_NAME + i);
        }

        int nbEpistemicArguments =
                config.getNbEpistemicArguments() - config.getNbControlArguments();

        for(int i=sharedTheory.getEpistemicArguments().size();
            i< nbEpistemicArguments; i++)
        {
            copy.addEpistemicArgument(ownerName + EPISTEMIC_ARG_NAME + i);
        }

        for(int i=sharedTheory.getPracticalArguments().size();
            i< config.getNbPracticalArguments(); i++)
        {
            copy.addPracticalArgument(ownerName + PRACTICAL_ARG_NAME + i);
        }



        List<Attack> possibleAttacks = getAllPossibleAttacks(copy);
        possibleAttacks.removeAll(copy.getDungTheory().getAttacks());

        int nbAttacks = config.getNbAttacks() - copy.getDungTheory().getAttacks().size();

        int random;
        for(int i = 0; i<nbAttacks; i++)
        {
            random = r.nextInt(possibleAttacks.size());
            copy.addAttack(possibleAttacks.get(random));
            possibleAttacks.remove(random);
        }
        possibleAttacks = null;
        System.gc();

        return copy;
    }
    private Theory generateSharedTheory()
    {
        if(generationConfig.isCoherent())
        {
            Theory sharedTheory = new Theory();
            int nbEpistemicArguments =
                    generationConfig.getSharedTheory().getNbEpistemicArguments()
                            - generationConfig.getSharedTheory().getNbControlArguments();
            for(int i = 0; i< nbEpistemicArguments; i++)
            {
                sharedTheory.addEpistemicArgument(
                        SHARED_ARG_NAME + EPISTEMIC_ARG_NAME + i
                );
            }

            int nbControlArguments = generationConfig.getSharedTheory()
                    .getNbControlArguments();

            for(int i = 0; i< nbControlArguments; i++)
            {
                sharedTheory.addControlArgument(
                        SHARED_ARG_NAME + CONTROL_ARG_NAME + i
                );
            }

            int nbPracticalArguments = generationConfig.getSharedTheory()
                    .getNbPracticalArguments();

            for(int i = 0; i< nbPracticalArguments; i++)
            {
                sharedTheory.addPracticalArgument(
                        SHARED_ARG_NAME + PRACTICAL_ARG_NAME + i
                );
            }
            List<Attack> possibleAttacks = getAllPossibleAttacks(sharedTheory);

            int random;
            for(int i = 0; i< generationConfig.getSharedTheory().getNbAttacks(); i++)
            {
                random = r.nextInt(possibleAttacks.size());
                sharedTheory.addAttack(possibleAttacks.get(random));
                possibleAttacks.remove(random);
            }

            possibleAttacks = null;
            System.gc();
            return sharedTheory;

        }
        return null;
    }

    private List<Attack> getAllPossibleAttacks(Theory t)
    {
       List<Attack> possibleAttacks = getAllPossibleAttacks(
               t.getControlArguments(),t.getEpistemicArguments()
       );

       possibleAttacks.addAll(
               getAllPossibleAttacks(
                       t.getControlArguments(), t.getControlArguments()
               )
       );


       possibleAttacks.addAll(
               getAllPossibleAttacks(
                       t.getControlArguments(), t.getPracticalArguments()
               )
       );

       possibleAttacks.addAll(
               getAllPossibleAttacks(
                       t.getEpistemicArguments(), t.getEpistemicArguments()
               )
       );

       possibleAttacks.addAll(
               getAllPossibleAttacks(
                       t.getEpistemicArguments(), t.getPracticalArguments()
               )
       );

        possibleAttacks.addAll(
                getAllPossibleAttacks(
                        t.getPracticalArguments(), t.getPracticalArguments()
                )
        );

       return possibleAttacks;

    }

    private List<Attack> getAllPossibleAttacks(Set<Argument> attacker, Set<Argument> attacked)
    {
        List<Attack> possibleAttacks = new ArrayList<>();
        attacker.forEach(_attacker ->{
            attacked.forEach(_attacked ->{
                if(!_attacker.equals(_attacked)){
                    possibleAttacks.add(new Attack(_attacker, _attacked));
                }
            });
        });

        return possibleAttacks;


    }

    public void setSeed(long seed){
        r.setSeed(seed);
    }

    private HashMap<Offer, Set<String>> generateOfferSupports(Collection<Double> offersRate, Collection<Argument> practicalArguments ){
        HashMap<Offer, Set<String>> offersAttributions = new HashMap<>();
        List<Integer> offerNumberOfSupportingArgs =
                offersRate.stream().mapToDouble(t->t*practicalArguments.size())
                        .boxed().mapToInt(t -> t.intValue()).boxed().
                        collect(Collectors.toList());



        List<Argument> practicalArgumentsList = practicalArguments.stream().collect(Collectors.toList());

        int random;
        while(offerNumberOfSupportingArgs.stream().mapToInt(t->t).sum() < practicalArguments.size())
        {
            random = r.nextInt(offerNumberOfSupportingArgs.size());
            offerNumberOfSupportingArgs.set(random, offerNumberOfSupportingArgs.get(random) + 1);
        }

        Integer nbSupports;
        for (int i = 0; i< offerNumberOfSupportingArgs.size(); i++) {
            Offer o = new Offer("O" + i);
            offersAttributions.put(o, new HashSet<>());
            nbSupports = offerNumberOfSupportingArgs.get(i);
            for(int j = 0; j < nbSupports; j++){
                random = r.nextInt(practicalArgumentsList.size());
                offersAttributions.get(o).add(practicalArgumentsList.get(random).getName());
                practicalArgumentsList.remove(random);
            }
        }

        return offersAttributions;

    }

    private LinkedHashMap<Offer, Set<String>> intersectWithTheoryPracticalArguments(
            Theory theory,
            HashMap<Offer, Set<String>> offerArgumentAttribution
            )
    {
        LinkedHashMap<Offer, Set<String>> intersection = new LinkedHashMap<>();

        Set<String> allTheoryArguments = theory.getPracticalArguments().stream()
                .map(a -> a.getName()).collect(Collectors.toSet());

        offerArgumentAttribution.keySet().forEach(o -> {
            HashSet clone = new HashSet<>(offerArgumentAttribution.get(o));
            clone.retainAll(allTheoryArguments);
            intersection.put(o, clone);
        });

       return intersection;
    }

    private LinkedHashMap<Offer, Set<String>> reorderRandomly(LinkedHashMap<Offer, Set<String>> map)
    {
        List<Map.Entry<Offer, Set<String>>> entryList = new ArrayList<>(map.entrySet());

        LinkedHashMap<Offer, Set<String>> newOrder = new LinkedHashMap<>();

        int random = 0;
        while(!entryList.isEmpty())
        {
            random = r.nextInt(entryList.size());
            newOrder.put(entryList.get(random).getKey(), entryList.get(random).getValue());
            entryList.remove(random);
        }

        return newOrder;

    }



}
