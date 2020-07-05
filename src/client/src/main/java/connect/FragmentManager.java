package connect;

import WebSocket.WebSocket;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;

public class FragmentManager extends Thread {

    private static File fragmentFolder = null;
    private static String serverIP = null;
    private static int serverPort = -1;
    private static int selfPort = -1;

    private Socket toServer;
    private WebSocket user;
    private DataOutputStream outToServer = null;
    private DataInputStream inFromServer = null;
    private int requestId, fragmentId, type;

    public FragmentManager(int rId, int fId, int t) {
        requestId = rId;
        fragmentId = fId;
        type = t;
    }

    public FragmentManager(WebSocket iUser) {
        user = iUser;
    }

    @Override
    public void run() {
        try {

            String msg = new String(user.recv());
            System.out.println(msg);
            //TODO token
            if (msg.equals("U")) {
                System.out.println("Upload");
                String fileName = new String(user.recv());
                System.out.println(fileName);
                recvDigest(fileName);
                recvFragment(fileName);
                /*
                byte b1,b2;
                b1=user.catchBytes(1)[0];
                b2=user.catchBytes(1)[0];
                System.out.print(b1);
                System.out.print(',');
                System.out.print(b2);
                System.out.print(',');
                while(b1!=(byte)0x81||b2!=(byte)0xA0){
                    b1=b2;
                    b2=user.catchBytes(1)[0];
                    System.out.print(b2);
                    System.out.print(',');
                }*/
            } else if (msg.equals(("D"))) {
                System.out.println("Download");
                String fileName = new String(user.recv());
                System.out.println(fileName);
                sendFragment(fileName);
                sendDigest(fileName);
            } else if (msg.equals(("E"))) {
                System.out.println("Echo");
                user.echo();
            } else {
                System.out.println("Undefined operation");
            }
            user.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    /*
    public boolean submit() {
        boolean status = true;

        if (serverIP == null)
            return false;

        try {
            toServer = new Socket(serverIP, serverPort);
            toServer.setKeepAlive(true);
            toServer.setSoTimeout(5000);
            outToServer = new DataOutputStream(toServer.getOutputStream());
            inFromServer = new DataInputStream(new BufferedInputStream(toServer.getInputStream()));
            System.out.println("Connect to server successfully(data)!");
            if (type == 1)
                status = sendFragment();
            else if (type == 2)
                status = recvFragment();
            else if (type == 3)
                status = deleteFragment();
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
        } finally {
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
        return status;
    }

     */

    public static void init(File f) throws IOException {
        fragmentFolder = f;
        //selfPort = port;
    }

    private boolean sendFragment(String fileName) throws Exception {
        boolean status;
        String sentense;

        //File f = new File(fragmentFolder, Integer.toString(fragmentId));
        File f = new File(fragmentFolder, fileName);
        if (!f.exists()) {
            errorHandler(1);
            return false;
        }

        /*outToServer.writeBytes(String.format("%d %d %d\n", type, requestId, fragmentId));
        //outToServer.flush();


        sentense = inFromServer.readLine();
        if (!sentense.equals("received!"))
            return false;

        status = FileTransporter.sendFile(f, inFromServer, outToServer);

        if (status) {
            sentense = inFromServer.readLine();
            if (!sentense.equals("received!"))
                status = false;
        }*/
        user.sendFile(f);
        status = true;//TODO
        return status;

    }

    private boolean recvFragment(String fileName) throws Exception {

        //File f = new File(fragmentFolder, Integer.toString(fragmentId));
        File f = new File(fragmentFolder, fileName);
        if (f.exists()) {
            f.delete();
        }

        /*
        outToServer.writeBytes(String.format("%d %d %d\n", type, requestId, fragmentId));
        outToServer.flush();

        if (FileTransporter.recvFile(f, inFromServer, outToServer)) {
            outToServer.writeBytes("received!\n");
            outToServer.flush();
            return true;
        } else {
            return false;
        }*/
        user.recvFile(f);
        user.sendMessage("fragment success");
        System.out.println(String.format("recvFragment %s", fileName));
        return true;//TODO
    }

    private boolean sendDigest(String fileName) throws Exception {
        boolean status;
        String sentense;

        //File f = new File(fragmentFolder, Integer.toString(fragmentId));
        File f = new File(fragmentFolder, fileName + ".digest");
        if (!f.exists()) {
            errorHandler(1);
            return false;
        }
        user.sendMessage(new String(Files.readAllBytes(f.toPath())));
        return true;

    }

    private boolean recvDigest(String fileName) throws Exception {

        //File f = new File(fragmentFolder, Integer.toString(fragmentId));
        File f = new File(fragmentFolder, fileName + ".digest");
        if (f.exists()) {
            f.delete();
        }
        OutputStream os = new FileOutputStream((f));
        byte[] recv_bytes = user.recv();
        System.out.println(String.format("recvDigest : %s", new String(recv_bytes)));
        os.write(recv_bytes);
        user.sendMessage("digest success");
        return true;//TODO
    }

    private boolean deleteFragment() throws Exception {

        File f = new File(fragmentFolder, Integer.toString(fragmentId));
        if (f.exists()) {
            f.delete();
        }

        /*
        outToServer.writeBytes(String.format("%d %d %d\n", type, requestId, fragmentId));
        outToServer.flush();

        @SuppressWarnings("deprecation")
        String sentense = inFromServer.readLine();
        if (sentense.equals("received!"))
            return true;
        else
            return true;

         */
        return true;//TODO
    }

    // handle fatal errors, finish it later
    private void errorHandler(int type) {
        // type = 1 can not find fragment
        return;
    }

}
