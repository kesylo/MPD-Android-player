package be.keming.mpdplayer;

import android.app.Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

public class GlobalClass {

    public static String IP = "172.30.40.33";
    public static int port = 6600;
    public static Socket mySocket = new Socket();
    public static PrintWriter printer;
    public static BufferedReader reader;

    public static String getIP() {
        return IP;
    }

    public static void setIP(String IP) {
        GlobalClass.IP = IP;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        GlobalClass.port = port;
    }
}