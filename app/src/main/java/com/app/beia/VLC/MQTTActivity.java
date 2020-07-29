package com.app.beia.VLC;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.patterns.io.mqttpatterns.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;



public class MQTTActivity extends AppCompatActivity implements MQTTConnectFragment.ConnectDataPassListener, MQTTSubscribeAndPublishFragment.PublishDataPassListener,  MqttCallback, MQTTSubscribeAndPublishFragment.SubscribeDataPassListener {

    private static final int PICKFILE_RESULT_CODE = 1;

    public String                   topicToPublish;
    public String                   topicToSubscribe;
    public String                   content;
    public String                   MQTTmessage;
    public String                   broker;
    public String                   port;
    public String                   clientId;
    public String                   messages[] = {"","","","",""};
    public String                   brokerURI;

    public int                      qos = 0;

    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;

    public MqttClient               client;
    public MqttConnectOptions       options;

    public MQTTConnectFragment      connectFragment;
    public MQTTSubscribeAndPublishFragment subscribeFragment;

    public Uri                      pathUri;
    public static int               uniqueID = 19149;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );

        // Set the layout and the toolbar
        setContentView(R.layout.detail_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Fragment manager later used to work with the changing fragments
        fragmentManager     = getSupportFragmentManager();

        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            //Set the title to display on the toolbar
            setTitle(getString(R.string.mqtt_connect));

            // Create an instance of MQTTConnectFragment
            MQTTSubscribeAndPublishFragment connectFragment = new MQTTSubscribeAndPublishFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            connectFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, connectFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Interface to launch an MQTTPublishFragment fragment

    public void launchPublishFragment(String[] messageParams) {

        // Set the title in the toolbar
        setTitle(getString(R.string.mqtt_publish));

        // Create instance of MQTTPublishFragment
        //publishFragment  = new MQTTPublishFragment();

        Bundle args = new Bundle();

        // Pass information fromt the variables in the activity to the fragment
        args.putString("topic",topicToPublish);
        args.putString("text", content);
        args.putInt   ("qos",qos);
       // publishFragment.setArguments(args);

        // Start transaction and commit
        fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.replace(R.id.fragment_container, publishFragment);
        fragmentTransaction.commit();
    }


    // Interface to launch an MQTTConnectFragment fragment
    public void launchConnectFragment(String data) {

        // Set the title in the toolbar
        setTitle(getString(R.string.mqtt_connect));

        // Create instance of MQTTConnectFragment
        connectFragment  = new MQTTConnectFragment();

        Bundle args = new Bundle();

        // Pass information from the variables in the activity to the fragment
        args.putString("broker", broker);
        args.putString("port",   port);
        args.putString("client", clientId);
        connectFragment.setArguments(args);

        // Start transaction and commit
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, connectFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void launchPublishFragment(String data) {
        // Set the title in the toolbar
        setTitle(getString(R.string.mqtt_publish));

        // Create instance of MQTTPublishFragment
        //publishFragment  = new MQTTPublishFragment();

        Bundle args = new Bundle();

        // Pass information fromt the variables in the activity to the fragment
        args.putString("topic",topicToPublish);
        args.putString("text", content);
        args.putInt   ("qos",qos);
        // publishFragment.setArguments(args);

        // Start transaction and commit
        fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.replace(R.id.fragment_container, publishFragment);
        fragmentTransaction.commit();

    }

    // Interface to launch an MQTTSubscribeFragment fragment
    public void launchSubscribeFragment(String data) {

        // Set the title in the toolbar
        setTitle(getString(R.string.mqtt_subscribe));

        // Create instance of MQTTSubscribeFragment
        subscribeFragment  = new MQTTSubscribeAndPublishFragment();

        Bundle args = new Bundle();

        // Pass information from the variables in the activity to the fragment
        args.putString("topic", topicToSubscribe);
        args.putStringArray("messages", messages);
        subscribeFragment.setArguments(args);

        // Start transaction and commit
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, subscribeFragment);
        fragmentTransaction.commit();
    }


    // Interface to call the construction of MQTT client from fragments.
    public void createMQTTClient(String connectParams[]){

        // This method is called from  MQTTConnectFragment and it passes an array of
        // strings with the information gathered from the GUI to create an MQQT client
        MQTTClientClass mqttClient = new MQTTClientClass();
        mqttClient.execute(connectParams);
    }

    // Interface to publish an MQTT message to topicToPublish.
    public void publishMQTTmessage(String publishParams[]) {

        // This method is called from  MQTTPublishFragment and it passes an array of
        // strings with the information gathered from the GUI to create an MQQT message
        MQTTClientClass mqttClient = new MQTTClientClass();
        mqttClient.execute(publishParams);
    }

    // Interface to subscribe the MQTT client to a given Topic
    public void subscribeMQTTtopic(String subscribeParams[]){

        try {
            // Declare callback function to be called when a reception event occurs
            client.setCallback(this);
            // Subscribe to the topic given by the MQTTSubscribeFragment
            client.subscribe(subscribeParams[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Nu se poate face abonarea ", Toast.LENGTH_SHORT).show();

        }
        Toast.makeText(getApplicationContext(), "Abonare Topic " + subscribeParams[1] + " realizată", Toast.LENGTH_SHORT).show();
    }


    // Method to start a default file browser specific to each device.
    public void findFile(){

        // Create intent
        Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
        fileintent.setType("*/*");

        try {
            // Start the activity that returns to onActivityResult
            startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
        }
    }


    // This method is called after the startActivityForResult method is invoked.
     @Override
     public  void onActivityResult(int requestCode, int resultCode, Intent data) {
         // TODO Fix no activity available
         if (data == null)
             return;
         switch (requestCode) {
             case PICKFILE_RESULT_CODE:
                 if (resultCode == RESULT_OK) {
                     // The data intent returns an URI with getData
                     pathUri = data.getData();
                 }
         }
     }


    // This has to be implemented in order for the subscribe callback to be declared
    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(), "Conexiune pierdută", Toast.LENGTH_SHORT).show();

    }

    // This has to be implemented in order for the subscribe callback to be declared
    // When the message arrives, the GUI is updated, and the data is stored, 5 items max.
    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {

        // Update local variable for topic and received message
        MQTTmessage     = message.toString();
        topicToSubscribe = topic;

        // Shift the data in the array containing the received messages
        for(int i = 4; i >= 1; i--){
            messages[i] =  messages[i-1];
        }
        messages[0] =  " " + message;

        String notification_message = String.valueOf(message);
        char[] ch = notification_message.toCharArray();
        String temp_aer = String.valueOf(ch[19]) + String.valueOf(ch[20]);
        int temp_aer_int = Integer.parseInt(temp_aer);
        String umid_aer = String.valueOf(ch[38]) + String.valueOf(ch[39]);
        int umid_aer_int = Integer.parseInt(umid_aer);
        String pres_atm = String.valueOf(ch[56]) + String.valueOf(ch[57]) + String.valueOf(ch[58]);
        int pres_atm_int = Integer.parseInt(pres_atm);
        String umid_sol = String.valueOf(ch[76]) + String.valueOf(ch[77]);
        int umid_sol_int = Integer.parseInt(umid_sol);
        String temp_sol = String.valueOf(ch[97]) + String.valueOf(ch[98]);
        int temp_sol_int = Integer.parseInt(temp_sol);


        ///notification alert
        //Notification not = new Notification();
        //not.notificationAlert(0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notification.setSmallIcon(R.drawable.vlc1);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("VLC");
        notification.setLights(Color.RED,2000,1000);
        notification.setSound(alarmSound);


        Intent intent = new Intent(this, MQTTActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        String mesaj_notificare = "Date senzori primite";
        notification.setContentIntent(pendingIntent);
        notification.setContentText(mesaj_notificare);
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(mesaj_notificare));
        notificationManager.notify(uniqueID, notification.build());




        // Updating the GUI has to be done in the main Thread, since here we are in a side thread,
        // it is necessary to call runOnUiThread to do so
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    // Update the GUI only if we are in the Subscribe Fragment, otherwise we cannot reach the Views
                    if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof MQTTSubscribeAndPublishFragment) {
                        // Call updateList from the MQTTSubscribeFragment to be able to see the Views and variable to update
                        // TODO: check if the the instance of MQTTSubscribeFragment that was previously created can be used here...
                        MQTTSubscribeAndPublishFragment fragment_obj = (MQTTSubscribeAndPublishFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        fragment_obj.updateList(messages);
                    }
                } catch (Exception e) {
                    Log.d("Error", "" + e);
                }
            }
        });
    }

    // This has to be implemented in order for the subscribe callback to be declared
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /*
    The AsyncTask is called with <Params, Progress, Result>
    This class contains all the Paho MQTT functionality
    */
    public class MQTTClientClass extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... paramString) {

            // The same Async function will be called from different fragments, keeping the unity
            // of the implementation. For this, the first string of the passed parameters is checked
            // for matches with the cases to process
            switch (paramString[0]){

                // If called from MQTTConnectFragment
                case "connect":

                    // Retrieve the information from the arguments into the local variables
                    broker                        = paramString[1];
                    port                          = paramString[2];
                    brokerURI                     = paramString[3];
                    clientId                      = paramString[4];

                    // Add memory persistence to the client
                    MemoryPersistence persistence = new MemoryPersistence();

                    try {
                        // Create client with the given URI, ID and persistence, add options and session type
                        client  = new MqttClient(brokerURI, clientId, persistence);
                        options = new MqttConnectOptions();
                        options.setCleanSession(true);
                        options.setConnectionTimeout(60);
                        options.setKeepAliveInterval(60);
                        options.setUserName("vlc");
                        options.setPassword("ZZBndNAGqjdSj2NU".toCharArray());


                        // Connect to the server
                        System.out.println("Conectare la broker: " + broker);
                        client.connect(options);
                        System.out.println("Conecatat");

                        return paramString;

                    } catch(MqttException me) {
                        // TODO: Rid these debugging prints
                        System.out.println("reason "+me.getReasonCode());
                        System.out.println("msg "   +me.getMessage());
                        System.out.println("loc "   +me.getLocalizedMessage());
                        System.out.println("cause " +me.getCause());
                        System.out.println("excep " + me);
                        me.printStackTrace();
                    } catch (Exception e) {
                        Log.d("Things Flow I/O", "Error " + e);
                        e.printStackTrace();
                    }
                    break;

                // If called from MQTTPublishFragment
                case "publish":

                    // Retrieve the information from the arguments into the local variables
                    content         = paramString[1];
                    topicToPublish  = paramString[2];
                    qos             = Integer.parseInt(paramString[3]);

                    // Create the message to send and set the Quality of Service
                    // TODO: Rid these debugging prints
                    System.out.println("Publishing message: " + content);
                    MqttMessage message = new MqttMessage(content.getBytes());
                    message.setQos(qos);

                    try {
                        // Publish the msessage
                        client.publish(topicToPublish, message);
                        System.out.println("Message published");
                        // TODO: Rid these debugging prints
                    } catch (MqttException e) {
                        e.printStackTrace();
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return paramString;


                // If called from MQTTSubscribeFragment
                case "subscribe":
                    //TODO: Subscription extra actions, nothing so far
                    break;
            }
            return null;
        }

        // To do after the Async task has finished
        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            // If execution of the task was successful, the result will not be null
            if(result != null){
                switch (result[0]){
                    case "connect":
                        Log.d("Connect", "just connected");
                        Toast.makeText(getApplicationContext(), "Conectare realizată", Toast.LENGTH_SHORT).show();
                        break;

                    case "publish":
                        Log.d("Publish", "just published");
                        Toast.makeText(getApplicationContext(), "Published " + content + " on Topic " + topicToPublish, Toast.LENGTH_SHORT).show();
                        break;


                    case "subscribe":
                        Toast.makeText(getApplicationContext(), "Abonare " + content+ " Topic " + topicToPublish + "realizată", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Nu se poate realiza acțiunea, verifică conexiunea ", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
