package net.platyrhynchos.nolofreepieclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_start = this.findViewById(R.id.btn_start);
        final TextView statusText1 = this.findViewById(R.id.statusText1);
        final TextView statusText2 = this.findViewById(R.id.statusText2);
        final EditText serverAddressText = this.findViewById(R.id.txt_serveraddress);
        final EditText clientPortText = this.findViewById(R.id.txt_clientport);
        final EditText waitText = this.findViewById(R.id.txt_wait);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle bundle = intent.getExtras();
                        String message = bundle.getString("message");

                        switch (intent.getAction()) {
                            case "SHOW_STATUS1_ACTION":
                                statusText1.setText(message);
                                break;
                            case "SHOW_STATUS2_ACTION":
                                statusText2.setText(message);
                                break;
                        }
                    }
                };

                String serverAddress = serverAddressText.getText().toString();
                String serverIp = "192.168.2.210";
                int serverPort = 5678;

                if(serverAddress!=null){
                    String[] array = serverAddress.split(":");
                    //TODO バリデーション
                    serverIp = array[0];
                    if(array.length>=2){
                        serverPort = Integer.parseInt(array[1]);
                    }
                }

                int clientPort = Integer.parseInt(clientPortText.getText().toString());
                int trackingWait = Integer.parseInt(waitText.getText().toString());

                IntentFilter intentFilter1 = new IntentFilter();
                intentFilter1.addAction("SHOW_STATUS1_ACTION");
                intentFilter1.addAction("SHOW_STATUS2_ACTION");
                registerReceiver(receiver, intentFilter1);

                Intent intent = new Intent(getApplication(), NoloDataTrackerService.class);
                intent.putExtra("SERVER_IP", serverIp);
                intent.putExtra("SERVER_PORT", serverPort);
                intent.putExtra("CLIENT_PORT", clientPort);
                intent.putExtra("TRACKING_WAIT", trackingWait);

                startService(intent);
            }
        });

        Button btn_exit = this.findViewById(R.id.btn_stop);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), NoloDataTrackerService.class);
                stopService(intent);
            }
        });
    }

}
