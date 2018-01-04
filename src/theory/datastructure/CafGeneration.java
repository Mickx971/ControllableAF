package theory.datastructure;

import caf.datastructure.Caf;

import java.io.PrintWriter;

public class CafGeneration {
    private Caf caf1;
    private Caf caf2;

    public CafGeneration(Caf caf1, Caf caf2) {
        this.caf1 = caf1;
        this.caf2 = caf2;
    }

    public Caf getCaf1() {
        return caf1;
    }

    public Caf getCaf2() {
        return caf2;
    }

    @Override
    public String toString() {
        return "#caf1\n" + caf1 + "\n\n#caf2\n" + caf2;
    }

    public void writeToFiles(String fileName) throws Exception{
        PrintWriter writer = new PrintWriter("caf1" + fileName, "UTF-8");
        writer.print(caf1.toString());
        writer.close();

        writer = new PrintWriter("caf2" + fileName, "UTF-8");
        writer.print(caf2.toString());
        writer.close();

    }

    private String getStats(Caf caf)
    {

        return "NumberOfFixedArguments: "+ caf.getFixedArguments().size()
                + "  (" + ((double)caf.getFixedArguments().size()
                /caf.getArguments().size()) * 100
                + "%)\nNumberOfControlArguments: "+ caf.getControlArguments().size()
                + "  (" + ((double)caf.getControlArguments().size()
                /caf.getArguments().size()) * 100
                + "%)\nNumberOfUncertainArguments: "+ caf.getUncertainArguments().size()
                + "  (" + ((double)caf.getUncertainArguments().size()
                /caf.getArguments().size()) * 100
                + "%)\nNumberOfAttacks: "+ caf.getAttacks().size()
                + "  (" + ((double)caf.getAttacks().size()
                /(caf.getArguments().size()*(caf.getArguments().size() - 1))) * 100
                + "%)\nNumberOfCertainAttacks: "+ caf.getCertainAttacks().size()
                + "  (" + ((double)caf.getCertainAttacks().size()
                /caf.getAttacks().size()) * 100
                + "%)\nNumberOfUncertainAttacks: "+ caf.getUncertainAttacks().size()
                + "  (" + ((double)caf.getUncertainAttacks().size()
                /caf.getAttacks().size()) * 100
                + "%)\nNumberOfUndirectedAttacks: "+ caf.getUncertainAttacks().size()
                + "  (" + ((double)caf.getUndirectedAttacks().size()
                /caf.getAttacks().size())* 100 + "%)";

    }

    public String getStats()
    {
        return "caf1\n" + getStats(caf1) + "\n\ncaf2\n" + getStats(caf2);
    }
}
