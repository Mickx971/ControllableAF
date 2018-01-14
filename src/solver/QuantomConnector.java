package solver;

import caf.datastructure.Argument;
import caf.datastructure.Attack;
import caf.datastructure.Caf;
import caf.generator.CafGenerator;
import caf.transform.CafFormulaGenerator;
import caf.transform.datastructure.PropositionalQuantifiedFormula;
import caf.transform.datastructure.QuantifiedPrefix;

import net.sf.tweety.arg.dung.DungTheory;
import net.sf.tweety.arg.dung.StableReasoner;
import net.sf.tweety.arg.dung.semantics.Extension;
import net.sf.tweety.logics.pl.syntax.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class QuantomConnector {

    private final Path path;
    private String cnfFileName;

    public QuantomConnector() {
        this.path = Paths.get(System.getProperty("user.dir"));
    }

    public void setAgentName(String agentName) {
        cnfFileName = agentName + "caf.cnf";
    }

    public Collection<Argument> isCredulouslyAcceptedWithControl(Caf tempCaf, String argName) throws Exception {
        CafFormulaGenerator formulaGen = new CafFormulaGenerator();
        formulaGen.setCaf(tempCaf);
        Argument theta = tempCaf.getArgument(argName);

        if(theta == null)
            throw new Exception("Unknown argument in caf named: " + argName);

        PropositionalQuantifiedFormula qbfFormula = formulaGen.encodeCredulousQBFWithControl(Arrays.asList(new Argument[]{theta}));

        PropositionalFormula formula = qbfFormula.getCafFormula().getFormula();
        if(formula instanceof Tautology) {
            return computeSimpleExtension(tempCaf, argName);
        }
        if(formula instanceof Contradiction) {
            return new ArrayList<>();
        }

        return computePotentSet(qbfFormula);
    }

    public boolean isCredulouslyAcceptedWithoutControl(Caf tempCaf, String argName) throws Exception {
        CafFormulaGenerator formulaGen = new CafFormulaGenerator();
        formulaGen.setCaf(tempCaf);

        Argument theta = tempCaf.getArgument(argName);
        if(theta == null)
            throw new Exception("Unknown argument in caf named: " + argName);

        PropositionalQuantifiedFormula qbfFormula = formulaGen.encodeCredulousQBFWithoutControl(Arrays.asList(new Argument[]{theta}));

        PropositionalFormula formula = qbfFormula.getCafFormula().getFormula();
        if(formula instanceof Tautology) {
            return true;
        }
        if(formula instanceof Contradiction) {
            return false;
        }

        return !computePotentSet(qbfFormula).isEmpty();
    }

    private Collection<Argument> computePotentSet(PropositionalQuantifiedFormula qbfFormula) throws Exception {
        createCNFFile(qbfFormula, path.resolve(cnfFileName));
        Process p = Runtime.getRuntime().exec("depqbf --qdo --no-dynamic-nenofex " + path.resolve(cnfFileName));
        return readResult(p, qbfFormula);
    }

    private Collection<Argument> computeSimpleExtension(Caf tempCaf, String argName) throws Exception {
        DungTheory theory = new DungTheory();
        for(Argument arg: tempCaf.getArguments()) {
            theory.add(new net.sf.tweety.arg.dung.syntax.Argument(arg.getName()));
        }
        for(Attack att: tempCaf.getAttacks()) {
            Argument[] args = att.getArguments();
            theory.add(new net.sf.tweety.arg.dung.syntax.Attack(
                    new net.sf.tweety.arg.dung.syntax.Argument(args[0].getName()),
                    new net.sf.tweety.arg.dung.syntax.Argument(args[1].getName())
            ));
        }
        StableReasoner stableReasoner = new StableReasoner(theory);
        Optional<Extension> ext = stableReasoner.getExtensions().stream()
                .filter(e -> e.contains(new net.sf.tweety.arg.dung.syntax.Argument(argName))).findFirst();
        if(ext.isPresent()) {
            return ext.get().stream().map(a -> new Argument(a.getName(), Argument.Type.FIXE)).collect(Collectors.toSet());
        }
        throw new Exception("Tautology found but not extension found");
    }

    private Collection<Argument> readResult(Process p, PropositionalQuantifiedFormula qbfFormula) throws Exception {

        List<Integer> solutionIds = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line = buffer.readLine();
            if(line == null)
                throw new Exception("error, Unexpected output from QBF Solver");
            if(line.split(" ")[2].equals("0"))
                return new ArrayList<>();

            while ((line = buffer.readLine()) != null) {

                line = line.trim();
                if(line.isEmpty())
                    continue;

                Integer argId = Integer.parseInt(line.split(" ")[1]);
                if(argId > 0)
                    solutionIds.add(argId);
            }
        }

        return qbfFormula.getArgumentsFor(solutionIds);
    }

    public void createCNFFile(PropositionalQuantifiedFormula qbfFormula, Path path) {

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {

            Conjunction formula = (Conjunction) qbfFormula.getPropositionalFormula();
            writer.println("p cnf " + qbfFormula.getNbVariables() + " " + formula.size());

            //Quantificateurs
            for(int i = 0; i < qbfFormula.getDegree(); i++) {
                QuantifiedPrefix prefix = qbfFormula.getQuantifiedPrefix(i);

                if(prefix.getQuantifiedProposition().isEmpty())
                    continue;

                if(prefix.getType() == QuantifiedPrefix.QuantifiedPrefixType.EXIST) {
                    writer.print("e");
                }
                else {
                    writer.print("a");
                }
                for(Proposition p : prefix.getQuantifiedProposition()) {
                    writer.printf(" " + qbfFormula.getIdentifier(p));
                }

                writer.println(" 0");
            }

            //Clauses
            for(PropositionalFormula clause : formula) {
                for(PropositionalFormula var : clause.getLiterals()) {
                    if(var instanceof Proposition) {
                        writer.print(qbfFormula.getIdentifier((Proposition) var) + " ");
                    }
                    else {
                        writer.print("-" + qbfFormula.getIdentifier((Negation) var) + " ");
                    }
                }
                writer.println("0");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CafGenerator g = new CafGenerator();
        Caf caf;
        try {
            caf = g.parseCAF("caf2test.caf");

            QuantomConnector qConnector = new QuantomConnector();
            qConnector.setAgentName("Agent1");

            Collection<Argument> result = qConnector.isCredulouslyAcceptedWithControl(caf, "SE1");
            System.out.println(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
