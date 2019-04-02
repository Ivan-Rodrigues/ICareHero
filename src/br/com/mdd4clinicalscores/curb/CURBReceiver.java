package br.com.mdd4clinicalscores.curb;

import br.com.mdd4clinicalscores.model.NotificationHero;
import com.google.gson.Gson;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CURBReceiver implements NodeConnectionListener, UDIDataReaderListener<ApplicationObject> {

    private static String       gatewayIP  = "127.0.0.1";
    private static int          gatewayPort  = 5500;
    private MrUdpNodeConnection connection;
    private UUID                myUUID;
    private static SddlLayer    core;
    private List<NotificationHero> listNotification;
    private String confusion;
    private int BUN;
    private int age;

    public CURBReceiver() {
        listNotification = new ArrayList<>();
        core = UniversalDDSLayerFactory.getInstance(UniversalDDSLayerFactory.SupportedDDSVendors.OpenSplice);
        core.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);
        core.createPublisher();
        core.createSubscriber();

        Object receiveMessageTopic = core.createTopic(lac.cnet.sddl.objects.Message.class, lac.cnet.sddl.objects.Message.class.getSimpleName());
        core.createDataReader(this, receiveMessageTopic);

        Object toMobileNodeTopic = core.createTopic(PrivateMessage.class, PrivateMessage.class.getSimpleName());
        core.createDataWriter(toMobileNodeTopic);

        myUUID = UUID.fromString("788b2b22-baa6-4c61-b1bb-01cff1f5f878");
        InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
        try {
            connection = new MrUdpNodeConnection(myUUID);
            connection.addNodeConnectionListener(this);
            connection.connect(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.INFO);

        new CURBReceiver();
    }

    public void connected(NodeConnection remoteCon) {
        ApplicationMessage message = new ApplicationMessage();
        message.setContentObject("Registering");

        try {
            connection.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //sendMepaQuery(UUID.fromString("9d65e1eb-cc3b-4dce-8c4f-e771c1db438b"));

    }

    public void newMessageReceived(NodeConnection remoteCon, Message message) {
        //System.out.println("Receiver Node!");

        Gson g = new Gson();
        NotificationHero notificationHero = g.fromJson(message.getContentObject().toString(),NotificationHero.class);
        try {
            switch (notificationHero.getSensorName()) {

                case "zephyrbloodpressure":
                    if (listNotification.size() == 0) {
                        listNotification.add(notificationHero);
                    } else if (listNotification.size() > 0) {
                        listNotification.set(0, notificationHero);
                    }
                    break;
                case "zephyrrespiratory":
                    if (listNotification.size() == 1) {
                        listNotification.add(notificationHero);
                    } else if (listNotification.size() > 1) {
                        listNotification.set(1, notificationHero);
                    }
            }
        }catch (Exception e){
            //e.printStackTrace();
        }

        if (listNotification != null && listNotification.size() >=2){
            int score = 0;
            for (NotificationHero not : listNotification){
                score += Double.parseDouble(not.getScore());
                System.out.println(not.getSensorName() +" - "+ not.getValue()+" "+not.getUnityMeasuremente()
                        +" - "+not.getObsRule()+" - Score: "+not.getScore());
            }
            //calcula escore da idade
            setAge();
            if (age >= 65){
                score +=1;
            }
            System.out.println("Age: "+age+" - Score: "+(age>65? 1:0)+"");

            //calcula escore BUR
            setBUR();
            if (BUN > 19){
                score +=1;
            }
            System.out.println("BUN: "+BUN+"mg/dL - Score: "+(BUN>19? 1:0)+"");

            setConfusion();
            if (confusion.equals("YES")){
                score += 1;
            }
            System.out.println("Confusion: "+confusion+" - Score: "+(confusion.equals("YES")? 1:0)+"");
            System.out.println("------------------------ Result -----------------------------");
            if (score ==0 || score ==1){
                System.out.println("Score: "+score+"\nRisk: 1.5% mortality\nDisposition: Outpatient care\n");
            }else if (score==2){
                System.out.println("Score: "+score+"\nInpatient vs. observation admission\nDisposition: Outpatient care\n");
            }else{
                System.out.println("Score: "+score+"\n22% mortality\nInpatient admission with consideration for ICU admission with score of 4 or 5\n");
            }

            // System.exit(0);
        }
    }

    private void setAge(){
        Random random = new Random();
        int aux = random.nextInt(2);
        if (aux ==0){
            this.age = 54;
        }else{
            this.age = 69;
        }
    }

    private void setBUR(){
        Random random = new Random();
        int aux = random.nextInt(2);
        if (aux==0){
            this.BUN = 14;
        }else{
            this.BUN = 22;
        }
    }

    private void setConfusion(){
        Random random = new Random();
        int aux = random.nextInt(2);
        if (aux==0){
            this.confusion = "YES";
        }else{
            this.confusion = "NO";
        }
    }







    public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}

    public void disconnected(NodeConnection remoteCon) {}

    public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}

    public void internalException(NodeConnection remoteCon, Exception e) {}






    @Override
    public void onNewData(ApplicationObject applicationObject) {
        lac.cnet.sddl.objects.Message message = (lac.cnet.sddl.objects.Message) applicationObject;
        String content = new String( message.getContent() );
        JSONParser parser = new JSONParser();
        Gson gson = new Gson();
        try {
            JSONObject object = (JSONObject) parser.parse( content );

            //System.out.println(gson.toJson(message));
            String tag = (String) object.get( "tag" );

            switch( tag ) {
                case "SensorData":
                    break;

                case "EventData":
                    final String label = (String) object.get( "label" );
                    final String data  = (String) object.get( "data" );
                    //   System.out.println( label + " - "+ data );
                    break;

                case "ReplyData":
                case "ErrorData":

                    break;
            }
        } catch( Exception ex ) {
            System.out.println( ex.getMessage() );
        }
    }




}