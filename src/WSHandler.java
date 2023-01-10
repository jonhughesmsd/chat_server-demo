import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class WSHandler {

    private String firstWord_ = "";
    private String restOfMessage_ = "";
    private HTTPRunner runner_;

    // Constructor
    public WSHandler(DataInputStream wsIn, HTTPRunner runner) throws Exception {

        runner_ = runner;

        // Read incoming frame, parse header and first word of payload
        readFrameIn(wsIn);

        // Parse remainder of payload and generate the JSON string response
        parseFrameIn();

    }

    private void readFrameIn(DataInputStream wsIn) throws Exception {

        // read in and parse first two bytes of header
        byte[] header =  wsIn.readNBytes( 2 );

        boolean isFin = (header[0] & 0x80) != 0;
        byte opCode = (byte) (header[0] & 0x0F);
        boolean hasMask = (header[1] & 0x80) != 0;
        int payloadLength = (header[1] & 0x7F);

        // handle OpCodes other than 1
        if (opCode == 8){
            throw new Exception("End Thread");
        }
        else if (opCode != 1){
            throw new Exception("Unexpected OpCode");
        }

        // read in (extended payload length) and mask bytes
        byte[] mask;
        ArrayList<Integer> extPayloadLength = null;

        if (payloadLength <= 125){
            mask = wsIn.readNBytes(4);
        }
        else if (payloadLength == 126){
            payloadLength = wsIn.readShort();
            mask = wsIn.readNBytes(4);
        }
        else {
            extPayloadLength = new ArrayList<>();
            extPayloadLength.add(wsIn.readInt());
            extPayloadLength.add(wsIn.readInt());
            mask = wsIn.readNBytes(4);

            System.out.println("Payload length is a long! It's too long to deal with right now");
            System.exit(-1);
        }

        // read in the remainder of the frame (i.e. the encoded payload)
        byte[] encoded = wsIn.readNBytes(payloadLength);

        // decode the payload and write to String
        String decodedStr = "";
        for (int i=0; i<payloadLength; i++){
            decodedStr += (char)(encoded[i] ^ mask[i % 4]);
        }

        // split the now decoded string into its first word (i.e. command) and the rest of the payload
        if (decodedStr.equals("leave")){
            firstWord_ = "leave";
            restOfMessage_ = "";
        }
        else {
            String arr[] = decodedStr.split(" ", 2);
            firstWord_ = arr[0];
            restOfMessage_ = arr[1];
        }
    }

    private void parseFrameIn() throws Exception {


        if (firstWord_.equals("join")){

            // further split the payload to get the room name and the username set from the user
            String arr[] = restOfMessage_.split(" ", 2);
            String roomName = arr[0];
            String userName = arr[1];

            // set the username and password in the runnable
            runner_.setUserName(userName);
            runner_.setRoomName(roomName);

            // get socket from the runnable
            Socket clientSocket = runner_.getClientSocket();

            // get the chat room
            Room chatRoom = Room.getRoom(roomName);
            chatRoom.addUser(clientSocket, userName);

            // get list of users in room as a comma-separated list
            String listOfNames = chatRoom.getListOfNames();

            //construct join JSON string
            String outputPayload = "{\"cmd\":\"join\",\"roomname\":\"" + roomName +
                                    "\",\"username\":\"" + userName +
                                    "\",\"userlist\":\"" + listOfNames + "\"}";
            long outputPLL = outputPayload.length();

            // generate header for payload
            byte[] header = chatRoom.writeFrameOut(outputPLL);
            chatRoom.sendToAll(header, outputPayload);

            // read each line out of a chatlog file and send it to the user who is joining the room
            String filename = "ChatLogs/" + roomName + ".txt";
            try {
                Scanner logReader = new Scanner(new FileInputStream(filename));
                while (logReader.hasNextLine()) {
                    outputPayload = logReader.nextLine();
                    outputPLL = outputPayload.length();
                    byte[] joinHeader = chatRoom.writeFrameOut(outputPLL);
                    chatRoom.sendToOne(joinHeader, outputPayload, clientSocket);
                }
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }


        }
        else if (firstWord_.equals("leave")){

            // get username and room name from runnable
            String userName = runner_.getUserName();
            String roomName = runner_.getRoomName();

            Room chatRoom = Room.getRoom(roomName);

            // remove username from array of usernames in room
            chatRoom.removeUserName(userName);

            // get the updated list of usernames in room
            String listOfNames = chatRoom.getListOfNames();

            // construct the JSON string to be sent to everyone in the room
            String outputPayload = "{\"cmd\":\"leave\",\"username\":\"" + userName +
                                    "\",\"userlist\":\"" + listOfNames + "\"}";
            long outputPLL = outputPayload.length();

            // generate header for outgoing frame and send
            byte[] header = chatRoom.writeFrameOut(outputPLL);
            chatRoom.sendToAll(header, outputPayload);

            // remove the user's socket from the room and reset the runnable's room name
            chatRoom.removeUserSocket(runner_.getClientSocket());
            runner_.setRoomName("");

        }
        else if (firstWord_.equals("msg")) {

            // parse the remainder of the incoming string and get the message contents and the timestamp
            String arr[] = restOfMessage_.split(" ", 2);
            String timestamp = arr[0];
            String message = arr[1];

            // get the username and room name from the runnable
            String userName = runner_.getUserName();
            String roomName = runner_.getRoomName();

            // construct the outgoing JSON string
            String outputPayload = "{\"cmd\":\"msg\",\"username\":\"" + userName +
                                    "\",\"message\":\"" + message +
                                    "\",\"timestamp\":\"" + timestamp + "\"}";
            long outputPLL = outputPayload.length();

            // get the chat room
            Room chatRoom = Room.getRoom(roomName);

            // generate the outgoing header and send
            byte[] header = chatRoom.writeFrameOut(outputPLL);
            chatRoom.sendToAll(header, outputPayload);

            // write the outgoing JSON string to a text file
            String filepath = "Chatlogs/" + roomName + ".txt";
            FileWriter fw = new FileWriter(filepath, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(outputPayload);
            pw.flush();
            pw.close();
        }
    }
}
