package com.loli;

import connect.RequestManager;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 9998;
        //WebSocket.init(port);
        //System.out.println("WebSocket Server has started on 127.0.0.1:" + port + ".\r\nWaiting for a connection...");
        connect.FragmentManager.init(new File("/home/ubuntu/client_test"));
        RequestManager requestManager = new RequestManager(port);
        requestManager.start();
        try {
            requestManager.join();
        } catch (Exception e) {
            System.out.println(e);
        }
        //server.close();
    }
}
