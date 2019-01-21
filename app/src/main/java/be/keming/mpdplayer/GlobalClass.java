package be.keming.mpdplayer;

import android.app.Application;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

public class GlobalClass {

    // public static String IP = "172.30.40.33";
    // public static int port = 6600;
    public static String IP = "10.128.8.220";
    public static int port = 9900;
    public static Socket mySocket = new Socket();
    public static PrintWriter printer;
    public static BufferedReader bufreader;
    public static DataOutputStream outToServer;








}