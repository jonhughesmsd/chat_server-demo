import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {


    public static void main(String[] args) throws IOException {

        boolean done = false;

        // test server socket connection
        try{
            ServerSocket testSocket = new ServerSocket(8080);
            testSocket.close();
        }
        catch(Exception e){
            System.out.println("Could not open socket:8080");
            done = true;
        }

        // initialize web server
        ServerSocket localServerSocket = new ServerSocket(8080);

        // forever loop listening for http requests
        while (!done) {
            try {
                Socket localSocket = localServerSocket.accept();

                HTTPRunner runner = new HTTPRunner(localSocket);

                // create a new thread for each connection
                Thread t = new Thread(runner);
                t.start();

            }
            catch(Exception e){
                System.out.println("Uh oh! Something has gone wrong somewhere");
                e.printStackTrace();
            }
        }
    }
}


