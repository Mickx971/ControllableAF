package Agents;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String args[])
    {
        Runtime runtime = Runtime.instance();
        Profile config = new ProfileImpl("localhost", 8888, null);
        config.setParameter("gui", "true");
        AgentContainer mc = runtime.createMainContainer(config);
        AgentController ac;
        try {
            Object[] agentArguments = {new AID(), true};

            ac = mc.createNewAgent("negociator1",
                    NegociationAgent.class.getName(), agentArguments);
            ac.start();


        } catch (StaleProxyException e) {
            e.printStackTrace();
            return;
        }
    }
}
