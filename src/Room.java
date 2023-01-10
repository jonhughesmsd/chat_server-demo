import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Room {

    public static HashMap rooms_ = new HashMap();
    private ArrayList<Socket> users_;
    private ArrayList<String> userNames_;
    private String roomName_ = "";

    // Constructor
    private Room(String roomName) {
        users_ = new ArrayList<>();
        userNames_ = new ArrayList<>();
        roomName_ = roomName;
    }

    public synchronized static Room getRoom(String roomName) {

        Room r = (Room) rooms_.get(roomName);

        // if room exists, return room. if not, create room and return it
        if (r != null) {
            return r;
        } else {
            Room newRoom = new Room(roomName);
            rooms_.put(roomName, newRoom);
            return newRoom;
        }
    }

    public synchronized void addUser(Socket clientSocket, String userName) {
        users_.add(clientSocket);
        userNames_.add(userName);
    }

    public void removeUserName(String userName) {
        userNames_.remove(userName);
    }

    public void removeUserSocket(Socket clientSocket) {
        users_.remove(clientSocket);
    }


    public synchronized byte[] writeFrameOut(long outputPLL) throws Exception {

        // generate header for outgoing frame
        byte[] header;

        // byte 0 is always the same if sending a text message
        byte b0 = (byte) 0x81;    // 10000001
        byte b1;

        // based on payload length, generate byte array of correct length and assign byte 1
        if (outputPLL <= 125) {
            header = new byte[2];
            b1 = (byte) outputPLL;    // 0??????? (0-125)
        } else if (outputPLL < Short.MAX_VALUE * 2) {
            header = new byte[4];
            b1 = 126;
        } else {
            header = new byte[10];
            b1 = 127;
        }

        // begin populating header byte array
        header[0] = b0;
        header[1] = b1;

        // handle cases of longer payloads
        if (header.length == 4) {
            byte b2 = (byte) (outputPLL >>> 8);
            byte b3 = (byte) outputPLL;
            header[2] = b2;
            header[3] = b3;
        } else if (header.length == 10) {
            System.out.println("Payload is currently too big to deal with");
            throw new Exception("File Too Large");
        }

        return header;
    }

    public void sendToAll(byte[] header, String outputPayload) throws IOException {
        // send message to all users in the room
        for (Socket userSocket : users_) {
            OutputStream streamOut = userSocket.getOutputStream();

            //send header
            for (byte b : header) {          // send header bytes
                streamOut.write(b);
            }
            streamOut.flush();

            //send payload
            PrintWriter streamPrinter = new PrintWriter(streamOut);

            for (int i = 0; i < outputPayload.length(); i++) {
                streamPrinter.print(outputPayload.charAt(i));
            }
            streamPrinter.flush();
        }
    }

    public void sendToOne(byte[] header, String outputPayload, Socket clientSocket) throws IOException {

        OutputStream streamOut = clientSocket.getOutputStream();

        //send header
        for (byte b : header) {          // send header bytes
            streamOut.write(b);
        }
        streamOut.flush();

        // send payload
        PrintWriter streamPrinter = new PrintWriter(streamOut);

        for (int i = 0; i < outputPayload.length(); i++) {
            streamPrinter.print(outputPayload.charAt(i));
        }
        streamPrinter.flush();

    }

    // generate a comma-separated list of usernames of users in the room
    public String getListOfNames() {
        String listOfNames = "";
        Collections.sort(userNames_);
        for (int i = 0; i < userNames_.size(); i++) {
            listOfNames += userNames_.get(i);
            if (i < userNames_.size() - 1) {
                listOfNames += ",";
            }
        }
        return listOfNames;
    }


}
