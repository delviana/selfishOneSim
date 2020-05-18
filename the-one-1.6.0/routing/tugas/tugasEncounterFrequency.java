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

import routing.community.Duration;
import routing.community.FrequencyDecisionEngine;

/**
 *
 * @author jarkom
 */
public class tugasEncounterFrequency implements RoutingDecisionEngine,FrequencyDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    int encounterPeer;
    int encounterThis;

    public tugasEncounterFrequency(Settings s) {

    }

    public tugasEncounterFrequency(tugasEncounterFrequency t) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
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
        } else {
            history=connHistory.get(peer);
//            connHistory.get(peer).add(new Duration(time, etime));
        }

        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
            connHistory.put(peer, history);
        }

//        startTimestamps.remove(peer);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        tugasEncounterFrequency de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo()==aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
       return m.getTo()!=thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        tugasEncounterFrequency de = getOtherDecisionEngine(otherHost);
        if (de.connHistory.containsKey(dest)) {
            encounterPeer = de.connHistory.get(dest).size();
        } if (this.connHistory.containsKey(dest)) {
            encounterThis = this.connHistory.get(dest).size();
        }
        return encounterPeer > encounterThis;
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
        return new tugasEncounterFrequency(this);
    }

    private tugasEncounterFrequency getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (tugasEncounterFrequency) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public Map<DTNHost, List<Duration>> getFrequency() {
        return this.connHistory;
    }
}