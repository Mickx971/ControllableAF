package caf.generator;

import caf.datastructure.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CafGenerator {

    private Pattern afPattern = Pattern.compile("^(arg\\([\\pL\\pN]+|att\\([\\pL\\pN]+,[\\pL\\pN]+)\\)\\.\\s*$");
    private Pattern cafPattern = Pattern.compile("^(f_arg\\([\\pL\\pN]+|c_arg\\([\\pL\\pN]+|u_arg\\([\\pL\\pN]+|att\\([\\pL\\pN]+,[\\pL\\pN]+|u_att\\([\\pL\\pN]+,[\\pL\\pN]+|ud_att\\([\\pL\\pN]+,[\\pL\\pN]+)\\)\\.\\s*$");

    public enum AfTag {
        arg,
        att
    }

    public enum CafTag {
        f_arg,
        c_arg,
        u_arg,
        att,
        u_att,
        ud_att
    }

    private Caf parseAF(String filename) throws IOException {
        Caf caf = new Caf();
        List<String> lines = Files.readAllLines(Paths.get(filename));

        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if(line.trim().isEmpty())
                continue;

            Matcher matcher = afPattern.matcher(line);

            if(matcher.find()) {
                String[] words = matcher.group().split("\\(");
                AfTag type = AfTag.valueOf(words[0]);
                words = words[1].substring(0,words[1].indexOf(")")).split(",");

                switch(type) {
                    case arg:
                        caf.addFixedArgument(words[0]);
                        break;
                    case att:
                        caf.addAttack(words[0], words[1]);
                        break;
                }
            }
            else {
                throw new IOException("Error at line: " + (i+1));
            }
        }

        return caf;
    }

    public Caf parseCAF(String filename) throws IOException {
        Caf caf = new Caf();
        List<String> lines = Files.readAllLines(Paths.get(filename));

        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if(line.trim().isEmpty())
                continue;

            Matcher matcher = cafPattern.matcher(line);

            if(matcher.find()) {
                String[] words = matcher.group().split("\\(");
                CafTag type = CafTag.valueOf(words[0]);
                words = words[1].substring(0,words[1].indexOf(")")).split(",");

                switch(type) {
                    case f_arg:
                        caf.addFixedArgument(words[0]);
                        break;
                    case c_arg:
                        caf.addControlArgument(words[0]);
                        break;
                    case u_arg:
                        caf.addUncertainArgument(words[0]);
                        break;
                    case att:
                        caf.addAttack(words[0], words[1]);
                        break;
                    case u_att:
                        caf.addUncertainAttack(words[0], words[1]);
                        break;
                    case ud_att:
                        caf.addUndirectedAttack(words[0], words[1]);
                        break;
                }
            }
            else {
                throw new IOException("Error at line: " + (i+1));
            }
        }

        return caf;
    }

    private void configureCaf(Caf caf, CafConfiguration conf, Caf inputAf, LinkedList<Argument> allArg) {

        int fixedArgPart = (int)((conf.FixedPartRate/100) * allArg.size());
        int uncertainArgPart = (int)((conf.UncertainPartRate/100) * inputAf.getArguments().size());
        int unknownArgPart = (int)((conf.UnknownArgumentRate/100) * inputAf.getArguments().size());

        Collections.shuffle(allArg);
        Set<Argument> unknownArgs = new HashSet<>();
        for(int i = 0; i < unknownArgPart; i++) {
            unknownArgs.add(allArg.removeFirst());
        }

        Collections.shuffle(allArg);
        for(int i = 0; i < fixedArgPart; i++) {
            caf.addFixedArgument(allArg.removeFirst().getName());
        }

        Collections.shuffle(allArg);
        for(int i = 0; i < uncertainArgPart; i++) {
            caf.addUncertainArgument(allArg.removeFirst().getName());
        }

        for(Argument a : allArg) {
            caf.addControlArgument(a.getName());
        }

        HashSet<Attack> allUAttackSet =  new HashSet<>();
        for(Argument arg : caf.getUncertainArguments()) {
            allUAttackSet.addAll(inputAf.getAllArgAttack(arg.getName()).stream().filter(
                    att -> {
                        Argument[] arguments = att.getArguments();
                        return !unknownArgs.contains(arguments[0]) && !unknownArgs.contains(arguments[1]);
                    }
            ).collect(Collectors.toSet()));
        }

        LinkedList<Attack> allUAttack =  new LinkedList<>(allUAttackSet);
        HashSet<Attack> otherAttacks = new HashSet<>(inputAf.getAttacks());

        Collection<Argument> uncertainArguments = caf.getUncertainArguments();
        otherAttacks.removeIf( att -> {
            Argument[] arguments = att.getArguments();
            boolean unknown = unknownArgs.contains(arguments[0]) || unknownArgs.contains(arguments[1]);
            boolean uncertain = uncertainArguments.contains(arguments[0]) || uncertainArguments.contains(arguments[1]);
            return unknown || uncertain;
        });

        int undirectedAttackPart = (int)((conf.UndirectedAttackRate/100) * allUAttack.size());
        int uncertainAttackPart = (int)((conf.UncertainAttackRate/100) * allUAttack.size());

        Collections.shuffle(allUAttack);
        for(int i = 0; i < undirectedAttackPart; i++) {
            Argument[] arguments = allUAttack.removeFirst().getArguments();
            caf.addUndirectedAttack(arguments[0].getName(), arguments[1].getName());
        }

        Collections.shuffle(allUAttack);
        for(int i = 0; i < uncertainAttackPart; i++) {
            Argument[] arguments = allUAttack.removeFirst().getArguments();
            caf.addUncertainAttack(arguments[0].getName(), arguments[1].getName());
        }

        for(Attack att : allUAttack) {
            Argument[] arguments = att.getArguments();
            caf.addAttack(arguments[0].getName(), arguments[1].getName());
        }

        for(Attack att : otherAttacks) {
            Argument[] arguments = att.getArguments();
            caf.addAttack(arguments[0].getName(), arguments[1].getName());
        }
    }

    public Caf createCaf(Caf af, CafConfiguration conf) {
        Caf caf = new Caf();
        LinkedList<Argument> allArg = new LinkedList<>(af.getArguments());
        configureCaf(caf, conf, af, allArg);
        return caf;
    }

    public List<Caf> createCafs(Caf af, CafCommonConfiguration conf) {
        List<Caf> cafs = new ArrayList<>();
        for(int i = 0; i < conf.getConfs().size(); i++) {
            cafs.add(new Caf());
        }

        float minFixedPart = conf.getConfs().stream().min( (c1,c2) -> {
            if(c1.FixedPartRate > c2.FixedPartRate)
                return 1;
            return -1;
        }).get().FixedPartRate;

        LinkedList<Argument> allArg = new LinkedList<>(af.getArguments());
        int commonFixedArgPart = (int)((conf.CommonFixedPartRate/100) * (minFixedPart/100) * allArg.size());

        Collections.shuffle(allArg);
        for(int i = 0; i < commonFixedArgPart; i++) {
            String argName = allArg.removeFirst().getName();
            for(Caf caf : cafs) {
                caf.addFixedArgument(argName);
            }
        }

        float commonFixedRate = ((float) commonFixedArgPart * 100) / af.getArguments().size();

        for(int i = 0; i < conf.getConfs().size(); i++) {
            CafConfiguration c = conf.getConfs().get(i);
            c.FixedPartRate -= commonFixedRate;
            configureCaf(cafs.get(i), c, af, new LinkedList<>(allArg));
        }

        return cafs;
    }

    public Caf generateCafFromAF(String filename, CafConfiguration conf) throws IOException {
        Caf af = parseAF(filename);
        return createCaf(af, conf);
    }

    public List<Caf> generateCafsFromAF(String filename, CafCommonConfiguration conf) throws IOException {
        Caf af = parseAF(filename);
        return createCafs(af, conf);
    }

    public static void main(String[] args) {
        CafGenerator g = new CafGenerator();
        try {
            String filename = "/Users/mickx/Desktop/A/1/stb_190_70.apx";
            String outputPath = "/Users/mickx/Desktop";
//            CafConfiguration conf = new CafConfiguration(50, 30, 30,30, 10);
//            Caf caf = g.generateCafFromAF(filename, conf);
//            System.out.println(caf);


            List<CafConfiguration> confs = new ArrayList<>();
            confs.add(new CafConfiguration(20, 30, 30,30));
            confs.add(new CafConfiguration(20, 30, 30,30, 10));
            CafCommonConfiguration cconf = new CafCommonConfiguration(confs,10);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

            List<Caf> cafs = g.generateCafsFromAF(filename, cconf);
            for(Caf caf : cafs) {
                Path outputFileName = Paths.get(outputPath).resolve("caf" + sdf.format(new Date()));
                PrintWriter writer = new PrintWriter(outputFileName.toFile(), "UTF-8");
                writer.println(caf);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
