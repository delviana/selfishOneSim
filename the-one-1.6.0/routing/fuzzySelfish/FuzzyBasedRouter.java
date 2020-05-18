/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzySelfish;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
import routing.DecisionEngineRouter;
import routing.community.BufferDetectionEngine;
//import routing.community.VarianceDetectionEngine;

/**
 *
 * @author Afra Rian Yudianto, Sanata Dharma University
 */
public class FuzzyBasedRouter implements RoutingDecisionEngine, BufferDetectionEngine {

//    public static final String FCL_NAMES = "fcl";
    public static final String CLOSENESS = "closeness";
    public static final String VARIANCE = "variance";
    public static final String TRANSFER_OF_UTILITY = "hasil";
//    public Map<DTNHost, List<Double>> varianceMap;
    public Map<DTNHost, List<Double>> bufferMap;
    public DTNHost thisBuffer;
    private FIS fcl;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    public FuzzyBasedRouter(Settings s) {
//        String fclString = s.getSetting(FCL_NAMES);
//        fcl = FIS.load(fclString);
    }

    public FuzzyBasedRouter(FuzzyBasedRouter t) {
//        this.fcl = t.fcl;
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
//        varianceMap = new HashMap<DTNHost, List<Double>>();
        bufferMap = new HashMap<DTNHost, List<Double>>();

    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // Find or create the connection history list
        thisBuffer = thisHost;
        double getLastDisconnect = 0;
        if (startTimestamps.containsKey(peer)) {
            getLastDisconnect = startTimestamps.get(peer);
        }
        double currentTime = SimClock.getTime();

        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
//            connHistory.put(peer, history);

        } else {
            history = connHistory.get(peer);

        }

//         add this connection to the list
        if (currentTime - getLastDisconnect > 0) {
            history.add(new Duration(getLastDisconnect, currentTime));

        }
        connHistory.put(peer, history);
        this.startTimestamps.remove(peer);
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        this.startTimestamps.put(peer, SimClock.getTime());
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
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

        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {

            return true;
        }
        DTNHost dest = m.getTo();
        FuzzyBasedRouter de = getOtherDecisionEngine(otherHost);
        Double me = getPercepatanBuffer(thisBuffer);
        Double peer = getPercepatanBuffer(otherHost);
//        Double me = this.getNormalizedVarianceOfNodes(dest);
//        Double peer = de.getNormalizedVarianceOfNodes(dest);
        List<Double> history;
        if (!bufferMap.containsKey(otherHost)) {
            history = new LinkedList<Double>();

        } else {
            history = bufferMap.get(otherHost);
        }
//        System.out.println(me);
//        System.out.println(peer);
        history.add(me);
        bufferMap.put(otherHost, history);
        return me > peer;
    }

    private Double getResidualBuffer(DTNHost buffer) {
        //semakin besar semakin penuh
        Double initialBuffer = Double.valueOf(buffer.getRouter().getBufferSize());
        Double x = initialBuffer - Double.valueOf(buffer.getRouter().getFreeBufferSize());
        Double residualBuffer = x / initialBuffer;
        return residualBuffer;
    }

    private Double getKecepatanBuffer(DTNHost h) {
        Double K = 10.0;
        Double epsilon = Math.pow(10, -3);
        Double buffer = Double.valueOf(h.getRouter().getFreeBufferSize());
        Double maxBuffer = Double.valueOf(h.getRouter().getBufferSize());
        Double pow = Math.exp(Math.log(epsilon) * buffer / maxBuffer);
        Double kecepatan = ((K * Math.log(epsilon)) / maxBuffer) * pow;
//        System.out.println(kecepatan);
        return kecepatan;
    }

    private Double getPercepatanBuffer(DTNHost h) {
        Double K = 10.0;
        Double epsilon = Math.pow(10, -3);
        Double buffer = Double.valueOf(h.getRouter().getFreeBufferSize());
        Double maxBuffer = Double.valueOf(h.getRouter().getBufferSize());
        Double pow = (Math.log(epsilon) * buffer) / maxBuffer;
        Double a = (Math.log(epsilon)) / maxBuffer;
        Double percepatan = (K * Math.pow(a, 2)) * Math.exp(pow);
        return percepatan;
    }

    private double DefuzzificationSimilarity(DTNHost nodes) {
        double closenessValue = getClosenessOfNodes(nodes);
        double varianceValue = getNormalizedVarianceOfNodes(nodes);
        FunctionBlock functionBlock = fcl.getFunctionBlock(null);

        functionBlock.setVariable(CLOSENESS, closenessValue);
        functionBlock.setVariable(VARIANCE, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    private double Defuzzificationbuffer(DTNHost nodes) {
        double residualBufferValue = getResidualBuffer(nodes);
//        double varianceValue = getNormalizedVarianceOfNodes(nodes);
        FunctionBlock functionBlock = fcl.getFunctionBlock(null);

        functionBlock.setVariable(CLOSENESS, residualBufferValue);
//        functionBlock.setVariable(VARIANCE, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    public double getVarianceOfNodes(DTNHost nodes) {
        List<Duration> list = getListDuration(nodes);
        Iterator<Duration> duration = list.iterator();
        double temp = 0;
        double mean = getAverageShortestSeparationOfNodes(nodes);
        while (duration.hasNext()) {
            Duration d = duration.next();
            temp += Math.pow((d.end - d.start) - mean, 2);
        }
        return temp / list.size();
    }

    public double getNormalizedVarianceOfNodes(DTNHost nodes) {
        double k = getListDuration(nodes).size();
        double N = 0;
        double sigmf = 0;
        Iterator<Duration> iterator = getListDuration(nodes).iterator();
        while (iterator.hasNext()) {
            Duration duration = iterator.next();
            double timeDuration = (duration.end - duration.start);
            N += timeDuration;
            sigmf += Math.pow(timeDuration, 2);
        }
        Double d = (k * (Math.pow(N, 2) - sigmf)) / (Math.pow(N, 2) * (k - 1));
//        if(d.isNaN()){
//            d=0.0;
//        }
        return d;
    }

    public List<Duration> getListDuration(DTNHost nodes) {
        if (connHistory.containsKey(nodes)) {
            return connHistory.get(nodes);
        } else {
            List<Duration> d = new LinkedList<>();
            return d;
        }
    }

    private Double getClosenessOfNodes(DTNHost nodes) {
        double rataShortestSeparation = getAverageShortestSeparationOfNodes(nodes);
        double variansi = getVarianceOfNodes(nodes);

        Double c = Math.exp(-(Math.pow(rataShortestSeparation, 2) / (2 * variansi)));
//        System.out.println(c);
        if (c.isNaN()) {
            c = 0.0;
        }
        return c;
    }

    public double getAverageShortestSeparationOfNodes(DTNHost nodes) {
        List<Duration> list = getListDuration(nodes);
        Iterator<Duration> duration = list.iterator();
        double hasil = 0;
        while (duration.hasNext()) {
            Duration d = duration.next();
            hasil += (d.end - d.start);
        }
        return hasil / list.size();
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new FuzzyBasedRouter(this);
    }

    private FuzzyBasedRouter getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (FuzzyBasedRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
//FOR REPORT PURPOSE

    @Override
    public Map<DTNHost, List<Double>> getBufferMap() {
        return bufferMap;
    }
//    @Override
//    public Map<DTNHost, List<Double>> getVarianceMap() {
//        return varianceMap;
//    }

}