import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HTTPResponse {

    private String version = "HTTP/1.1";
    private int resultCode = -1;
    private String resultCodeText = "";
    private long fileSize = -1;
    private String fileName_ = null;
    private  String fileType_ = null;
    private FileInputStream fileStream = null;

    public HTTPResponse(OutputStream streamOut, HTTPRequest listener) throws NoSuchAlgorithmException {

        // get file to send from http request
        fileName_ = listener.getFileName();

        // insert default page if necessary
        if (fileName_.charAt(fileName_.length() - 1) == '/') {
            fileName_ = "/index.html";
        }

        // get file extension
        String fileExtension = "";
        int i = fileName_.lastIndexOf('.');
        if (i > 0) {
            fileExtension = fileName_.substring(i+1);
        }

        // generate content-type based on file extension
        if (fileName_.endsWith("html") || fileName_.endsWith("css")){
            fileType_ = "text/" + fileExtension;
        }
        else if (fileName_.endsWith("js")){
            fileType_ = "text/javascript";
        }
        else if (fileName_.endsWith("jpg") || fileName_.endsWith("jpeg")){
            fileType_ = "image/jpeg";
        }
        else if (fileName_.endsWith("png") || fileName_.endsWith("gif")){
            fileType_ = "image/" + fileExtension;
        }

        // add directory structure to path
        File webFile = new File("Resources" + fileName_);

        // check if websocket request
        if (listener.isWebSocket()){
            createWSHandshake(streamOut, listener);
        }
        else {
            // if http request handle file found vs 404
            try {
                fileStream = new FileInputStream(webFile);
                createValidHeader(webFile);
            } catch (FileNotFoundException e) {
                createInvalidHeader();
            }
            responsePrinter(streamOut);
        }


    }

    private void createWSHandshake(OutputStream streamOut, HTTPRequest listener) throws NoSuchAlgorithmException {
        // get websocket key from request header and generate the key for the response header
        String requestKey = listener.getHeaderMapValue("Sec-WebSocket-Key");
        String responseKey = generateResponseKey(requestKey);

        // print out websocket response header
        PrintWriter streamPrinter = new PrintWriter(streamOut);

        streamPrinter.print("HTTP/1.1 101 Switching Protocols\r\n");
        streamPrinter.print("Upgrade: websocket\r\n");
        streamPrinter.print("Connection: Upgrade\r\n");
        streamPrinter.print("Sec-WebSocket-Accept: " + responseKey + "\r\n");
        streamPrinter.print("\r\n");    // empty line required at end of header
        streamPrinter.flush();

    }

    private String generateResponseKey(String requestKey) throws NoSuchAlgorithmException {
        // generate response key for response websocket header

        String provided = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        requestKey += provided;
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        byte[] hashed = md.digest( requestKey.getBytes() );
        String result = Base64.getEncoder().encodeToString( hashed );

        return result;
    }


    private void createValidHeader(File webFile) {
        // assign appropriate variables for a response to a valid http request
        resultCode = 200;               // Success
        resultCodeText = "OK";
        fileSize = webFile.length();

    }

    private void createInvalidHeader(){
        // assign appropriate variables for a response to an invalid http request
        // send 404 page if requested file not found
        File fnfFile = new File("Resources/404.html");

        try {
            fileStream = new FileInputStream(fnfFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        resultCode = 404;
        resultCodeText = "File Not Found";
        fileSize = fnfFile.length();

    }

    private void responsePrinter(OutputStream streamOut) {
        //send out the header and file of the http response
        PrintWriter streamPrinter = new PrintWriter(streamOut);

        streamPrinter.print(version + " " + resultCode + " " + resultCodeText + "\r\n");
        streamPrinter.print("Server: Localhost:8080\r\n");
        streamPrinter.print("Content-Type: " + fileType_ + "\r\n");
        streamPrinter.print("Content-Length: " + fileSize + "\r\n");
        streamPrinter.print("\r\n");
        streamPrinter.flush();

        try {
            fileStream.transferTo(streamOut);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO exception at transferTo");
        }

    }


}


