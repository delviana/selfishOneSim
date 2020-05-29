package routing.bubleRapSelfish;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Centrality;
import routing.community.CommunityDetection;
import routing.community.CommunityDetectionEngine;
import routing.community.Duration;
import routing.community.SimpleCommunityDetection;

/**
 *
 * @author Jarkom
 */
public class BubbleRapSelfishNode implements RoutingDecisionEngine, CommunityDetectionEngine {

    //setting ID untuk setting algoritma deteksi Komunitas : setting id {@value{}
    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    //setting ID untuk setting algoritma komputasi centrality 
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
    //Membuat Map untuk startTimestamps
    protected Map<DTNHost, Double> startTimestamps;
    //Membuat Map untuk Conection history, Duration menggunakan List
    protected Map<DTNHost, List<Duration>> connHistory;
    //inisialisasi komunity
    protected CommunityDetection community;
    //Mengambil TTL dari EnergyModel
    public static final String MSG_TTL_S = "msgTTL";
    //Mengambil messaget TTL
    protected int msgTTL;
    //inisialisasi centrality
    protected Centrality centrality;
    protected DTNHost thisHosts;

    protected Map<DTNHost, List<TupleForwardReceive>> exChange;

    private double currentEnergy;

    public BubbleRapSelfishNode(Settings s) {

        //contains : mengembalikan nilai true jika nama settingan memiliki beberapa nilai tertentu 
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            //jika Community Bernilai, maka nilai comunity akan dilemparkan ke comunity detection (kelas interface)
            //maka buat objek dari community_alg_setting 
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
            //jika tidak maka
        } else {
            //comunity detection yang digunakan adalah simplecomunitydetection
            this.community = new SimpleCommunityDetection(s);
        }
    }

    public BubbleRapSelfishNode(BubbleRapSelfishNode proto) {
        this.community = proto.community.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        exChange = new HashMap<>();
    }

    @Override
    //pertama kali koneksi dibangun
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        thisHosts = thisHost;
        //cek baterai level dari kedua node baik thisHost maupun peer apakah diatas thr=misalnya 7000
        if ((getEnergy(thisHost) > 7000) && (getEnergy(peer) > 7000)) {
            //exchange ListPastForwards_O dan ListPastReceive_I
            CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;

            List<TupleForwardReceive> FR = null;
            if (!exChange.containsKey(peer)) {
                FR = new LinkedList<TupleForwardReceive>();
            } else {
                FR = exChange.get(peer);
            }

            ListPastForwards_O O = new ListPastForwards_O(thisHost, peer, this.community, peerCD, SimClock.getTime());
            ListPastReceive_I I = new ListPastReceive_I(thisHost, peer, this.community, peerCD, SimClock.getTime());

//            tuple = new TupleForwardReceive(O, I);
//            this.exChange.put(pastO, pastI);
//            //exchange = keep only the most recent contact
            //exhange battery level and metadata of message carried
            //compute utilities of each message (perceived altruism value of the message
            //with he regard to the peer.
            //bila batery level tidak lebih besar dari 7000/threshold, maka            
        } else {
            //tidak terjadi pertukaran atau Conectiondown
//            double time = startTimestamps.get(peer);
//            double etime = SimClock.getTime();
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = startTimestamps.get(peer);
        double etime = SimClock.getTime();
//        System.out.println(getInitialEnergy(thisHost));

//menemukan atau membuat daftar Connection History
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }
//menambahkan koneksi ke dalam daftar
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }
//perhatikan 
        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;
//menginformasikan objek komunitas bahwa koneksi telah terputus
//objek mungkin membutuhkan Connection History saat ini
        community.connectionLost(thisHost, peer, peerCD, history);

