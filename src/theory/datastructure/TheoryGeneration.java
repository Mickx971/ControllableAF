package theory.datastructure;

import caf.datastructure.Caf;
import theory.datastructure.Theory;

import java.io.PrintWriter;

public class TheoryGeneration {
    Theory T1;
    Theory T2;
    Theory sharedTheory;

    public TheoryGeneration() {
    }

    public TheoryGeneration(Theory t1, Theory t2, Theory sharedTheory) {
        T1 = t1;
        T2 = t2;
        this.sharedTheory = sharedTheory;
    }

    public Theory getT1() {
        return T1;
    }

    public void setT1(Theory t1) {
        T1 = t1;
    }

    public Theory getT2() {
        return T2;
    }

    public void setT2(Theory t2) {
        T2 = t2;
    }

    public Theory getSharedTheory() {
        return sharedTheory;
    }

    public void setSharedTheory(Theory sharedTheory) {
        this.sharedTheory = sharedTheory;
    }

    @Override
    public String toString() {
        return "#sharedTheory\n" + sharedTheory + "\n#T1\n" + T1 + "\n#T2\n" + T2;
    }

    public String getStats() {
        return "sharedTheory:\n" + sharedTheory.getStats()
                +"\n\nT1:\n" + T1.getStats()
                +"\n\nT2:\n" + T2.getStats()
                + "\n\nrateOfSharedEpistemicArguments: "
                + ((double)sharedTheory.getEpistemicArguments().size()
                /T1.getEpistemicArguments().size())
                +"\nrateOfSharedPracticalArguments: "
                + ((double) sharedTheory.getPracticalArguments().size()
                / T1.getPracticalArguments().size())
                +"\nrateOfSharedControlArguments: "
                + ((double) sharedTheory.getControlArguments().size()
                / T1.getControlArguments().size())
                +"\nrateOfSharedAttacks: "
                + ((double) sharedTheory.getDungTheory().getAttacks().size()
                / T1.getDungTheory().getAttacks().size());
    }

    public void writeToFile(String fileName) throws Exception{
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.print(toString());
        writer.close();

    }
}
