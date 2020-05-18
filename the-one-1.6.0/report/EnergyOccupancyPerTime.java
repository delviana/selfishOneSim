/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.*;
import java.util.*;

public class EnergyOccupancyPerTime extends Report implements UpdateListener {

    public static final String ENERGY_REPORT__INTERVAL = "occupancyInterval";
    public static final int DEFAULT_ENERGY_REPORT_INTERVAL = 300;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Double>> energyCounts = new HashMap<DTNHost, ArrayList<Double>>();

    public EnergyOccupancyPerTime() {
        super();

        Settings settings = getSettings();
        if (settings.contains(ENERGY_REPORT__INTERVAL)) {
            interval = settings.getInt(ENERGY_REPORT__INTERVAL);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_ENERGY_REPORT_INTERVAL;
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }
        if (simTime - lastRecord >= interval) {
//            lastRecord = SimClock.getTime();
            printLine(hosts);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    private void printLine(List<DTNHost> hosts) {
        for (DTNHost h : hosts) {
            ArrayList<Double> energyList = new ArrayList<Double>();
            Double temp = (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
//            temp = (temp <= 1000.0) ? (temp) : (1000.0);
            if (energyCounts.containsKey(h)) {
//                    bufferCounts.put(h,(bufferCounts.get(h) + temp)/2); seems WRONG;
//                    bufferCounts.put(h,(bufferCounts.get(h) + temp);
//                      write("" +bufferCounts.get(h));
                        energyList = energyCounts.get(h);
                        energyList.add(temp);
                        energyCounts.put(h, energyList);
            }else{
                energyCounts.put(h, energyList);
//                write(""+ bufferCounts.get(h));
            }

        }
    }

    @Override
    public void done(){
            for(Map.Entry<DTNHost, ArrayList<Double>> entry : energyCounts.entrySet()){
            DTNHost a = entry.getKey();
            Integer b = a.getAddress();
            
//            Double avgBuffer = entry.getValue()/updateCounter;
//              String printHost = "Node" + entry.getKey().getAddress() +"\t";
                String printHost = "Node" + a.toString() + "\t";
                for(Double energyList : entry.getValue()){
                    printHost = printHost + "\t" + energyList;
                }
                write(printHost);
//                write("" + b + ' ' + entry.getValue());

    }
            super.done();
}}
