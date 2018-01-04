package theory.datastructure;

import caf.datastructure.Caf;
import theory.datastructure.Theory;

public class TheoryGeneration {
    Theory T1;
    Theory T2;
    Theory sharedTheory;

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
}
