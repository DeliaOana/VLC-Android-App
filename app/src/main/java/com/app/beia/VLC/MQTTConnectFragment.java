package com.app.beia.VLC;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.fragment.app.Fragment;

import com.patterns.io.mqttpatterns.R;

public class MQTTConnectFragment extends Fragment {

    public String                   brokerString = "mqtt.beia-telemetrie.ro";
    public String                   portString = "1883";
    public String                   clientString = "SampleClient";
    public String                   protocol;
    public String                   filePath;

    public boolean                  TLS = false;


    public Button                   fabConnectToBroker;
    public Button                   fabSubscribe;


    public ConnectDataPassListener mCallback;

    public MQTTConnectFragment() {
        // Required empty public constructor
    }

    // Interface of the functions from the parent Activity that this Fragment will call
    public interface ConnectDataPassListener {
        void launchPublishFragment(String data);
        void launchSubscribeFragment(String data);
        void createMQTTClient(String connectParams[]);
        void findFile();
    }

    //
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        // An Activity is needed to create the interface callback, so it is cast from the context
        // This is due to the onAttach method with Activity instead of context has ben deprecated
        if (context instanceof Activity) {
            activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mCallback = (ConnectDataPassListener) activity;

            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement ConnectDataPassListener");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(R.menu.devicefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {

            // Create a dialog that pups up with information about the application
            final Dialog dialog = new Dialog(getActivity());

            dialog.setContentView(R.layout.about_layout);
            dialog.setTitle(R.string.aboutpatterns);

            Button btnCancel = (Button) dialog.findViewById(R.id.dismiss);
            dialog.show();

            btnCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

        fabConnectToBroker  = (Button) rootView.findViewById(R.id.fabConnectToBroker);
        fabSubscribe        = (Button) rootView.findViewById(R.id.fabSubscribe);


        fabConnectToBroker  .setOnClickListener(onClickListenerMQTT);
        fabSubscribe        .setOnClickListener(onClickListenerMQTT);

        return rootView;
    }

    /*
     This is called when landing here from another fragment (through the parent Activity)
     Therefore, the values are extracted of the arguments that have been passed onto here
     to have consistency in the UI values and update them as needed
     */
    @Override
    public void onStart() {
        super.onStart();

    }

    // onClickListener for all Views. The action if filtered by the name of the View
    private View.OnClickListener onClickListenerMQTT = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            switch (v.getId()) {

                case R.id.fabConnectToBroker:

                    String URIbroker;
                    URIbroker = "tcp://" + brokerString + ":" + portString;
                    protocol = "tcp";


                    // Bundle the parameters, and call the parent Activity method to start the connection
                    String connectParams[] = {"connect", brokerString, portString,
                            URIbroker, clientString, protocol, filePath};
                    mCallback.createMQTTClient(connectParams);

                    break;

                case R.id.fabSubscribe:
                    // Change to the Subscribe fragment, through the parent Activity interface
                    mCallback.launchSubscribeFragment("");
                    break;

            }
        }
    };
}



