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

    public Conjunction createStableSemanticFormula(CafFormula cafFormula, Attack udAtt)
    {
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
                            .combineWithOr(new Negation(cafFormula.getAccFor(attacker, udAtt))));
                }
            }

            Proposition acc = cafFormula.getAccFor(arg, udAtt);

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

        return stableFormula;
    }

    public CafFormula createCafFormula() {
        CafFormula cafFormula = new CafFormula();
        caf.getArguments().forEach(a1 -> caf.getArguments().forEach(a2 -> cafFormula.addAttFor(a1, a2)));
        caf.getUncertainAttacks().forEach(att -> cafFormula.setUAtt(att));
        caf.getControlArguments().forEach(a -> cafFormula.addOnAcFor(a));
        caf.getUncertainArguments().forEach(a -> cafFormula.addOnUFor(a));

        if(caf.getUndirectedAttacks().isEmpty()) {
            caf.getArguments().forEach(arg -> cafFormula.addAccFor(arg, null));
        }
        else {
            caf.getUndirectedAttacks().forEach(
                    ua -> caf.getArguments().forEach(arg -> cafFormula.addAccFor(arg, ua))
            );
        }

        return cafFormula;
    }

    public CafFormula addSkepticalAcceptanceFormula(CafFormula cafFormula, Attack ua, Collection<Argument> arguments)
    {
        Conjunction formula = createStableSemanticFormula(cafFormula, ua);
        arguments.forEach(t-> formula.add(cafFormula.getAccFor(t, ua)));
        if(cafFormula.getFormula() == null)
            cafFormula.setFormula(formula);
        else
            cafFormula.setFormula(new Negation(cafFormula.getFormula()).combineWithOr(formula));
        return cafFormula;
    }

    public CafFormula addCredulousAcceptanceFormula(CafFormula cafFormula, Attack ua, Collection<Argument> arguments)
    {
        Conjunction formula = createStableSemanticFormula(cafFormula, ua);
        arguments.forEach(t-> formula.add(cafFormula.getAccFor(t, ua)));
        if(cafFormula.getFormula() == null)
            cafFormula.setFormula(formula);
        else
            cafFormula.setFormula(cafFormula.getFormula().combineWithAnd(formula));
        return cafFormula;
    }

    public CafFormula encodeCredulousFormulaForQBF(Collection<Argument> arguments)
    {
        CafFormula cafFormula = createCafFormula();
        if(caf.getUndirectedAttacks().isEmpty()) {
            addCredulousAcceptanceFormula(cafFormula, null, arguments);
        }
        else {
            for (Attack ua : caf.getUndirectedAttacks()) {
                addCredulousAcceptanceFormula(cafFormula, ua, arguments);
            }
        }
        System.out.println(cafFormula.getFormula());
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
        Collection<Proposition> all = cafFormula.getAllVariables();
        all.removeAll(cafFormula.getAllOnAc());
        all.removeAll(cafFormula.getAllOnU());
        all.removeAll(cafFormula.getAllUAtt());
        accPrefix.addAll(all);

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
            caf = g.parseCAF("caf2test.caf");

            CafFormulaGenerator formulaGenerator = new CafFormulaGenerator();
            formulaGenerator.setCaf(caf);

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("caf2017.txt"), "utf-8"))) {
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
