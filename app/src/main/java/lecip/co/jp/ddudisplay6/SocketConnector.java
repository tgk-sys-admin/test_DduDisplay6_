package lecip.co.jp.ddudisplay6;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class SocketConnector {
    private static final String TAG = "SocketConnector";

    public static final int OBC_DISCONNECT = 0;
    public static final int OBC_PROCESSING = 1;
    public static final int OBC_CONNECT = 2;
    private int mConnectStatus = OBC_DISCONNECT;

    private DatagramSocket sendSocket = null;   //UDP送信用ソケット
    private UdpRecive udpRecive = null;         //UDP受信用ソケット

    public void startSocketConnection(int cmd) {
        Log.d(TAG, "startSocketConnection : s");
        Log.d(TAG, "cmd : "+ cmd);
        switch (cmd){
            // ステータス情報コマンド送信
            case DduConst.CMD_STATUSINFO:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(null == sendSocket) {
                            try {
                                sendSocket = new DatagramSocket();
                            } catch (SocketException e) {
                                e.printStackTrace();
                            }
                        }
                        InetAddress inetAddress = null;//送信先
                        try {
                            inetAddress = InetAddress.getByName(DduConst.OBC_IPADDR_FRONT);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                        byte[] senddata = new byte[29];

//                        int index = 0;
//                        senddata[index++] = 0x02;
//
//                        senddata[index++] = 0;
//                        senddata[index++] = 23;
//
//                        byte sumdata = (byte) (senddata[1] + senddata[2]);
//                        byte sizesum = (byte) (1 + ~sumdata);
//                        senddata[index++] = sizesum;
//
//                        sumdata = 0;
//                        for (int i = 0; i < 23; i++) {
//                            senddata[index++] = 0;
//                        }
//                        byte checksum = (byte) (1 + ~sumdata);
//                        senddata[index++] = checksum;
//                        senddata[index++] = 0x03;

                        senddata = DduSecCommand.getCommand_StatusInfo();

                        Log.d(TAG, "senddata data : " + Util.bin2hex(senddata));

                        DatagramPacket packet = new DatagramPacket(senddata, senddata.length, inetAddress, DduConst.OBC_PORTNUM_FRONT);//IPアドレス、ポート番号も指定
                        try {
                            sendSocket.send(packet);//送信
                            MainActivity.TxTextSet(":"+ Util.bin2hex(senddata));
                            // 送信成功時に受信クラス生成
                            try {
                                udpRecive= new UdpRecive(DduConst.DDU_IPADDR, DduConst.DDU_PORTNUM_FRONT);
                                udpRecive.receiveStart();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            MainActivity.TxTextSet("Exception : "+ e.getMessage());
                        }
                    }
                }).start();
                break;

            // 経路設定コマンド送信
            case DduConst.CMD_ROUTELINE:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(null == sendSocket) {
                            try {
                                sendSocket = new DatagramSocket();
                            } catch (SocketException e) {
                                e.printStackTrace();
                            }
                        }
                        InetAddress inetAddress = null;//送信先
                        try {
                            inetAddress = InetAddress.getByName(DduConst.OBC_IPADDR_FRONT);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        byte[] senddata = new byte[13];

//                        int index = 0;
//
//                        senddata[index++] = 0x02;
//                        senddata[index++] = 0;
//                        senddata[index++] = 7;
//
//                        byte sumdata = (byte) (senddata[1] + senddata[2]);
//                        byte sizesum = (byte) (1 + ~sumdata);
//                        senddata[index++] = sizesum;
//
//                        senddata[index++] = 2; // コマンド番号
//
//                        senddata[index++] = 0; // 変更理由 //TODO
//                        senddata[index++] = 0; // 往復
//                        senddata[index++] = 0; // 系統番号(3byte) //TODO
//                        senddata[index++] = 0; // 系統番号(3byte) //TODO
//                        senddata[index++] = 0; // 系統番号(3byte) //TODO
//                        senddata[index++] = 0; // 停留所連番号 //TODO
//
//                        sumdata = 0;
//                        byte checksum = (byte) (1 + ~sumdata);
//                        senddata[index++] = checksum;
//                        senddata[index++] = 0x03;

//                        Log.d(TAG,"!!!!!!checksum : "+ checksum);

                        senddata = DduSecCommand.getCommand_KeitoInfo();

                        Log.d(TAG, "senddata data : " + Util.bin2hex(senddata));

                        DatagramPacket packet = new DatagramPacket(senddata, senddata.length, inetAddress, DduConst.OBC_PORTNUM_FRONT);//IPアドレス、ポート番号も指定
                        try {
                            sendSocket.send(packet);//送信
                            MainActivity.TxTextSet(":"+ Util.bin2hex(senddata));
                            Log.d(TAG,"!!!!Send!!!!!");
                            // 送信成功時に受信クラス生成
                            try {
                                udpRecive= new UdpRecive(DduConst.DDU_IPADDR, DduConst.DDU_PORTNUM_FRONT);
                                udpRecive.receiveStart();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            MainActivity.TxTextSet("Exception : "+ e.getMessage());
                        }
                    }
                }).start();
                break;

            case DduConst.CMD_TELOPCODE:
            case DduConst.CMD_TIMEINFO:
            case DduConst.CMD_VERSIONINFO:
            default:
                Log.d(TAG, "Do Nothing");
                break;
        }
    }

    public void stopSocketConnection() {
        Log.d(TAG, "closeAll S");
        Log.d(TAG, "!!!!!sendSocket : "+ sendSocket);
        // 送信ソケットクローズ
        if(null != sendSocket){
            sendSocket.close();
            sendSocket = null;
            MainActivity.TxTextSet("Close");
        }
        Log.d(TAG, "!!!!!udpRecive : "+ udpRecive);
        if(null != udpRecive) {
            // 受信ソケットクローズ
            udpRecive.receiveStopt();
            udpRecive = null;
        }
        MainActivity.RxTextSet("Close");
    }
//
//    private void modetoConnect() {
//        if (mIsconnectedObc) {
//            Log.i(TAG, "!!!!!All Port Connected. OBC_CONNECT start.");
//            // 接続成功 通信可能状態に移行
//            mConnectStatus = OBC_CONNECT;
//        }
//    }

}
