package theory.generator;

import caf.datastructure.Caf;
import javafx.util.Pair;
import net.sf.tweety.arg.dung.semantics.ArgumentStatus;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.syntax.Attack;
import scala.collection.parallel.ParIterableLike;
import theory.datastructure.CafGeneration;
import theory.datastructure.Theory;
import theory.generator.config.CafConfig;
import theory.generator.config.GenerationConfig;
import theory.datastructure.TheoryGeneration;

import java.util.*;
import java.util.stream.Collectors;

public class CafGenerator {
    TheoryGeneration theoryGeneration;
    GenerationConfig generationConfig;

    private Random r;
    public CafGenerator() {
        generationConfig = new GenerationConfig();
        r = new Random();
    }

    public CafGeneration generate() throws Exception {
        generationConfig.testCoherence();
        return new CafGeneration(
                generateCaf(
                        generationConfig.getCaf1(), theoryGeneration.getT1(), theoryGeneration.getT2()
                ),generateCaf(
                generationConfig.getCaf2(), theoryGeneration.getT2(), theoryGeneration.getT1()
        )
        );
    }

    public CafGeneration parseFiles(String fileName) throws Exception
    {
        caf.generator.CafGenerator g = new caf.generator.CafGenerator();
        return new CafGeneration(
                g.parseCAF("caf1"+fileName),
                g.parseCAF("caf2"+fileName)
        );
    }

    private Caf generateCaf(CafConfig cafConfig, Theory myTheory, Theory otherTheory)
    {
        Theory sharedTheory = theoryGeneration.getSharedTheory();
        Caf caf = new Caf();
        List<Argument> controlArguments = new ArrayList<>();
        controlArguments.addAll(myTheory.getControlArguments());
        controlArguments.removeAll(sharedTheory.getControlArguments());
        int nbControlArguments =
                new Double(
                        cafConfig.getRateOfControlArguments()*controlArguments.size()
                ).intValue();
        List<Argument> rest = controlArguments;
        controlArguments = new ArrayList<>();
        int random;
        for(int i = 0; i<nbControlArguments; i++)
        {
            random = r.nextInt(rest.size());
            controlArguments.add(rest.get(random));
            rest.remove(random);
        }

        List<Argument> fixedArguments = new ArrayList<>();
        fixedArguments.addAll(sharedTheory.getControlArguments());
        fixedArguments.addAll(sharedTheory.getEpistemicArguments());
        fixedArguments.addAll(sharedTheory.getPracticalArguments());

        rest = new ArrayList<>();
        rest.addAll(otherTheory.getEpistemicArguments());
        rest.addAll(otherTheory.getPracticalArguments());
        rest.addAll(otherTheory.getControlArguments());
        rest.removeAll(controlArguments);
        rest.removeAll(fixedArguments);
        

        int nbFixedArguments = new Double(
                rest.size()*cafConfig.getRateOfFixedArguments()

        ).intValue();

        for(int i = 0; i<nbFixedArguments; i++)
        {
            random = r.nextInt(rest.size());
            fixedArguments.add(rest.get(random));
            rest.remove(random);
        }
        
        fixedArguments.forEach(t-> caf.addFixedArgument(t.getName()));
        controlArguments.forEach(t-> caf.addControlArgument(t.getName()));
        rest.forEach(t->caf.addUncertainArgument(t.getName()));

        List<Attack> certainAttacks ;
        List<Attack> uncertainAttacks = new ArrayList<>();
        List<Attack> undirectedAttacks = new ArrayList<>();
        List<Attack> attacks = new ArrayList<>(otherTheory.getDungTheory().getAttacks());

        certainAttacks = attacks.stream().filter(t->
            fixedArguments.contains(t.getAttacker()) &&
                    fixedArguments.contains(t.getAttacked())
        ).collect(Collectors.toList());
        attacks.removeAll(certainAttacks);
        int nbCertainAttacks = new Double(
                cafConfig.getRateOfCertainAttacks() * attacks.size()
        ).intValue();

        int nbUncertainAttacks = new Double(
                cafConfig.getRateOfUncertainAttacks() * attacks.size()
        ).intValue();

        int nbUndirectedAttacks = new Double(
                cafConfig.getRateOfUndirectedAttacks() * attacks.size()
        ).intValue();

        for(int i = 0; i< nbCertainAttacks;i++)
        {
            random = r.nextInt(attacks.size());
            certainAttacks.add(attacks.get(random));
            attacks.remove(random);
        }

        for(int i = 0; i< nbUncertainAttacks;i++)
        {
            random = r.nextInt(attacks.size());
            uncertainAttacks.add(attacks.get(random));
            attacks.remove(random);
        }

        for(int i = 0; i< nbUndirectedAttacks;i++)
        {
            random = r.nextInt(attacks.size());
            undirectedAttacks.add(attacks.get(random));
            attacks.remove(random);
        }

        certainAttacks.forEach(t->{
            caf.addAttack(t.getAttacker().getName(), t.getAttacked().getName());
        });

        uncertainAttacks.forEach( t->{
            caf.addUncertainAttack(t.getAttacker().getName(), t.getAttacked().getName());
        });

        undirectedAttacks.forEach(t->{
            caf.addUndirectedAttack(t.getAttacker().getName(), t.getAttacked().getName());
        });


        List<caf.datastructure.Argument> otherArgumentsThanControl =
                new ArrayList<>(caf.getFixedArguments());
        otherArgumentsThanControl.addAll(caf.getUncertainArguments());
        //random generation of attacks from c to u and f
        for(Pair<String, String> attack: generateRandomAttacks(caf.getControlArguments(),
                otherArgumentsThanControl, cafConfig.getDensityOfControlAttacks()))
        {
            caf.addAttack(attack.getKey(), attack.getValue());
        }

        //adding existing attacks from c to f from my theory
        Set<Attack> atts = myTheory.getDungTheory().getAttacks().stream()
                .filter(attack ->
                    myTheory.getControlArguments().contains(attack.getAttacker())
                            && caf.hasArgument(attack.getAttacker().getName())
                            && caf.hasArgument(attack.getAttacked().getName())
                 )
                .collect(Collectors.toSet());

        for (Attack attack:atts) {
            caf.addAttack(attack.getAttacker().getName(), attack.getAttacked().getName());
        }





        return caf;
    }


    public void setSeed(long seed){
        r.setSeed(seed);
    }
    public TheoryGeneration getTheoryGeneration() {
        return theoryGeneration;
    }

    public void setTheoryGeneration(TheoryGeneration theoryGeneration) {
        this.theoryGeneration = theoryGeneration;
    }

    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public void setConfigFile(String configFile) throws Exception {

        generationConfig.loadConfigFromFile(configFile);

    }

    private List<Pair<String, String>> generateRandomAttacks(
            Collection<caf.datastructure.Argument> sources,
            Collection<caf.datastructure.Argument> targets,
            double rate)
    {
        List<Pair<String, String>> possibleAttacks = new ArrayList<>();
        for( caf.datastructure.Argument src: sources)
        {
            for(caf.datastructure.Argument trg: targets)
                if(!src.equals(trg))
                {
                    possibleAttacks.add(new Pair<>(src.getName(), trg.getName()));
                }
        }

        List<Pair<String, String>> randomAttacks = new ArrayList<>();
        int random;
        int nbAttacks = new Double(rate * possibleAttacks.size()).intValue();
        for(int i = 0; i < nbAttacks; i++)
        {
            random = r.nextInt(possibleAttacks.size());
            randomAttacks.add(possibleAttacks.get(random));
            possibleAttacks.remove(random);


        }
        return randomAttacks;
    }
}
