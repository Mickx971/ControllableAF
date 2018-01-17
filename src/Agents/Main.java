package Agents;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.sf.tweety.arg.dung.DungTheory;
import net.sf.tweety.arg.dung.StableReasoner;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.syntax.Attack;
import net.sf.tweety.commons.Interpretation;
import net.sf.tweety.logics.pl.sat.LingelingSolver;
import net.sf.tweety.logics.pl.sat.Sat4jSolver;
import net.sf.tweety.logics.pl.sat.SatSolver;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;
import theory.generator.TheoryGenerator;

import java.util.Collection;

public class Main {

    public static void main(String args[])
    {
        Runtime runtime = Runtime.instance();
        Profile config = new ProfileImpl("localhost", 8888, null);
        config.setParameter("gui", "true");
        AgentContainer mc = runtime.createMainContainer(config);
        AgentController ac;
        SatSolver.setDefaultSolver(new Sat4jSolver());
        try {
            Object[] agent1Arguments = {new AID("negotiator2", AID.ISLOCALNAME),
                    true, 1};
            Object[] agent2Arguments = {new AID("negotiator1", AID.ISLOCALNAME), false,
                    2};

            ac = mc.createNewAgent("negociator1",
                    NegotiationAgent.class.getName(), agent1Arguments);
            ac.start();

            ac = mc.createNewAgent("negotiator2",
                    NegotiationAgent.class.getName(), agent2Arguments);
            ac.start();


        } catch (StaleProxyException e) {
            e.printStackTrace();
            return;
        }


//        try {
//            SatSolver.setDefaultSolver(new Sat4jSolver());
//            TheoryGenerator g = new TheoryGenerator();
//            StableReasoner sr = new StableReasoner(g.parseFromFile("theory.theory").getT1().getDungTheory());
//            System.out.println(sr.getExtensions());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
