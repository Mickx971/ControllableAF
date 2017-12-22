package caf.transform;

import caf.datastructure.Argument;
import caf.datastructure.Attack;
import caf.datastructure.Caf;
import caf.generator.CafGenerator;
import javafx.util.Pair;
import net.sf.tweety.logics.pl.syntax.*;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Stream;


public class CafFormulaGenerator {
    public final static String ACC_PROPOSITION = "ACC";
    public final static String ATTACK_PROPOSITION = "ATT";
    public final static String ON_PROPOSITION = "ON";


    private Caf caf;

    public  PropositionalFormula getStableSemanticFormula()
    {
        Conjunction stableFormula = new Conjunction();
        Conjunction conjunction;

        for(Argument arg: caf.getArguments())
        {
            conjunction = new Conjunction();

            if(arg.getType().equals(Argument.Type.CONTROL)||
                    arg.getType().equals(Argument.Type.UNCERTAIN))
            {
                conjunction.add(new Proposition(ON_PROPOSITION + arg.getName()));
            }

            for(Argument attacker: caf.getArguments())
            {
                if(!arg.equals(attacker))
                {
                    conjunction.add((new Negation(new Proposition(ATTACK_PROPOSITION +
                            attacker.getName() + arg.getName())))
                            .combineWithOr(new Negation(new Proposition(ACC_PROPOSITION +
                                    attacker.getName()))));
                }
            }

            Proposition acc = new Proposition(ACC_PROPOSITION + arg.getName());

            stableFormula.add((new Negation(acc)).combineWithOr(conjunction));
            stableFormula.add((new Negation(conjunction)).combineWithOr(acc));
        }

        for(Attack attack : caf.getCertainAttacks())
        {
            try {
                stableFormula.add(new Proposition(ATTACK_PROPOSITION +
                        attack.getSource().getName() + attack.getTarget().getName()));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        for(Attack attack : caf.getUndirectedAttacks())
        {
            Argument [] arguments = attack.getArguments();
            stableFormula.add(new Proposition(ATTACK_PROPOSITION +
                    arguments[0].getName() + arguments[1].getName())
                    .combineWithOr(new Proposition(ATTACK_PROPOSITION +
                    arguments[1].getName() + arguments[0].getName()))
            );

        }

        Set<Pair<Argument, Argument>> attacks = new HashSet<>();

        Stream.concat(caf.getCertainAttacks().stream(), caf.getUncertainAttacks().stream())
                .forEach(t ->{
                    try {
                        attacks.add(new Pair<Argument, Argument>(t.getSource(), t.getTarget()));
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                });

        caf.getUndirectedAttacks().stream().forEach(t->{
            Argument[] arguments = t.getArguments();
            attacks.add(new Pair<>(arguments[0], arguments[1]));
            attacks.add(new Pair<>(arguments[1], arguments[0]));
        });

        caf.getArguments().forEach(t ->{
            caf.getArguments().forEach(u->{
                if(!attacks.contains(new Pair<>(t,u)))
                {
                    stableFormula.add(new Negation(new Proposition(ATTACK_PROPOSITION +
                    t.getName() + u.getName())));
                }
            });
        });

        return stableFormula.collapseAssociativeFormulas();
    }

    public PropositionalFormula getSkepticalAcceptanceFormula(Collection<Argument> arguments)
    {
        Conjunction conjunction = new Conjunction();
        arguments.forEach(t->{
            conjunction.add(new Proposition(ACC_PROPOSITION+t.getName()));
        });
        return new Negation(getStableSemanticFormula()).combineWithOr(conjunction);
    }

    public PropositionalFormula getCredulousAcceptanceFormula(Collection<Argument> arguments)
    {
        Conjunction conjunction = new Conjunction();
        arguments.forEach(t->{
            conjunction.add(new Proposition(ACC_PROPOSITION+t.getName()));
        });
        return getStableSemanticFormula().combineWithAnd(conjunction);
    }
    public Caf getCaf() {
        return caf;
    }

    public void setCaf(Caf caf) {
        this.caf = caf;
    }

    public static void main(String[] args) {
        CafGenerator g = new CafGenerator();
        Caf caf;
        try {
            caf = g.parseCAF("/media/ider/disque" +
                    " local/workSpace/java/ControllableAF/caf2017");

            CafFormulaGenerator formulaGenerator = new CafFormulaGenerator();
            formulaGenerator.setCaf(caf);

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("/media/ider/disque local/workSpace/java/ControllableAF/caf2017.txt"), "utf-8"))) {
                writer.write(TseitinTransformation.toCNF(formulaGenerator.getStableSemanticFormula()).toString());
            }
            System.out.println();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
