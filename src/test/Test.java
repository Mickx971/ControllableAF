package test;

import caf.datastructure.Caf;
import caf.generator.CafGenerator;
import solver.QuantomConnector;

import java.io.IOException;

public class Test {

    public static void main(String args[])
    {
        CafGenerator g = new CafGenerator();
        try {
            Caf c = g.parseCAF("caf1.caf");
            c.setAgentName("negotiator1");
            System.out.println(c.argumentIsCredulouslyAcceptedWithoutControl("b"));
            System.out.println(c);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
