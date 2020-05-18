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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

import routing.community.Duration;

/**
 *
 * @author Acer
 */
public class tugasShortestSeparationPeriod implements RoutingDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, List<Double>> durasi;

    double encounterPeer;
    double encounterThis;

    public tugasShortestSeparationPeriod(Settings s) {

    }

    public tugasShortestSeparationPeriod(tugasShortestSeparationPeriod t) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }
//    private int getLastConnection(List)

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // Find or create the connection history list
        double time = 0;
        if (startTimestamps.containsKey(peer)) {
            time = startTimestamps.get(peer);
        }

        double etime = SimClock.getTime();
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
//            connHistory.put(peer, history);

        } else {
            history = connHistory.get(peer);

        }

//         add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));

        }
        connHistory.put(peer, history);
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        tugasShortestSeparationPeriod de = this.getOtherDecisionEngine(peer);  
        startTimestamps.put(peer, SimClock.getTime());
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer
    ) {
        DTNHost myHost = con.getOtherNode(peer);
        tugasShortestSeparationPeriod de = this.getOtherDecisionEngine(peer);

//        this.startTimestamps.put(peer, SimClock.getTime());
//        de.startTimestamps.put(myHost, SimClock.getTime());
    }

    @Override
    public boolean newMessage(Message m
    ) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost
    ) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost
    ) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost
    ) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        tugasShortestSeparationPeriod de = getOtherDecisionEngine(otherHost);
        if (de.connHistory.containsKey(dest)) {
            double hasil = 0;
            for (int i = 0; i < de.connHistory.get(dest).size(); i++) {
                hasil += (de.connHistory.get(dest).get(i).end) - (de.connHistory.get(dest).get(i).start);
            }
            encounterPeer = hasil;
        }
        if (this.connHistory.containsKey(dest)) {
            double hasil = 0;
            for (int i = 0; i < this.connHistory.get(dest).size(); i++) {
                hasil += (this.connHistory.get(dest).get(i).end) - (this.connHistory.get(dest).get(i).start);
            }
            encounterThis = hasil;

        }
        return encounterPeer < encounterThis;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost
    ) {
        return m.getTo() == otherHost;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld
    ) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new tugasShortestSeparationPeriod(this);
    }

    private tugasShortestSeparationPeriod getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (tugasShortestSeparationPeriod) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}