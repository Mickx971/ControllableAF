package caf.datastructure;

import java.util.List;

public class CafCommonConfiguration {

    public final float CommonFixedPartRate;

    private List<CafConfiguration> confs;

    public CafCommonConfiguration(List<CafConfiguration> confs, float CommonFixedPartRate ) {
        this.confs = confs;
        this.CommonFixedPartRate = CommonFixedPartRate;
    }

    public List<CafConfiguration> getConfs() {
        return confs;
    }
}
