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
import routing.RoutingDecisionEngine;

/**
 *
 * @author Windows
 */
public class DecisionEngineSprayAndWaitRouter implements RoutingDecisionEngine {

    /**
     * identifier for the initial number of copies setting ({@value})
     */
    public static final String NROF_COPIES = "nrofCopies";
    /**
     * identifier for the binary-mode setting ({@value})
     */
    public static final String BINARY_MODE = "binaryMode";
    /**
     * SprayAndWait router's settings name space ({@value})
     */
    public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouter";
    /**
     * Message property key
     */
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";

    protected static final double DEFAULT_TIMEDIFF = 300;

    protected int initialNrofCopies;
    protected boolean isBinary;

    public DecisionEngineSprayAndWaitRouter(Settings s) {

        initialNrofCopies = s.getInt(NROF_COPIES);
        isBinary = s.getBoolean(BINARY_MODE);
    }

    public DecisionEngineSprayAndWaitRouter(DecisionEngineSprayAndWaitRouter snw) {

        this.initialNrofCopies = snw.initialNrofCopies;
        this.isBinary = snw.isBinary;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        Integer numofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        numofCopies = (int) Math.ceil(numofCopies / 2.0);
        m.updateProperty(MSG_COUNT_PROPERTY, numofCopies);

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

        int numofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (numofCopies > 1) {
            return true;
        }

        return false;

    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        int numofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (numofCopies > 1) {
            numofCopies /= 2;
        } else {
            return true;
        }

        m.updateProperty(MSG_COUNT_PROPERTY, numofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new DecisionEngineSprayAndWaitRouter(this);
    }
}