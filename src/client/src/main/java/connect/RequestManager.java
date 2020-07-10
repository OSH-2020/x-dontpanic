package connect;

import WebSocket.WebSocket;

import java.io.IOException;

public class RequestManager extends Thread {
    static int selfDataPort;
    private String selfIp;

    public RequestManager(int port, String selfIp) throws IOException {
        WebSocket.init(port);
        selfDataPort = port;
        this.selfIp = selfIp;
    }

    @Override
    public void run() {
        // TODO 线程池
        System.out.println("WebSocket Server has started on " + selfIp + ":" + selfDataPort + ".\r\nWaiting for a connection...");
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
