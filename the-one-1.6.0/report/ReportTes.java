/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.GlobalCentralityEngine;


/**
 *
 * @author Acer
 */
public class ReportTes extends Report  {

    Map<DTNHost,List<Integer>> Centrality = new HashMap<>();

    public ReportTes() {
        init();
    }


   
   public void done(){
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        
        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if(!(r instanceof DecisionEngineRouter)){
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter)r).getDecisionEngine();
            if(!(de instanceof GlobalCentralityEngine)){
                continue;
            }
            GlobalCentralityEngine cd =(GlobalCentralityEngine)de;
            int [] array = cd.ArrayGlobalCentrality();
            List<Integer> myarray = new ArrayList<>();
            for (int i : array) myarray.add(i);
            Centrality.put(h, myarray);
            
            
        }
        for (Map.Entry<DTNHost, List<Integer>> entry : Centrality.entrySet()) {
           DTNHost key = entry.getKey();
           List<Integer> value = entry.getValue();
           write(key+" "+value);
       }
        super.done();
    }

}