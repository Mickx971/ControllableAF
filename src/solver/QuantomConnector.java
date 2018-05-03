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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class QuantomConnector {

    private final Path path;
    private String cnfFileName = "tempcaf.cnf";
    private String[] solverCommand;

    public QuantomConnector() {
        this.path = Paths.get(System.getProperty("user.dir"));
        solverCommand = new String[2];
        solverCommand[0] = "./quantom --solvemode=0 ";
        solverCommand[1] = "./quantom --solvemode=1 ";
    }

    public void setAgentName(String agentName) {
        cnfFileName = agentName + "Caf.cnf";
    }

    public Collection<Argument> isCredulouslyAcceptedWithControl(Caf tempCaf, String argName, Set<Set<Argument>> potentSetsUsed) throws Exception {

        int useMaxQBF = potentSetsUsed == null ? 0 : 1;

        CafFormulaGenerator formulaGen = new CafFormulaGenerator();
        formulaGen.setCaf(tempCaf);
        Argument theta = tempCaf.getArgument(argName);

        if(theta == null)
            throw new Exception("Unknown argument in Caf named: " + argName);

        PropositionalQuantifiedFormula qbfFormula = formulaGen.encodeCredulousQBFWithControl(Arrays.asList(new Argument[]{theta}), potentSetsUsed);

        PropositionalFormula formula = qbfFormula.getCafFormula().getFormula();
        if(formula instanceof Tautology) {
            return computeSimpleExtension(tempCaf, argName);
        }
        if(formula instanceof Contradiction) {
            return new ArrayList<>();
        }

        HashSet<Argument> res = new HashSet<>();
        computePotentSet(qbfFormula, res, useMaxQBF);
        return res;
    }

    public boolean isCredulouslyAcceptedWithoutControl(Caf tempCaf, String argName) throws Exception {
        CafFormulaGenerator formulaGen = new CafFormulaGenerator();
        formulaGen.setCaf(tempCaf);
        Argument theta = tempCaf.getArgument(argName);
        if(theta == null)
            throw new Exception("Unknown argument in Caf named: " + argName);

        PropositionalQuantifiedFormula qbfFormula = formulaGen.encodeCredulousQBFWithoutControl(Arrays.asList(new Argument[]{theta}));
        PropositionalFormula formula = qbfFormula.getCafFormula().getFormula();
        if(formula instanceof Tautology) {
            return true;
        }
        if(formula instanceof Contradiction) {
            return false;
        }

        return computePotentSet(qbfFormula, new HashSet<>(), 0);
    }

    private boolean computePotentSet(PropositionalQuantifiedFormula qbfFormula, Set<Argument> res, int mode) throws Exception {
        createCNFFile(qbfFormula, path.resolve(cnfFileName));
        Process p = Runtime.getRuntime().exec(solverCommand[mode] + cnfFileName);
        return readResult(p, qbfFormula, res);
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

    private boolean readResult(Process p, PropositionalQuantifiedFormula qbfFormula, Collection<Argument> solutions) throws Exception {

        List<Integer> solutionIds = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line = null;

            while ((line = buffer.readLine()) != null) {

                //System.out.println(line);

                if(line.startsWith("s"))
                {
                    //System.out.println();
                    String res[] = line.split(" ");

                    if(res[1].equals("UNSATISFIABLE") || res[1].equals("UNKNOWN")) {
                        return false;
                    }

                    if(res[1].equals("SATISFIABLE"))
                    {
                        line = buffer.readLine();
                        if(line != null) {
                            res = line.split(" ");
                            for (int i = 1; i < res.length; i++)
                                solutionIds.add(Integer.parseInt(res[i]));

                            solutions.addAll(qbfFormula.getArgumentsFor(solutionIds));
                        }
                        return true;
                    }

                }

            }
        }

        return true;
    }

    public void createCNFFile(PropositionalQuantifiedFormula qbfFormula, Path path) {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path.toFile()), "us-ascii"))) {

            Conjunction formula = (Conjunction) qbfFormula.getPropositionalFormula();
            writer.write("p cnf " + qbfFormula.getNbVariables() + " " + formula.size() + "\n");

            //Quantificateurs
            for(int i = 0; i < qbfFormula.getDegree(); i++) {
                QuantifiedPrefix prefix = qbfFormula.getQuantifiedPrefix(i);

                if(prefix.getQuantifiedProposition().isEmpty())
                    continue;

                if(prefix.getType() == QuantifiedPrefix.QuantifiedPrefixType.EXIST) {
                    writer.write("e");
                }
                else {
                    writer.write("a");
                }
                for(Proposition p : prefix.getQuantifiedProposition()) {
                    writer.write(" " + qbfFormula.getIdentifier(p));
                }

                writer.write(" 0\n");
            }

            //Clauses
            for(int i = 0; i < formula.size(); i++) {
                for(PropositionalFormula var : formula.get(i).getLiterals()) {
                    if(var instanceof Proposition) {
                        writer.write(qbfFormula.getIdentifier((Proposition) var) + " ");
                    }
                    else {
                        writer.write("-" + qbfFormula.getIdentifier((Negation) var) + " ");
                    }
                }
                writer.write("0\n");
            }

            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CafGenerator g = new CafGenerator();
        Caf caf;
        try {
            caf = g.parseCAF("tempcaf.caf");

            System.out.println(caf.argumentIsCredulouslyAcceptedWithoutControl("SP0"));
            QuantomConnector qConnector = new QuantomConnector();
            qConnector.setAgentName("temp");


            Collection<Argument> result = qConnector.isCredulouslyAcceptedWithControl(caf, "SP0", new HashSet<>());
            System.out.println(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
