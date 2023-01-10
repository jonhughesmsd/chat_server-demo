import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class HTTPRunner implements Runnable {

    private final Socket clientSocket_;
    private String userName_ = "";
    private boolean nameIsSet_ = false;
    private String roomName_ = "";

    // Constructor
    public HTTPRunner(Socket clientSocket) {
       clientSocket_ = clientSocket;
    }

    // Setter for chat usernames
    public void setUserName(String userName){
        if (!nameIsSet_) {
            userName_ = userName;
        }
    }

    // Setter for chat room names
    public void setRoomName(String roomName){
        roomName_ = roomName;
    }

    // Getter for chat usernames
    public String getUserName(){
        return userName_;
    }

    //Getter for chat room names
    public String getRoomName(){
        return roomName_;
    }

    // Getter for the chat room socket connection
    public Socket getClientSocket(){
        return clientSocket_;
    }

    @Override
    public void run() {

        try {
            // handle HTTP request
            InputStream streamIn = clientSocket_.getInputStream();
            HTTPRequest listener = new HTTPRequest(streamIn);

            // send HTTP response
            OutputStream streamOut = clientSocket_.getOutputStream();
            HTTPResponse responder = new HTTPResponse(streamOut, listener);

            // handle if connection is a websocket request
            if (listener.isWebSocket()) {

                // forever loop listening for ws messages
                while (true){
                    DataInputStream wsIn = new DataInputStream( clientSocket_.getInputStream() );
                    try {
                        WSHandler wsHandler = new WSHandler(wsIn,this);
                    }
                    catch (Exception e){
                        //remove user from room
                        System.out.println(e.getMessage());
                        System.out.println("Exception Thrown: Client Closed");
                        break;
                    }
                }
            }

            // close socket after HTTP response is sent
            clientSocket_.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

