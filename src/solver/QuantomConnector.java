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
    private String cnfFileName = "caf.cnf";

    public QuantomConnector() {
        this.path = Paths.get(System.getProperty("user.dir"));
    }
    // tester cette fonction quen retoune les arguments a mettre on pour que ca soit credulement
    //accepté == compute_psa
    public Collection<Argument> isCredulouslyAcceptedWithControl(Caf tempCaf, String argName) throws Exception {
        CafFormulaGenerator formulaGen = new CafFormulaGenerator();
        formulaGen.setCaf(tempCaf);
        Argument theta = tempCaf.getArgument(argName);
        PropositionalQuantifiedFormula qbfFormula = formulaGen.encodeCredulousQBFWithControl(Arrays.asList(new Argument[]{theta}));

        PropositionalFormula formula = qbfFormula.getCafFormula().getFormula();
        if(formula instanceof Tautology) {
            return computeSimpleExtension(tempCaf, argName);
        }
        if(formula instanceof Contradiction) {
            return new ArrayList<>();
        }

        createCNFFile(qbfFormula, path.resolve(cnfFileName));
        Process p = Runtime.getRuntime().exec("./quantom --solvemode=0 " + path.resolve(cnfFileName));
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

    public boolean isCredulouslyAcceptedWithoutControl(Caf tempCaf, String argName) throws IOException {
        CafFormulaGenerator formulaGen = new CafFormulaGenerator();
        formulaGen.setCaf(tempCaf);
        Argument theta = tempCaf.getArgument(argName);
        PropositionalQuantifiedFormula qbfFormula = formulaGen.encodeCredulousQBFWithoutControl(Arrays.asList(new Argument[]{theta}));

        PropositionalFormula formula = qbfFormula.getCafFormula().getFormula();
        if(formula instanceof Tautology) {
            return true;
        }
        if(formula instanceof Contradiction) {
            return false;
        }

        createCNFFile(qbfFormula, path.resolve(cnfFileName));
        Process p = Runtime.getRuntime().exec("./quantom --solvemode=0 " + path.resolve(cnfFileName));
        Collection<Argument> potentSet = readResult(p, qbfFormula);
        return !potentSet.isEmpty();
    }

    private Collection<Argument> readResult(Process p, PropositionalQuantifiedFormula qbfFormula) throws IOException {

        ArrayList<Argument> potentSet = new ArrayList<>();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = buffer.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.equals("s UNSATISFIABLE")) {
                        return potentSet;
                    }

                    if (line.startsWith("v")) {
                        String st[] = line.split(" ");
                        for (int i = 1; i < st.length; i++) {
                            potentSet.add(qbfFormula.getArgumentFor(st[i]));
                        }

                        return potentSet;
                    }
                }
            }
        }

        return potentSet;
    }

    public void createCNFFile(PropositionalQuantifiedFormula qbfFormula, Path path) {

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {

            System.out.println(qbfFormula.getPropositionalFormula().toString());

            Conjunction formula = (Conjunction) qbfFormula.getPropositionalFormula();
            writer.println("p qbf " + qbfFormula.getNbVariables() + " " + formula.size());

            //Quantificateurs
            for(int i = 0; i < qbfFormula.getDegree(); i++) {
                QuantifiedPrefix prefix = qbfFormula.getQuantifiedPrefix(i);
                if(prefix.getType() == QuantifiedPrefix.QuantifiedPrefixType.EXIST) {
                    writer.print("e");
                }
                else {
                    writer.print("a");
                }
                for(Proposition p : prefix.getQuantifiedProposition()) {
                    writer.printf(" " + qbfFormula.getIdentifier(p));
                }

                writer.printf(" 0");
                writer.println();
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

    public boolean isCredulouslyAccepted(Caf tempCaf, List<String> argNames) {
        //TODO
        return false;
    }

    public static void main(String[] args) {
        CafGenerator g = new CafGenerator();
        Caf caf;
        try {
            caf = g.parseCAF("caf1test.caf");

            CafFormulaGenerator formulaGenerator = new CafFormulaGenerator();
            formulaGenerator.setCaf(caf);

            PropositionalQuantifiedFormula qbf = formulaGenerator.encodeCredulousQBFWithControl(
                    formulaGenerator.getCaf().getFixedArguments()
                            .stream().filter(a -> a.getName().equals("a"))
                            .collect(Collectors.toSet()));
            Path path = Paths.get("caf2017.txt");
            QuantomConnector qConnector = new QuantomConnector();
            qConnector.createCNFFile(qbf, path);
            //Collection<Argument> res = qConnector.isCredulouslyAcceptedWithControl(caf, caf.getFixedArguments().stream().findFirst().get().getName());
            //res.forEach(a -> System.out.println(a));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
