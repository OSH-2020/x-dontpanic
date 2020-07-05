package WebSocket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocket {
    private static ServerSocket server;
    private Socket client;
    private InputStream in;
    private OutputStream out;

    public static void init(int port) throws IOException {
        System.out.println(port);
        server = new ServerSocket(port);
    }

    public WebSocket() {
        try {
            client = server.accept();
            accept();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        client.close();
    }


    public void sendPong() throws IOException {
        System.out.println("send a pong ");
        byte[] head = createHead(0, 0xA);
        out.write(head, 0, head.length);
    }

    public byte[] catchBytes(int len) throws IOException {
        byte[] data = new byte[10000000];
        in.read(data, 0, len);
        return data;
    }

    public byte[] recv() throws IOException {
        byte[] head = new byte[14];
        byte[] data = new byte[10000000];
        int total_len = 0;
        while (true) {
            in.read(head, 0, 2);
            boolean fin = head[0] < 0;
            int opcode = head[0] & 0xf;
            /*
            1 text
            2 bin
            8 close
            9 ping
            A pong
             */
            boolean mask = head[1] < 0;//TODO throw
            int payload_len = head[1] & 0x7f;
            if (payload_len == 0x7e) {
                in.read(head, 2, 2);
                payload_len = ((head[2]) & 0xff) << 8 | ((head[3]) & 0xff);
                //System.out.println("126");
                //System.out.println(payload_len);
            } else if (payload_len == 0x7f) {
                in.read(head, 2, 8);
                payload_len = ((head[6]) & 0xff) << 24 | ((head[7]) & 0xff) << 16 | ((head[8]) & 0xff) << 8 | ((head[9]) & 0xff);
                //System.out.println("127");
                //System.out.println(payload_len);
            }
            byte[] key = new byte[4];
            in.read(key, 0, 4);
            byte[] encoded = new byte[payload_len];
            int recv_len = in.read(encoded, 0, payload_len);

            System.out.println(String.format("opcode is %d, payload len is %d", opcode, payload_len));
            if (opcode == 0x9) {
                sendPong();
                continue;
            }
            if (opcode == 0x8) {
                System.out.println("closed!");
                return new byte[0];
            }

            byte[] decoded = new byte[payload_len];
            for (int i = 0; i < encoded.length; i++) {
                decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
            }
            System.arraycopy(decoded, 0, data, total_len, payload_len);
            total_len += payload_len;
            if (fin) break;
        }
        return Arrays.copyOf(data, total_len);
    }

    public void recvFile(File f) throws IOException {
        OutputStream os = new FileOutputStream((f));
        byte[] recv_bytes = recv();
        System.out.println(String.format("recieve file length :%d", recv_bytes.length));
        os.write(recv_bytes);
    }

    //TODO slip big file

    public byte[] createHead(int len, int opcode) {
        if (len > 0xffff) {
            return new byte[]{
                    (byte) (0x80 | (opcode & 0xf)),
                    (byte) 0x7f,
                    0, 0, 0, 0,
                    (byte) ((len >> 24) & 0xFF),
                    (byte) ((len >> 16) & 0xFF),
                    (byte) ((len >> 8) & 0xFF),
                    (byte) (len & 0xFF)
            };
        } else if (len > 0x7d) {
            return new byte[]{
                    (byte) (0x80 | (opcode & 0xf)),
                    (byte) 0x7e,
                    (byte) ((len >> 8) & 0xFF),
                    (byte) (len & 0xFF)
            };
        } else {
            return new byte[]{
                    (byte) (0x80 | (opcode & 0xf)),
                    (byte) (len & 0x7F)
            };
        }
    }
    public void sendText(byte[] payload) throws IOException {
        System.out.println(String.format("send text: %s", new String(payload)));
        byte[] head = createHead((payload.length), 1);
        out.write(head, 0, head.length);
        out.write(payload, 0, payload.length);
    }

    public void sendBin(byte[] payload) throws IOException {
        System.out.println(String.format("send binary length: %d", (payload.length)));
        byte[] head = createHead((payload.length), 2);
        out.write(head, 0, head.length);
        out.write(payload, 0, payload.length);
    }

    public void sendMessage(String msg) throws IOException {
        sendText(msg.getBytes());
    }

    public void sendFile(File f) throws IOException {
        sendBin(Files.readAllBytes(f.toPath()));
    }

    public byte[] echo() throws IOException {
        //echo
        byte[] msg_byte = recv();
        System.out.println(msg_byte);
        sendText(msg_byte);
        return msg_byte;
    }

    public void accept() throws IOException, NoSuchAlgorithmException {
        try {
            in = client.getInputStream();
            out = client.getOutputStream();
            Scanner s = new Scanner(in, "UTF-8");
            try {
                String data = s.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);
                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                            + "\r\n\r\n").getBytes("UTF-8");
                    out.write(response, 0, response.length);

                }
            } finally {
                //s.close();
            }
        } finally {
        }
    }
}