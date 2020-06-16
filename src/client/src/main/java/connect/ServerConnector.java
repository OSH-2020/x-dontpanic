package connect;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerConnector extends Thread {

    private static String serverIP;
    private static int controlPort;

    private Socket toServer;
    private int clientId;
    private boolean connecting = true;
    private client.SynItem syn;

    public ServerConnector(int cId, client.SynItem s) {
        clientId = cId;
        syn = s;
    }

    public static void init(String sIp, int cPort) {
        serverIP = sIp;
        controlPort = cPort;
    }

    @Override
    public void run() {
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;
        boolean status = true;
        String input;

        while (connecting) {
            try {
                toServer = new Socket(serverIP, controlPort);
                outToServer = new DataOutputStream(toServer.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
                System.out.println("Connect to server successfully(control)!");
            } catch (Exception e) {
                e.printStackTrace();
                status = false;
                try {
                    outToServer.close();
                } catch (Exception ex) {
                    // TODO: handle exception
                }
                try {
                    inFromServer.close();
                } catch (Exception ex) {
                    // TODO: handle exception
                }
                try {
                    toServer.close();
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            }

            if (!status) {
                break;
            }

            while (connecting) {
                try {
                    outToServer.writeBytes(String.format("1 %d %d\n", clientId, client.Client.getRS()));
                    outToServer.flush();
                    input = inFromServer.readLine();

                    //debug
                    System.out.println(input);
					/*
					int unreadRequest = Integer.parseInt(str[2]);
					while (unreadRequest > 0) {
						outToServer.writeBytes(String.format("2 %d\n", clientId));
						outToServer.flush();
						input = inFromServer.readLine();
						str = input.split(" ");
						int requestId = Integer.parseInt(str[0]);
						int fragmentId = Integer.parseInt(str[1]);
						int type = Integer.parseInt(str[2]);
						FragmentManager fManager = new FragmentManager(requestId, fragmentId, type);
						fManager.submit();
						unreadRequest--;
					}*/

                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            try {
                outToServer.writeBytes("exit\n");
                outToServer.flush();
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                outToServer.close();
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                inFromServer.close();
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                toServer.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        if (connecting) {
            syn.setStatus(1);
            System.out.println("ERR: connect to server has been interrupted!");
        }
    }

    public void stopConnect() {
        connecting = false;
    }

}
