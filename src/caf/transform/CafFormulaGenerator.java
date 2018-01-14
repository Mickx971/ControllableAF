package caf.transform;

import caf.datastructure.Argument;
import caf.datastructure.Attack;
import caf.datastructure.Caf;
import caf.generator.CafGenerator;
import caf.transform.datastructure.*;
import javafx.util.Pair;

import net.sf.tweety.logics.pl.syntax.*;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Stream;


public class CafFormulaGenerator {

    private Caf caf;

    public CafFormula getStableSemanticFormula()
    {
        CafFormula cafFormula = new CafFormula();
        caf.getArguments().forEach(a1 -> caf.getArguments().forEach(a2 -> cafFormula.addAttFor(a1, a2)));
        caf.getUncertainAttacks().forEach(att -> cafFormula.setUAtt(att));
        caf.getControlArguments().forEach(a -> cafFormula.addOnAcFor(a));
        caf.getUncertainArguments().forEach(a -> cafFormula.addOnUFor(a));
        caf.getArguments().forEach(a -> cafFormula.addAccFor(a));

        Conjunction stableFormula = new Conjunction();
        Conjunction conjunction;

        for(Argument arg: caf.getArguments())
        {
            conjunction = new Conjunction();

            if(arg.getType() == Argument.Type.CONTROL)
            {
                conjunction.add(cafFormula.getOnAcFor(arg));
            }

            if(arg.getType() == Argument.Type.UNCERTAIN)
            {
                conjunction.add(cafFormula.getOnUFor(arg));
            }

            for(Argument attacker: caf.getArguments())
            {
                if(!arg.equals(attacker))
                {
                    conjunction.add((new Negation(cafFormula.getAttFor(attacker, arg)))
                            .combineWithOr(new Negation(cafFormula.getAccFor(attacker))));
                }
            }

            Proposition acc = cafFormula.getAccFor(arg);

            stableFormula.add((new Negation(acc)).combineWithOr(conjunction));
            stableFormula.add((new Negation(conjunction)).combineWithOr(acc));
        }

        caf.getCertainAttacks().forEach(
                a -> stableFormula.add(cafFormula.getAttFor(a.getSource(), a.getTarget()))
        );

        for(Attack attack : caf.getUndirectedAttacks())
        {
            Argument [] arguments = attack.getArguments();
            stableFormula.add(cafFormula.getAttFor(arguments[0], arguments[1])
                    .combineWithOr(cafFormula.getAttFor(arguments[1], arguments[0])));
        }

        Set<Pair<Argument, Argument>> attacks = new HashSet<>();

        Stream.concat(caf.getCertainAttacks().stream(), caf.getUncertainAttacks().stream())
                .forEach(t -> attacks.add(new Pair<Argument, Argument>(t.getSource(), t.getTarget())));

        caf.getUndirectedAttacks().stream().forEach(t -> {
            Argument[] arguments = t.getArguments();
            attacks.add(new Pair<>(arguments[0], arguments[1]));
            attacks.add(new Pair<>(arguments[1], arguments[0]));
        });

        caf.getArguments().forEach(t ->
            caf.getArguments().forEach(u -> {
                if(!attacks.contains(new Pair<>(t,u))) {
                    stableFormula.add(new Negation(cafFormula.getAttFor(t, u)));
                }
            })
        );

        cafFormula.setFormula(stableFormula);

        return cafFormula;
    }

    public CafFormula getSkepticalAcceptanceFormula(Collection<Argument> arguments)
    {
        CafFormula cafFormula = getStableSemanticFormula();
        Conjunction conjunction = new Conjunction();
        arguments.forEach(t-> conjunction.add(cafFormula.getAccFor(t)));
        cafFormula.setFormula(new Negation(cafFormula.getFormula()).combineWithOr(conjunction));
        return cafFormula;
    }

    public CafFormula getCredulousAcceptanceFormula(Collection<Argument> arguments)
    {
        CafFormula cafFormula = getStableSemanticFormula();
        Conjunction conjunction = new Conjunction();
        arguments.forEach(t-> conjunction.add(cafFormula.getAccFor(t)));
        cafFormula.setFormula(cafFormula.getFormula().combineWithAnd(conjunction));
        return cafFormula;
    }

    public CafFormula encodeCredulousFormulaForQBF(Collection<Argument> arguments)
    {
        CafFormula cafFormula = getCredulousAcceptanceFormula(arguments);
        PropositionalFormula formula = cafFormula.getFormula();
        Conjunction credulousAcceptance = new Conjunction();
        Collection<Attack> undirectedAttacks = caf.getUndirectedAttacks();

        if(undirectedAttacks.isEmpty())
            return TseitinTransformation.toCNF(cafFormula);

        for(Attack ua: undirectedAttacks)
        {
            Argument[] args = ua.getArguments();
            Proposition att1 = cafFormula.getAttFor(args[0], args[1]);
            Proposition att2 = cafFormula.getAttFor(args[1], args[0]);
            Disjunction d = new Disjunction();

            Conjunction temp = new Conjunction();
            temp.add(new Negation(att1));
            temp.add(att2);
            temp.add(formula);
            d.add(temp);

            temp = new Conjunction();
            temp.add(att1);
            temp.add(new Negation(att2));
            temp.add(formula);
            d.add(temp);

            temp = new Conjunction();
            temp.add(att1);
            temp.add(att2);
            temp.add(formula);
            d.add(temp);

            credulousAcceptance.add(d);
        }


        cafFormula.setFormula(credulousAcceptance);

        return TseitinTransformation.toCNF(cafFormula);

    }

    public PropositionalQuantifiedFormula encodeCredulousQBFWithControl(Collection<Argument> arguments)
    {
        return encodeCredulousQBF(arguments, true);
    }

    public PropositionalQuantifiedFormula encodeCredulousQBFWithoutControl(Collection<Argument> arguments)
    {
        return encodeCredulousQBF(arguments, false);
    }

    public PropositionalQuantifiedFormula encodeCredulousQBF(Collection<Argument> arguments, boolean withControl)
    {
        CafFormula cafFormula = encodeCredulousFormulaForQBF(arguments);

        PropositionalQuantifiedFormula credulousQbf = new PropositionalQuantifiedFormula();

        ExistQuantifiedPrefix onAcPrefix = new ExistQuantifiedPrefix();
        onAcPrefix.addAll(cafFormula.getAllOnAc());

        AllQuantifiedPrefix onUAndAttPrefix = new AllQuantifiedPrefix();
        onUAndAttPrefix.addAll(cafFormula.getAllOnU());
        onUAndAttPrefix.addAll(cafFormula.getAllUAtt());

        QuantifiedPrefix accPrefix = new ExistQuantifiedPrefix();
        accPrefix.addAll(cafFormula.getAllAcc());

        if(withControl) {
            credulousQbf.addQuantifiedPrefix(onAcPrefix);
        }

        credulousQbf.addQuantifiedPrefix(onUAndAttPrefix);
        credulousQbf.addQuantifiedPrefix(accPrefix);
        credulousQbf.setCafFormula(cafFormula);

        return credulousQbf;
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
            caf = g.parseCAF("/media/ider/disque local/workSpace" +
                    "/java/ControllableAF/caf1test.caf");

            CafFormulaGenerator formulaGenerator = new CafFormulaGenerator();
            formulaGenerator.setCaf(caf);

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("/media/ider/disque local/workSpace/java/ControllableAF/caf2017.txt"), "utf-8"))) {
                writer.write(formulaGenerator.encodeCredulousFormulaForQBF(
                        formulaGenerator.getCaf().getFixedArguments()
                ).getFormula().toString());
            }
            System.out.println();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
