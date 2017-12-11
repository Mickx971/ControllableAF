package caf.generator;

import caf.datastructure.Caf;
import caf.datastructure.CafConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CafGenerator {

    private Pattern afPattern = Pattern.compile("^(arg\\([\\pL\\pN]+|att\\([\\pL\\pN]+,[\\pL\\pN]+)\\)\\.\\s*$");
    private Pattern cafPattern = Pattern.compile("^(f_arg\\([\\pL\\pN]+|c_arg\\([\\pL\\pN]+|u_arg\\([\\pL\\pN]+|att\\([\\pL\\pN]+,[\\pL\\pN]+|u_att\\([\\pL\\pN]+,[\\pL\\pN]+|ud_att\\([\\pL\\pN]+,[\\pL\\pN]+)\\)\\.\\s*$");

    public enum CafTag {
        arg,
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
            Matcher matcher = afPattern.matcher(line);

            if(matcher.find()) {
                String[] words = matcher.group().split("\\(");
                CafTag type = CafTag.valueOf(words[0]);
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

    private Caf parseCAF(String filename) throws IOException {
        Caf caf = new Caf();
        List<String> lines = Files.readAllLines(Paths.get(filename));

        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
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

    public Caf generateCafFromAF(String filename, CafConfiguration conf) throws IOException {
        Caf caf = parseAF(filename);
        return caf.transform(conf);
    }

    public static void main(String[] args) {
        CafGenerator g = new CafGenerator();
        try {
            CafConfiguration conf = new CafConfiguration(50, 30, 30,30);
            Caf caf = g.generateCafFromAF("/Users/mickx/Desktop/A/1/stb_190_70.apx", conf);
            System.out.println(caf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
