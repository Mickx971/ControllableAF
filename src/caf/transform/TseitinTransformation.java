package caf.transform;

import caf.transform.datastructure.CafFormula;
import net.sf.tweety.logics.pl.parser.PlParser;
import net.sf.tweety.logics.pl.syntax.*;
import org.apache.commons.lang.mutable.MutableInt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;


public class TseitinTransformation {

    public static CafFormula toCNF(CafFormula cafFormula) {

        Set<Proposition> newVariables = new HashSet<>();
        System.out.println(__toCNF(cafFormula.getFormula().collapseAssociativeFormulas(), new MutableInt(0), newVariables));
        cafFormula.setFormula(
               __toCNF(cafFormula.getFormula().collapseAssociativeFormulas(), new MutableInt(0), newVariables)
                       .collapseAssociativeFormulas()
                //cafFormula.getFormula().toCnf()
        );

        cafFormula.addFakeVariables(newVariables);
        return cafFormula;
    }

    public static PropositionalFormula toCNF(PropositionalFormula formula)
    {
        return __toCNF(formula.collapseAssociativeFormulas(), new MutableInt(0), new HashSet<>()).collapseAssociativeFormulas();
    }

    private static PropositionalFormula __toCNF(PropositionalFormula formula,MutableInt count, Set<Proposition> newVariables)
    {
        if(formula.isLiteral())
            return formula;

        Conjunction tseitinConjunction = new Conjunction();

        if(formula instanceof Disjunction)
        {
            Disjunction disjunction = (Disjunction) formula;
            Disjunction additionalVariables = new Disjunction();
            Proposition z;
            Disjunction condition;
            for(PropositionalFormula f : disjunction)
            {
                if(f.isLiteral())
                {
                    additionalVariables.add(f);
                    continue;
                }

                z = new Proposition("z" + count.toString());
                newVariables.add(z);

                count.add(1);
                additionalVariables.add(z);

                condition = new Disjunction();
                condition.add(new Negation(z));
                condition.add(__toCNF(f, count, newVariables));

                tseitinConjunction.addAll(condition.toCnf());


            }
            tseitinConjunction.add(additionalVariables);
            return tseitinConjunction;

        }

        if(formula instanceof Conjunction)
        {
            Conjunction conjunction = (Conjunction) formula;

            for(PropositionalFormula f : conjunction)
            {
                tseitinConjunction.add(__toCNF(f, count, newVariables));
            }

            return tseitinConjunction;

        }

        if (formula instanceof Negation)
        {
            Negation negation = (Negation) formula;
            PropositionalFormula formulaWithNegationOnLiterals = negation.toNnf();
            return __toCNF(formulaWithNegationOnLiterals, count, newVariables);
        }


        return formula;
    }

    public static void main(String[] args) {
        Proposition a = new Proposition("a");
        Proposition b = new Proposition("b");
        Proposition c = new Proposition("c");
        Proposition d = new Proposition("d");
        Proposition e = new Proposition("e");
        PropositionalFormula f = a.combineWithOr(b.combineWithAnd(c));
        try {
            PlParser pl = new PlParser();
            f = pl.parseFormula(new BufferedReader(new FileReader("temp.formula")));
            System.out.println(TseitinTransformation.toCNF(f));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //System.out.println(g.get);
        //convertToCNF(e);

    }


}