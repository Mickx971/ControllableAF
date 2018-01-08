package solver;

import caf.datastructure.Argument;
import caf.datastructure.Caf;
import caf.transform.CafFormulaGenerator;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

import java.util.Arrays;
import java.util.List;

public class QuantomConnector {
    public static boolean isCredulouslyAccepted(Caf tempCaf, String argName) {
        CafFormulaGenerator formulaGen = new CafFormulaGenerator();
        formulaGen.setCaf(tempCaf);
        Argument theta = tempCaf.getArgument(argName);
        PropositionalFormula formula = formulaGen.getCredulousAcceptanceFormula(Arrays.asList(new Argument[]{theta}));
        //TODO
        return false;
    }

    public static boolean isCredulouslyAccepted(Caf tempCaf, List<String> argNames) {
        //TODO
        return false;
    }
}