//startTimestamps.remove(peer);
    }

    @Override
    //pertukaran untuk koneksi baru
    public void doExchangeForNewConnection(Connection con, DTNHost peer
    ) {
        DTNHost myHost = con.getOtherNode(peer);
        BubbleRapSelfishNode de = this.getOtherDecisionEngine(peer);
        //deklarasi objek baru getOtherDecisionEngine

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

        this.community.newConnection(myHost, peer, de.community);

        //pastO(myHost,peer, this.community, de.community)
//        this.past_O.newConnection(myHost, peer, de.past_O);
//        de.past_I.newConnection(peer, myHost, this.past_I);
    }

    @Override
    public boolean newMessage(Message m
    ) {
        return true;            //selalu simpan dan menruskan pesan yang dibuat
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost
    ) {
        return m.getTo() == aHost;      //unicast Routing
        //jika host adalah final destination berikan pesan
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost
    ) {
        return m.getTo() != thisHost;
        //jika host bukan dsetination teruskan
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost
    ) {
        if (m.getTo() == otherHost) {
            return true;        //sepele (trivial) untuk disimpan ke destination
        }

        //disini akan diputuskan kapan akan meneruskan pesan
        // Di BubleRap bekerja keras pd awal forwarding pesan di Global Central ia akan terus mencari hingga
        //menemukan node yang memiliki tujuan pesan yang sama dgn destination dalam lokal komunitas yang sama
        //pada saat masuk di Local Community dia akan menggunakan matriks lokal centrality 
        //untuk meneruskan pesan ke dalam komunitas
        DTNHost dest = m.getTo();
        BubbleRapSelfishNode de = getOtherDecisionEngine(otherHost);

        //yang memiliki local community terbaik dgn destination, host atau peer
        boolean peerInCommunity = de.commumesWithHost(dest); // apakh peer di dlm community nya dest
        boolean meInCommunity = this.commumesWithHost(dest); // apakh THIS ada di community nya dest

        if (peerInCommunity && !meInCommunity) // peer is in dest's community, but THIS is not
        {
            return true;
        } else if (!peerInCommunity && meInCommunity) // THIS is in dest'community, but peer is not
        {
            return false;
        } else if (peerInCommunity) // We're both in dest'community
        {
            // Forward to the one with the higher local centrality (in dest'community)

        }
        Double me = getEnergy(thisHosts);
        Double peer = getEnergy(otherHost);
        System.out.println("me = " + me + " peer = " + peer);

//        System.out.println(getInitialEnergy(dest));
//        System.out.println(getBuffer(dest));
//        System.out.println(getEnergy(dest));
//        System.out.println(getResidualEnergy(dest));
//        System.out.println(getResidualBuffer(dest));
//        return ((me > 9000) && (peer > 9000));
        return true;
    }

    @Override
    //di BubleRap memungkinkan suatu node utk remove pesan setelah itu diteruskan ke dalam 
    //local comunity dari destination
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost
    ) {
        // delete the message once it is forwarded to the node in the dest'community
        BubbleRapSelfishNode de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld
    ) {
//        BubbleRapSelfishNode de = this.getOtherDecisionEngine(hostReportingOld);
//        return de.commumesWithHost(m.getTo())
//                && !this.commumesWithHost(m.getTo());

        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new BubbleRapSelfishNode(this);
    }

    public void moduleValueChanged(String key, Object newValue) {
        this.currentEnergy = (Double) newValue;
    }

    private BubbleRapSelfishNode getOtherDecisionEngine(DTNHost peer) {
        MessageRouter otherRouter = peer.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (BubbleRapSelfishNode) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    private Double getEnergy(DTNHost host) {
        //ambil energy level dari energy modelnya
        return (Double) host.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) {
        //inisialisasi energy awal
//        System.out.println(getInitialEnergy(h));
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }

    //ambil waktu TTL dari pesan M
    private Double getTTL(DTNHost h) {
        //ambil semua collection message
        Collection<Message> list = h.getMessageCollection();
        //pakai iterator untuk ambil list TTL
        Iterator<Message> message = list.iterator();
        while (message.hasNext()) {
            Message m = message.next();
            if (m.getTo() == h) {
            }
            //kembalikan message.getTTL
            return Double.valueOf(m.getTtl());
        }
        //kembalikan null
        return null;
    }

    private Double getInitialTTL() {
        //inisialisasi nilai TTL awal
        return Double.valueOf(msgTTL);
    }

    private Double getBuffer(DTNHost h) {
        //mengambil data objek dari sisa buffer
        return Double.valueOf(h.getRouter().getFreeBufferSize());
    }

    private Double getResidualBuffer(DTNHost h) {
        Double bfAwal = Double.valueOf(h.getRouter().getBufferSize());
        Double bfAkhir = getBuffer(h);
        Double residualBuffer = bfAkhir / bfAwal;

        return residualBuffer;
    }

    private Double getResidualEnergy(DTNHost h) {
        Double eAwal = getInitialEnergy(h);
        Double eAkhir = getEnergy(h);
        Double residualEnergy = eAkhir / eAwal;
        return residualEnergy;
    }

    //ambil list duration
    public List<Duration> getListDuration(DTNHost nodes) {
        if (connHistory.containsKey(nodes)) {
            return connHistory.get(nodes);
        } else {
            List<Duration> d = new LinkedList<>();
            return d;
        }
    }

    private Double getClosenessOfNodes(DTNHost nodes) {
        double rataContactSeparation = getAverageContactOfNodes(nodes);
        double variansi = getVarianceOfNodes(nodes);

        Double c = Math.exp(-(Math.pow(rataContactSeparation, 2) / (2 * variansi)));
//        System.out.println(c);
        if (c.isNaN()) {
            c = 0.0;
        }
        return c;
    }

    //hitung nilai altruism
    public Double getAltruism(DTNHost h, Message m, DTNHost peer, Map<ListPastForwards_O, ListPastReceive_I> exChange) {
        //altruism (N,M) = type (M, o.m * thr (o.b) dijumlahkan dengan type(M, i.m * thr (i.b)
        Double altruism;
        altruism = thisHosts.getAddress() + peer.getAddress() * getEnergy(h);
        return null;
    }

    public boolean getAltruismValue(DTNHost h, Message m, DTNHost peer, Map<ListPastForwards_O, ListPastReceive_I> exChange) {
        return true;
        //cek apakah nilai altruism yang dihasilkan lebih dari treshold
        //if (altruism (peer,M) > threshold
        //        return true
        //maka forward message

//        getAltruism(peer, m) > getEnergy(peer);
    }

    protected boolean commumesWithHost(DTNHost dest) {
        return community.isHostInCommunity(dest);
    }

    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }

    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        BubbleRapSelfishNode de = getOtherDecisionEngine(otherHost);

        boolean peerInCommunity = de.commumesWithHost(dest); // apakh peer di dlm community nya dest
        boolean meInCommunity = this.commumesWithHost(dest); // apakh THIS ada di community nya dest

        if (peerInCommunity && !meInCommunity) // peer is in dest's community, but THIS is not
        {
            return true;
        } else if (!peerInCommunity && meInCommunity) // THIS is in dest'community, but peer is not
        {
            return false;
        } else if (peerInCommunity) // We're both in dest'community
        {
            // Forward to the one with the higher local centrality (in dest'community)

        }
        Double me = de.getEnergy(otherHost);
        Double peer = this.getEnergy(otherHost);
        System.out.println("me = " + me + " peer = " + peer);
