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
 * @author Acer
 */
public class tugasAverageContactPeriod implements RoutingDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, Double> ratarata;
    double encounterPeer;
    double encounterThis;

    public tugasAverageContactPeriod(Settings s) {

    }

    public tugasAverageContactPeriod(tugasAverageContactPeriod t) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        ratarata = new HashMap<DTNHost, Double>();
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
            connHistory.put(peer, history);
            ratarata.put(peer, 0.0);
        } else {
//            history = connHistory.get(peer);
            connHistory.get(peer).add(new Duration(time, etime));
            ratarata.put(peer, (ratarata.get(peer) + (etime - time)));
        }

        // add this connection to the list
//        if (etime - time > 0) {
//            history.add(new Duration(time, etime));
//            
//        }
    }
//        else {
//            startTimestamps.remove(peer);
//        }


@Override
        public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        tugasAverageContactPeriod de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

    }

    @Override
        public boolean newMessage(Message m) {
        return true;
    }

    @Override
        public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
        public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
        public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        tugasAverageContactPeriod de = getOtherDecisionEngine(otherHost);
        if (de.connHistory.containsKey(dest)) {
            encounterPeer = de.ratarata.get(dest) / de.connHistory.get(dest).size();

        } else if (this.connHistory.containsKey(dest)) {

            encounterThis = this.ratarata.get(dest) / this.connHistory.get(dest).size();
        }
        if (encounterPeer > encounterThis) {
            return true;
        } else {
            return false;
        }
    }

    @Override
        public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return m.getTo() == otherHost;
    }

    @Override
        public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
        public RoutingDecisionEngine replicate() {
        return new tugasAverageContactPeriod(this);
    }

    private tugasAverageContactPeriod getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (tugasAverageContactPeriod) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}