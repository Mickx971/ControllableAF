package caf.transform;

import net.sf.tweety.commons.Formula;
import net.sf.tweety.logics.pl.syntax.*;
import org.apache.commons.lang.mutable.MutableInt;

import java.util.concurrent.ArrayBlockingQueue;


public class TseitinTransformation {

    /*
    * the transformation is more efficient whene a disjunction is not composed
    * from disjunctions and conjunction is not composed from conjunctions
    * works but not efficient for a.combineWithOr(b.combineWithOr(c))
    * */
    public static PropositionalFormula toCNF(PropositionalFormula formula)
    {
        return __toCNF(formula.collapseAssociativeFormulas(), new MutableInt(0));
    }

    private static PropositionalFormula __toCNF(PropositionalFormula formula,MutableInt count)
    {
        if(formula.isLiteral())
            return formula;

        Conjunction tseitinConjunction = new Conjunction();

        if(formula instanceof Disjunction)
        {
            Disjunction disjunction = (Disjunction) formula;
            ;
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
                count.add(1);
                additionalVariables.add(z);

                condition = new Disjunction();
                condition.add(new Negation(z));
                condition.add(__toCNF(f, count));

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
                tseitinConjunction.add(__toCNF(f, count));
            }

            return tseitinConjunction;

        }

        if (formula instanceof Negation)
        {
            Negation negation = (Negation) formula;
            PropositionalFormula formulaWithNegationOnLiterals = negation.toNnf();
            return __toCNF(formulaWithNegationOnLiterals, count);
        }


        return formula;
    }

    public static void main(String[] args) {
        Proposition a = new Proposition("a");
        Proposition b = new Proposition("b");
        Proposition c = new Proposition("c");
        Proposition d = new Proposition("d");
        Proposition e = new Proposition("e");
        Disjunction f = new Proposition("a").combineWithOr(new Proposition("a"));



        System.out.println(f.getModels());

        //System.out.println(g.get);
        //convertToCNF(e);

    }


}