// System.out.println(getInitialEnergy(dest));
//        return (me
        return true;
    }

    private double getAverageContactOfNodes(DTNHost nodes) {
        //untuk ambil rata-rata Shortes Separation Of Nodes
        List<Duration> list = getListDuration(nodes);
        Iterator<Duration> duration = list.iterator();
        double temp = 0;
        double mean = getAverageContactOfNodes(nodes);
        while (duration.hasNext()) {
            Duration d = duration.next();
            temp += Math.pow((d.end - d.start) - mean, 2);
        }
        return temp / list.size();
    }

    private double getVarianceOfNodes(DTNHost nodes) {
        //ambil variansinya dari node
        List<Duration> list = getListDuration(nodes);
        Iterator<Duration> duration = list.iterator();
        //berfungsi untuk penunjuk dalam list
        double hasil = 0;
        while (duration.hasNext()) {
            Duration d = duration.next();
            hasil += (d.end - d.start);
        }
        return hasil / list.size();

    }

    private Double ResidualResource(DTNHost h) {
        //hitung residual dari Resourch total
        Double Energy = getEnergy(h);
        Double eAwal = getInitialEnergy(h);
        Double Buffer = getBuffer(h);
        Double bAwal = Double.valueOf(h.getRouter().getBufferSize());
        //RESIDUAL :((rENERGY + rBUFFER)/R.TOTAL)
//        System.out.println(Energy);
//        System.out.println(eAwal);
//        System.out.println(Buffer);
//        System.out.println(bAwal);
        Double TResidual = (Energy / eAwal) + (Buffer / bAwal);
        return TResidual;
    }

    private Double ResidualTTL(DTNHost h) {
        //hitung residual TTL
        Double TTL = getTTL(h);
        Double tAwal = getInitialTTL();
//        rTTL = kTTL/aTTL;

        Double ResidualTTL = TTL / tAwal;
        return ResidualTTL;
    }

}
