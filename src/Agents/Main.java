package Agents;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.sf.tweety.logics.pl.sat.Sat4jSolver;
import net.sf.tweety.logics.pl.sat.SatSolver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String args[])
    {
        Runtime runtime = Runtime.instance();
        Profile config = new ProfileImpl("localhost", 8888, null);
        config.setParameter("gui", "false");
        AgentContainer mc = runtime.createMainContainer(config);
        AgentController ac;
        SatSolver.setDefaultSolver(new Sat4jSolver());
        try {

            Properties prop = new Properties();
            InputStream is = new FileInputStream("solver.config");
            prop.load(is);
            boolean maxSoftAg1 = Boolean.parseBoolean(prop.getProperty("nego1.use-maxQBF"));
            boolean maxSoftAg2 = Boolean.parseBoolean(prop.getProperty("nego2.use-maxQBF"));

            Object[] agent1Arguments = {new AID("negotiator2", AID.ISLOCALNAME),
                    true, 1, maxSoftAg1};
            Object[] agent2Arguments = {new AID("negotiator1", AID.ISLOCALNAME), false,
                    2, maxSoftAg2};

            ac = mc.createNewAgent("negotiator1",
                    NegotiationAgent.class.getName(), agent1Arguments);
            ac.start();

            ac = mc.createNewAgent("negotiator2",
                    NegotiationAgent.class.getName(), agent2Arguments);
            ac.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
