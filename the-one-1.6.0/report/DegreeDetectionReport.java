/*

 * @(#)CommunityDetectionReport.java
 *
 * Copyright 2010 by University of Pittsburgh, released under GPLv3.
 * 
 */
package report;

import java.util.*;

import core.*;
import routing.*;
import routing.community.DegreeDetectionEngine;

public class DegreeDetectionReport extends Report {
    
    public DegreeDetectionReport() {
        init();
    }
    
    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        Map<DTNHost, List<Integer>> aDegree = new HashMap<DTNHost, List<Integer>>();
        
        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof DegreeDetectionEngine)) {
                continue;
            }
            
            DegreeDetectionEngine fe = (DegreeDetectionEngine) de;
            
            int[] aDegreeSaya = fe.getDegreeGlobalCentrality();
            List<Integer> arraySaya = new ArrayList<Integer>();
            for (int cent : aDegreeSaya) {
                arraySaya.add(cent);
            }
            aDegree.put(h, arraySaya);
            for(Map.Entry<DTNHost, List<Integer>> entry : aDegree.entrySet()) {
//                DTNHost key = entry.getKey();
//                List<Integer> value = entry.getValue();
                DTNHost a = entry.getKey();
                Integer b = a.getAddress();
                write(""+b+" "+entry.getValue());
            }
        }
        super.done();
        
    }
}
