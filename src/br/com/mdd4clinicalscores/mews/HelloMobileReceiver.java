package br.com.mdd4clinicalscores.mews;

import br.com.mdd4clinicalscores.model.AVPU;
import br.com.mdd4clinicalscores.model.NotificationHero;
import com.google.gson.Gson;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol;
import lac.cnclib.sddl.message.Message;
import lac.cnclib.sddl.serialization.Serialization;
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

public class HelloMobileReceiver implements NodeConnectionListener, UDIDataReaderListener<ApplicationObject> {

    private static String       gatewayIP  = "127.0.0.1";
    private static int          gatewayPort  = 5500;
    private MrUdpNodeConnection connection;
    private UUID                myUUID;
    private static SddlLayer    core;
    private List<NotificationHero> listNotification;

    public HelloMobileReceiver() {
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

        new HelloMobileReceiver();
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
                case "zephyrbpm":
                    if (listNotification.size() == 1) {
                        listNotification.add(notificationHero);
                    } else if (listNotification.size() > 1) {
                        listNotification.set(1, notificationHero);
                    }
                    break;
                case "zephyrrespiratory":
                    if (listNotification.size() == 2) {
                        listNotification.add(notificationHero);
                    } else if (listNotification.size() > 2) {
                        listNotification.set(2, notificationHero);
                    }
                    break;
                case "zephyrtemperature":
                    if (listNotification.size() == 3) {
                        listNotification.add(notificationHero);
                    } else if (listNotification.size() > 0) {
                        listNotification.set(3, notificationHero);
                    }
            }
        }catch (Exception e){
            //e.printStackTrace();
        }

        if (listNotification != null && listNotification.size() >=4){
          int score = 0;
          AVPU avpu;
            for (NotificationHero not : listNotification){
                score += Double.parseDouble(not.getScore());
                System.out.println(not.getSensorName() +" - "+ not.getValue()+" "+not.getUnityMeasuremente()
                        +" - "+not.getObsRule()+" - Score: "+not.getScore());
            }
            avpu = getAVPU();

            System.out.println("AVPU Score: "+avpu.getValue()+" - "+avpu.getScore());
            score = score+avpu.getScore();

            if (score <=4){
                System.out.println("Evaluation: Not linked to death or ICU admission   Result: "+score+"\n");
            }else{
                System.out.println("Statistically linked to death or ICU admission   Result: "+score+"\n");
            }

            System.exit(0);
        }
    }

    private AVPU getAVPU(){
        AVPU avpu;
        Random random = new Random();
        int aux = random.nextInt(4);

        switch (aux){
            case 0:  avpu = new AVPU("Alert", 0);
                break;
            case 1: avpu = new AVPU("Reacts to voice",1);
                break;
            case 2: avpu = new AVPU("Reacts to pain",2);
                break;
            default:
                avpu = new AVPU("Unresponsive", 3);
        }

        return avpu;
    }

    public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}

    public void disconnected(NodeConnection remoteCon) {}

    public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}

    public void internalException(NodeConnection remoteCon, Exception e) {}


    private void sendMepaQuery(UUID nodeDest)
    {
        // Send the message
        ApplicationMessage appMsg = new ApplicationMessage();
        appMsg.setPayloadType( ClientLibProtocol.PayloadSerialization.JSON );
       /* appMsg.setContentObject( "[{ \"MEPAQuery\": { \"type\":\"add\", " +
                "\"label\":\"AVGTemp\", \"object\":\"event\", " +
                "\"rule\":\"SELECT * FROM SensorData WHERE sensorName='zephyrbpm'\", \"target\":\"global\" } }]" );
       */
       appMsg.setContentObject("[{\"MEPAQuery\":{\"type\":\"add\",\"label\":\"ObsRuleBPMHigh\"," +
               "\"object\":\"event\",rule\":\"SELECT (sensorValue[0]) as value FROM SensorData (sensorName='zephyrbpm') WHERE sensorValue[0] >= 100.0\",\"target\":\"global\"}}]");

        sendUnicastMSG(appMsg, nodeDest );
    }

    public void sendUnicastMSG(ApplicationMessage appMsg, UUID nodeID ) {
        PrivateMessage privateMSG = new PrivateMessage();
        privateMSG.setGatewayId( UniversalDDSLayerFactory.BROADCAST_ID );
        privateMSG.setNodeId( nodeID );
        privateMSG.setMessage( Serialization.toProtocolMessage( appMsg ) );

        sendCoreMSG( privateMSG );

    //    System.out.println("MEPA query sent: "+ appMsg.getContentObject().toString());
    }

    private  void sendCoreMSG( PrivateMessage privateMSG ) {
        core.writeTopic( PrivateMessage.class.getSimpleName(), privateMSG );
    }

    @Override
    public void onNewData(ApplicationObject applicationObject) {
        lac.cnet.sddl.objects.Message message = (lac.cnet.sddl.objects.Message) applicationObject;
        String content = new String( message.getContent() );
        JSONParser parser = new JSONParser();
        Gson gson = new Gson();
        try {
            JSONObject object = (JSONObject) parser.parse( content );

           // System.out.println(gson.toJson(message));
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