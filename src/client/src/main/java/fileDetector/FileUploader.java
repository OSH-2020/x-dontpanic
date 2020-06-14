package fileDetector;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;

public class FileUploader {

    private static String serverIP = null;
    private static int serverPort = -1;
    private static File tmpFragmentFolder;

    private Socket toServer;
    private DataOutputStream outToServer = null;
    private DataInputStream inFromServer = null;

    public FileUploader() {
    }

    public static void init(File f, String ip, int port) {
        tmpFragmentFolder = f;
        serverIP = ip;
        serverPort = port;
    }

    public boolean checkFolders(String addr[]) {

        if (!createConnection())
            return false;

        try {
            // unable client id
            outToServer.writeBytes(String.format("6 %d %d\n", 0, addr.length));
            outToServer.flush();

            for (int i = 0; i < addr.length; i++) {
                int j = addr[i].lastIndexOf("/");
                // TODO:check index
                if (j == -1)
                    outToServer.writeBytes(
                            String.format("/ %s\n", addr[i]));
                else
                    outToServer.writeBytes(
                            String.format("%s/ %s\n", addr[i].substring(0, j), addr[i].substring(j + 1, addr[i].length())));
                outToServer.flush();
            }

            @SuppressWarnings("deprecation")
            String sentense = inFromServer.readLine();
            if (sentense.equals("received!"))
                return true;
            else
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }

    }

    public int registerFile(FileAttrs fa) {

        if (!createConnection())
            return -2;

        try {
            // unable client id
            outToServer.writeBytes(String.format("4 0 %s %s %s %d false\n",
                    fa.name, fa.path, fa.attr, fa.noa));
            outToServer.flush();

            @SuppressWarnings("deprecation")
            String sentense = inFromServer.readLine();

            String input[] = sentense.split(" ");
            if (!input[0].equals("FileId:"))
                return -2;
            return Integer.parseInt(input[1]);

        } catch (Exception e) {
            e.printStackTrace();
            return -2;
        } finally {
            closeConnection();
        }

    }

    @SuppressWarnings("deprecation")
    public boolean pushFragment(int fileId, int fragmentNum, int fragmentCount) {

        boolean status;
        String sentense;
        if (!createConnection())
            return false;

        try {
            File f = new File(tmpFragmentFolder, Integer.toString(
                    fileId * 100 + fragmentNum));
            if (!f.exists()) {
                errorHandler(1);
                return false;
            }

            outToServer.writeBytes(String.format("5 %d %d %d\n",
                    fileId, fragmentNum, fragmentCount));
            outToServer.flush();

            sentense = inFromServer.readLine();
            if (!sentense.equals("received!"))
                return false;

            status = connect.FileTransporter.sendFile(f, inFromServer, outToServer);

            if (status) {
                sentense = inFromServer.readLine();
                if (!sentense.equals("received!"))
                    status = false;
            }

            return status;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }

    }

    private boolean createConnection() {

        if (serverIP == null)
            return false;

        try {
            toServer = new Socket(serverIP, serverPort);
            toServer.setKeepAlive(true);
            toServer.setSoTimeout(5000);
            outToServer = new DataOutputStream(toServer.getOutputStream());
            inFromServer = new DataInputStream(new BufferedInputStream(toServer.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void closeConnection() {
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

    // handle fatal errors, finish it later
    private void errorHandler(int type) {
        // type = 1 can not find fragment
        return;
    }

}
