/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.tugas;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.BubbleRap;
import routing.community.CommunityDetection;
import routing.community.Duration;

/**
 *
 * @author jarkom
 */
public class tugasTotalContactPeriod implements RoutingDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, Double> rata;

//    double total;
    double encounterPeer;
    double encounterThis;

    public tugasTotalContactPeriod(Settings s) {

    }

    public tugasTotalContactPeriod(tugasTotalContactPeriod t) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        rata = new HashMap<DTNHost, Double>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = startTimestamps.get(peer);
        double etime = SimClock.getTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
//            connHistory.put(peer, history);
        } else {
            history=connHistory.get(peer);
//            connHistory.put(peer, (connHistory.get(peer) + (etime - time)));
        }

        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
            
        } 
        connHistory.put(peer, history);
        
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        tugasTotalContactPeriod de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return  m.getTo()==aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return  m.getTo()!=thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        tugasTotalContactPeriod de = getOtherDecisionEngine(otherHost);
        if (de.connHistory.containsKey(dest)) {
            double total = 0;
            for (Duration duration : de.connHistory.get(dest)) {
                total+=duration.end-duration.start;
            }
           encounterPeer=total;

        } else if (this.connHistory.containsKey(dest)) {
            double total = 0;
            for (Duration duration : this.connHistory.get(dest)) {
                total+=duration.end-duration.start;
            }
           encounterThis=total;
        }
        return encounterPeer>encounterThis;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return m.getTo()==otherHost;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new tugasTotalContactPeriod(this);
    }

    private tugasTotalContactPeriod getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (tugasTotalContactPeriod) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}