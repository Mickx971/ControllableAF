package caf.datastructure;

public class CafConfiguration {
    public float FixedPartRate;
    public float UncertainPartRate;
    public float UncertainAttackRate;
    public float UndirectedAttackRate;
    public float UnknownArgumentRate;

    public CafConfiguration(float FixedPartRate, float UncertainPartRate, float UncertainAttackRate, float UndirectedAttackRate) {
        this(FixedPartRate, UncertainPartRate, UncertainAttackRate, UndirectedAttackRate, 0);
    }

    public CafConfiguration(float FixedPartRate, float UncertainPartRate, float UncertainAttackRate, float UndirectedAttackRate, float UnknownArgumentRate) {
        this.FixedPartRate = FixedPartRate;
        this.UncertainPartRate = UncertainPartRate;
        this.UncertainAttackRate = UncertainAttackRate;
        this.UndirectedAttackRate = UndirectedAttackRate;
        this.UnknownArgumentRate = UnknownArgumentRate;
    }
}
