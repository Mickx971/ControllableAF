package theory.generator;

import caf.datastructure.Caf;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.syntax.Attack;
import net.sf.tweety.commons.util.Pair;
import theory.datastructure.CafGeneration;
import theory.datastructure.Offer;
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


        List<Argument> temp = controlArguments;
        controlArguments = new ArrayList<>();

        int random;
        for(int i = 0; i<nbControlArguments; i++)
        {
            random = r.nextInt(temp.size());
            controlArguments.add(temp.get(random));
            temp.remove(random);
        }

        //adding all the shared theory to the fixed part
        temp = new ArrayList<>();
        temp.addAll(sharedTheory.getControlArguments());
        temp.addAll(sharedTheory.getEpistemicArguments());
        temp.addAll(otherTheory.getPracticalArguments());
        temp.addAll(myTheory.getPracticalArguments());

        List<Argument> fixedArguments = new HashSet<>(temp).stream().collect(Collectors.toList());


        temp = new ArrayList<>();
        temp.addAll(otherTheory.getEpistemicArguments());
        temp.addAll(otherTheory.getControlArguments());
        temp.removeAll(controlArguments);
        temp.removeAll(fixedArguments);
        

        int nbFixedArguments = new Double(
                temp.size()*cafConfig.getRateOfFixedArguments()

        ).intValue();


        int nbUncertainArguments = new Double(
                temp.size()*cafConfig.getRateOfUncertainArguments()

        ).intValue();

        for(int i = 0; i<nbFixedArguments; i++)
        {
            random = r.nextInt(temp.size());
            fixedArguments.add(temp.get(random));
            temp.remove(random);
        }

        List<Argument> uncertainArguments = new ArrayList<>();
        for(int i = 0; i < nbUncertainArguments; i++){
            random = r.nextInt(temp.size());
            uncertainArguments.add(temp.get(random));
            temp.remove(random);
        }



        
        fixedArguments.forEach(arg-> caf.addFixedArgument(arg.getName()));
        controlArguments.forEach(arg-> caf.addControlArgument(arg.getName()));
        uncertainArguments.forEach(arg->caf.addUncertainArgument(arg.getName()));
        List<Attack> certainAttacks ;
        List<Attack> uncertainAttacks = new ArrayList<>();
        List<Attack> undirectedAttacks = new ArrayList<>();

        List<Attack> attacks = new ArrayList<>(otherTheory.getDungTheory().getAttacks());

        List<Attack> attacksToDelete = new ArrayList<>();
        for(Attack att: attacks){
            if(temp.contains(att.getAttacker()) || temp.contains(att.getAttacked()))
            {
                attacksToDelete.add(att);
            }
        }

        attacks.removeAll(attacksToDelete);


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
            caf.addAttack(attack.getFirst(), attack.getSecond());
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


        //adding offers
        for(Map.Entry<Offer, Set<String>> offerSupporters: otherTheory.getOffers().entrySet()){
            caf.addOfferSupporters(offerSupporters.getKey().getName(), offerSupporters.getValue());
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
