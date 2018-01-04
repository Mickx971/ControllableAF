package theory.generator;

import caf.datastructure.Caf;
import net.sf.tweety.arg.dung.semantics.ArgumentStatus;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.syntax.Attack;
import theory.datastructure.CafGeneration;
import theory.datastructure.Theory;
import theory.generator.config.CafConfig;
import theory.generator.config.GenerationConfig;
import theory.datastructure.TheoryGeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CafGenerator {
    TheoryGeneration theoryGeneration;
    GenerationConfig generationConfig;

    public CafGeneration generate()
    {
        return new CafGeneration(
                generateCaf(
                        generationConfig.getCaf1(), theoryGeneration.getT1(), theoryGeneration.getT2()
                ),generateCaf(
                generationConfig.getCaf2(), theoryGeneration.getT2(), theoryGeneration.getT1()
        )
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
        Random r = new Random();
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


        return caf;
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

    public void setGenerationConfig(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }
}
