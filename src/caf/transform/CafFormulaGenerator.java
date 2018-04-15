package caf.transform;

import caf.datastructure.Argument;
import caf.datastructure.Attack;
import caf.datastructure.Caf;
import caf.generator.CafGenerator;
import caf.transform.datastructure.*;

import net.sf.tweety.commons.util.Pair;
import net.sf.tweety.logics.pl.syntax.*;
import scala.collection.parallel.ParIterableLike;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Stream;


public class CafFormulaGenerator {

    private Caf caf;

    public Conjunction createStableSemanticFormula(CafFormula cafFormula)
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

        return stableFormula;
    }

    public CafFormula createCafFormula(boolean withControl) {
        CafFormula cafFormula = new CafFormula();
        caf.getArguments().forEach(a1 -> caf.getArguments().forEach(a2 -> cafFormula.addAttFor(a1, a2)));

        caf.getUncertainAttacks().forEach(att -> cafFormula.setUAtt(att));
        caf.getUndirectedAttacks().forEach(att -> cafFormula.setUdAtt(att));
        caf.getUncertainArguments().forEach(a -> cafFormula.addOnUFor(a));
        caf.getArguments().forEach(arg -> cafFormula.addAccFor(arg));
        if(withControl) {
            caf.getControlArguments().forEach(a -> cafFormula.addOnAcFor(a));
        }

        createFormula(cafFormula, caf.getUndirectedAttacks());
        return cafFormula;
    }



    public CafFormula encodeCredulousFormulaForQBF(Collection<Argument> arguments, boolean withControl)
    {
        CafFormula cafFormula = createCafFormula(withControl);
        addAccArgumentsToFormula(cafFormula, arguments);
        System.out.println(cafFormula.getFormula());
        //return TseitinTransformation.toCNF(cafFormula);
        cafFormula.setFormula(cafFormula.getFormula().toCnf());
        return cafFormula;

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
        CafFormula cafFormula = encodeCredulousFormulaForQBF(arguments, withControl);

        PropositionalQuantifiedFormula credulousQbf = new PropositionalQuantifiedFormula();

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
            ExistQuantifiedPrefix onAcPrefix = new ExistQuantifiedPrefix();
            onAcPrefix.addAll(cafFormula.getAllOnAc());
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
                        formulaGenerator.getCaf().getFixedArguments(), false
                ).getFormula().toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
//    public CafFormula addCredulousAcceptanceFormula(CafFormula cafFormula, Attack ua, Collection<Argument> arguments)
//    {
//
//
//        if(ua != null) {
//
//            Argument[] args = ua.getArguments();
//            Proposition att01 = cafFormula.getAttFor(args[0], args[1]);
//            Proposition att10 = cafFormula.getAttFor(args[1], args[0]);
//
//            Conjunction d = new Conjunction();
//
//            if(cafFormula.getFormula() == null) {
//                Conjunction temp = new Conjunction();
//                temp.add(d);
//                cafFormula.setFormula(temp);
//            }
//            else {
//                cafFormula.setFormula(cafFormula.getFormula().combineWithAnd(d).collapseAssociativeFormulas());
//            }
//
//        }
//        else {
//            Conjunction formula = createStableSemanticFormula(cafFormula);
//            cafFormula.setFormula(formula.collapseAssociativeFormulas());
//        }
//        System.out.println(formula);
//        System.out.println(cafFormula.getFormula());
//
//        return cafFormula;
//    }

    private void addAccArgumentsToFormula(CafFormula cafFormula,Collection<Argument> arguments)
    {
        PropositionalFormula formula = cafFormula.getFormula();
        for (Argument arg: arguments)
        {
            formula = formula.combineWithAnd(cafFormula.getAccFor(arg));
        }

        cafFormula.setFormula(formula);

    }
    private void createFormula(CafFormula cafFormula, Collection<Attack> undirectedAttacks)
    {
        Conjunction stableSemanticFormula = createStableSemanticFormula(cafFormula);
        Disjunction formula = new Disjunction();
        formula.add(stableSemanticFormula);
        if(undirectedAttacks == null)
            return;

        for(Attack ua: undirectedAttacks)
        {
            Argument[] args = ua.getArguments();
            Proposition att01 = cafFormula.getAttFor(args[0], args[1]);
            Proposition att10 = cafFormula.getAttFor(args[1], args[0]);

            Conjunction temp = (new Negation(att01)).combineWithAnd(new Negation(att10));
            formula.add(temp);
        }

        cafFormula.setFormula(formula.collapseAssociativeFormulas());

    }
}
