package caf.transform.datastructure;

import caf.datastructure.Argument;
import net.sf.tweety.logics.pl.syntax.Negation;
import net.sf.tweety.logics.pl.syntax.Proposition;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

import java.util.*;

public class PropositionalQuantifiedFormula {

    private List<QuantifiedPrefix> quantifiedPrefixes;
    private CafFormula cafFormula;

    public PropositionalQuantifiedFormula() {
        quantifiedPrefixes = new ArrayList<>();
    }

    public int getDegree() {
        return quantifiedPrefixes.size();
    }

    public PropositionalFormula getPropositionalFormula() {
        return cafFormula.getFormula();
    }

    public CafFormula getCafFormula() {
        return cafFormula;
    }

    public QuantifiedPrefix getQuantifiedPrefix(int degree) {
        return quantifiedPrefixes.get(degree);
    }

    public void addQuantifiedPrefix(QuantifiedPrefix prefix) {
        quantifiedPrefixes.add(prefix);
    }

    public String getIdentifier(Proposition var) {
        return Integer.toBinaryString(cafFormula.getIdentifier(var));
    }

    public String getIdentifier(Negation var) {
        Proposition prop = var.getAtoms().iterator().next();
        return Integer.toBinaryString(cafFormula.getIdentifier(prop));
    }

    public void setCafFormula(CafFormula cafFormula) {
        this.cafFormula = cafFormula;
    }

    public int getNbVariables() {
        return cafFormula.getNbVariables();
    }

    public Argument getArgumentFor(String s) {
        return cafFormula.getArgumentFor(Integer.parseInt(s,2));
    }
}
