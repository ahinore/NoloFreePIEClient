package net.platyrhynchos.nolofreepieclient;

import android.app.Service;

import android.content.Intent;
import android.os.IBinder;

import com.watchdata.usbhostconn.UsbCustomTransfer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NoloDataTrackerService extends Service{

    private boolean threadActive = true;
    private Thread trackingThread;
    private Thread hapticThread;

    private enum CurrentState{
          CHECK_USB
        , INIT_NETWORK
        , TRACK_DATA
    }

    UsbCustomTransfer usb = null;
    int usbState = 0;

    private String serverIp;
    private int serverPort;
    private int clientPort;
    private int trackingWait;

    private Runnable trackingTask = new Runnable(){

        DatagramChannel channel;
        InetSocketAddress serverInetAddress;

        @Override
        public void run() {
            CurrentState state = CurrentState.CHECK_USB;

            exit:
            while (threadActive) {

                switch(state){
                    case CHECK_USB:
                        postMessage("SHOW_STATUS1_ACTION", "CHECK_USB");
                        this.waitMillis(1000);
                        if(checkUsb()){
                            state = CurrentState.INIT_NETWORK;
                        }
                        break;

                    //Network接続確認
                    case INIT_NETWORK:
                        postMessage("SHOW_STATUS1_ACTION","INIT_NETWORK");
                        if(!checkUsb()){
                            state = CurrentState.CHECK_USB;
                            break;
                        }
                        if(!this.initNetwork()){
                            break exit;
                        }
                        state = CurrentState.TRACK_DATA;
                        postMessage("SHOW_STATUS1_ACTION","TRACKING");
                        break;

                    case TRACK_DATA:
                        this.waitMillis(trackingWait);

                        if(!checkUsb()){
                            state = CurrentState.CHECK_USB;
                        }

                        this.trackData(usb);
                        break;
                }
            }

            try {
                if(channel!= null) {
                    channel.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            postMessage("SHOW_STATUS1_ACTION","Tracking Thread stopped");
        }

        private boolean searchServer(){
            //TODO ブロードキャストを使ってサーバを検出しIPアドレスを取得する
            return true;
        }

        private boolean sendPacket(final String text){
            byte[] data = text.getBytes();
            try {

                ByteBuffer buf = ByteBuffer.allocate(data.length);
                buf.clear();
                buf.put(data);
                buf.flip();

                channel.send(buf, serverInetAddress);

            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean checkUsb(){
            return usbState == 1;
        }

        private boolean initNetwork(){
            try {
                channel = DatagramChannel.open();
                channel.socket().bind(new InetSocketAddress(clientPort+1));
                serverInetAddress = new InetSocketAddress(serverIp, serverPort);

            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean trackData(UsbCustomTransfer usb){
            String data = String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%d,%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%d,%d,%f,%f"
                    //HMD
                    , usb.getPoseByDeviceType(0).getPos().getX()
                    , usb.getPoseByDeviceType(0).getPos().getY()
                    , usb.getPoseByDeviceType(0).getPos().getZ()
                    , usb.getPoseByDeviceType(0).getNolo_Quaternion().getX()
                    , usb.getPoseByDeviceType(0).getNolo_Quaternion().getY()
                    , usb.getPoseByDeviceType(0).getNolo_Quaternion().getZ()
                    , usb.getPoseByDeviceType(0).getNolo_Quaternion().getW()
                    , usb.getPoseByDeviceType(0).getVecAngularVelocity().getX()
                    , usb.getPoseByDeviceType(0).getVecAngularVelocity().getY()
                    , usb.getPoseByDeviceType(0).getVecAngularVelocity().getZ()
                    , usb.getPoseByDeviceType(0).getVecVelocity().getX()
                    , usb.getPoseByDeviceType(0).getVecVelocity().getY()
                    , usb.getPoseByDeviceType(0).getVecVelocity().getZ()
                    //Left
                    , usb.getPoseByDeviceType(1).getPos().getX()
                    , usb.getPoseByDeviceType(1).getPos().getY()
                    , usb.getPoseByDeviceType(1).getPos().getZ()
                    , usb.getPoseByDeviceType(1).getNolo_Quaternion().getX()
                    , usb.getPoseByDeviceType(1).getNolo_Quaternion().getY()
                    , usb.getPoseByDeviceType(1).getNolo_Quaternion().getZ()
                    , usb.getPoseByDeviceType(1).getNolo_Quaternion().getW()
                    , usb.getPoseByDeviceType(1).getVecAngularVelocity().getX()
                    , usb.getPoseByDeviceType(1).getVecAngularVelocity().getY()
                    , usb.getPoseByDeviceType(1).getVecAngularVelocity().getZ()
                    , usb.getPoseByDeviceType(1).getVecVelocity().getX()
                    , usb.getPoseByDeviceType(1).getVecVelocity().getY()
                    , usb.getPoseByDeviceType(1).getVecVelocity().getZ()
                    , usb.getControllerStatesByDeviceType(1).getButtons()
                    , usb.getControllerStatesByDeviceType(1).getTouches()
                    , usb.getControllerStatesByDeviceType(1).getTouchpadAxis().getX()
                    , usb.getControllerStatesByDeviceType(1).getTouchpadAxis().getY()
                    //Right
                    , usb.getPoseByDeviceType(2).getPos().getX()
                    , usb.getPoseByDeviceType(2).getPos().getY()
                    , usb.getPoseByDeviceType(2).getPos().getZ()
                    , usb.getPoseByDeviceType(2).getNolo_Quaternion().getX()
                    , usb.getPoseByDeviceType(2).getNolo_Quaternion().getY()
                    , usb.getPoseByDeviceType(2).getNolo_Quaternion().getZ()
                    , usb.getPoseByDeviceType(2).getNolo_Quaternion().getW()
                    , usb.getPoseByDeviceType(2).getVecAngularVelocity().getX()
                    , usb.getPoseByDeviceType(2).getVecAngularVelocity().getY()
                    , usb.getPoseByDeviceType(2).getVecAngularVelocity().getZ()
                    , usb.getPoseByDeviceType(2).getVecVelocity().getX()
                    , usb.getPoseByDeviceType(2).getVecVelocity().getY()
                    , usb.getPoseByDeviceType(2).getVecVelocity().getZ()
                    , usb.getControllerStatesByDeviceType(2).getButtons()
                    , usb.getControllerStatesByDeviceType(2).getTouches()
                    , usb.getControllerStatesByDeviceType(2).getTouchpadAxis().getX()
                    , usb.getControllerStatesByDeviceType(2).getTouchpadAxis().getY()
            );

            return this.sendPacket(data);
        }

        private void waitMillis(int millis){
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable hapticTask = new Runnable(){
        DatagramChannel channel;

        @Override
        public void run() {
            CurrentState state = CurrentState.CHECK_USB;

            exit:
            while (threadActive) {

                switch(state){
                    case CHECK_USB:
                        postMessage("SHOW_STATUS2_ACTION","CHECK_USB");
                        this.waitMillis(1000);
                        if(checkUsb()){
                            state = CurrentState.INIT_NETWORK;
                        }
                        break;

                    //Network接続確認
                    case INIT_NETWORK:
                        postMessage("SHOW_STATUS2_ACTION","INIT_NETWORK");
                        if(!checkUsb()){
                            state = CurrentState.CHECK_USB;
                            break;
                        }
                        if(!this.initNetwork()){
                            break exit;
                        }
                        state = CurrentState.TRACK_DATA;
                        postMessage("SHOW_STATUS2_ACTION","TRACKING");
                        break;

                    case TRACK_DATA:

                        if(!checkUsb()){
                            state = CurrentState.CHECK_USB;
                        }

                        this.trackData();
                        break;

                }
            }

            try {
                if(channel!= null) {
                    channel.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            postMessage("SHOW_STATUS2_ACTION","Haptic Thread stopped");
        }

        private boolean checkUsb(){
            return usbState == 1;
        }

        private boolean initNetwork(){
            try {
                channel = DatagramChannel.open();
                channel.socket().bind(new InetSocketAddress(clientPort));

            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean trackData() {
            ByteBuffer buf = ByteBuffer.allocate(453);
            buf.clear();

            try {
                channel.receive(buf);

                buf.flip();
                byte[] data = new byte[buf.limit()];
                buf.get(data);

                String s = new String(data);

                String[] hdata = s.split(",");
                if(hdata.length != 2) {
                    throw new Exception("Incorrect Haptic Data");
                }

                int leftPulse = Integer.parseInt(hdata[0]);
                if(leftPulse!=0) {
                    usb.triggerHapticPulse(1, leftPulse);
                }

                int rightPulse = Integer.parseInt(hdata[1]);
                if(rightPulse!=0){
                    usb.triggerHapticPulse(2, rightPulse);
                }

            }catch(Exception e){
                e.printStackTrace();
                postMessage("SHOW_STATUS2_ACTION",e.getMessage());
                return false;
            }
            return true;
        }

        private void waitMillis(int millis){
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    };

    private void postMessage(String action, final String text){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("message", text);
        getBaseContext().sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent){
        this.postMessage("SHOW_STATUS1_ACTION", "Service bound");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        super.onStartCommand(intent, flags, startId);

        this.serverIp = intent.getStringExtra("SERVER_IP");
        this.serverPort = intent.getIntExtra("SERVER_PORT", 5678);
        this.trackingWait = intent.getIntExtra("TRACKING_WAIT", 0);
        this.clientPort = intent.getIntExtra("CLIENT_PORT", 5678);

        this.postMessage("SHOW_STATUS1_ACTION","onStartCommand");

        this.trackingThread = new Thread(null, trackingTask,"TrackingService");
        this.trackingThread.start();

        this.hapticThread = new Thread(null, hapticTask,"HapticService");
        this.hapticThread.start();


        return  START_NOT_STICKY;
    }

    private UsbCustomTransfer initUsb(){

        UsbCustomTransfer usb = UsbCustomTransfer.getInstance(NoloDataTrackerService.this.getApplicationContext());
        usb.setConnectedStatusCallback(new UsbCustomTransfer.ConnectedStatusCallback() {
            @Override
            public void setUsbDeviceConnState(int mstate) {
                usbState = mstate;
            }
        });

        usb.usb_init();
        return usb;
    }

    @Override
    public void onCreate(){
        this.postMessage("SHOW_STATUS1_ACTION","Service Started");
        this.usb = initUsb();
        super.onCreate();
    }

    public void onDestroy(){
        this.postMessage("SHOW_STATUS1_ACTION","Service Stopped");

        this.threadActive =false;
        this.trackingThread.interrupt();
        this.hapticThread.interrupt();

        if (usb != null && usbState != 0) {
            usb.usb_finish();
        }

        super.onDestroy();
    }
}
