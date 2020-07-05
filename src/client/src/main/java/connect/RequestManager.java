package connect;

import WebSocket.WebSocket;

import java.io.IOException;

public class RequestManager extends Thread {
    static int selfDataPort;

    public RequestManager(int port) throws IOException {
        WebSocket.init(port);
        selfDataPort = port;
    }

    @Override
    public void run() {
        // TODO 线程池
        System.out.println("WebSocket Server has started on 127.0.0.1:" + selfDataPort + ".\r\nWaiting for a connection...");
        while (true) {
            try {
                WebSocket user = null;
                user = new WebSocket();
                System.out.println("A user connected.");

                System.out.println(user);
                FragmentManager fragmentManager = new FragmentManager(user);
                fragmentManager.start();

                //String msg = new String(user.echo());
                //System.out.println(msg);
                /*
                try {
                    //user.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                 */

            } finally {
                //System.out.println("A user disconnected.");
            }

        }
    }
}
