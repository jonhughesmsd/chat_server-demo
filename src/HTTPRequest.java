import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class HTTPRequest {


    private String cmd_ = null;
    private String fileName_ = null;
    private String prot_ = null;
    private String fileType_ = null;
    private HashMap headerMap;
    private boolean isWebSocket_ = false;


    public HTTPRequest(InputStream streamIn) {

        Scanner streamReader = new Scanner(streamIn);
        // read in first line of http request header
        cmd_ = streamReader.next();
//        System.out.println("Cmd: " + cmd_);
        fileName_ = streamReader.next();
//        System.out.println("Filename: " + fileName_);
        prot_ = streamReader.next();
//        System.out.println("Prot: " + prot_);

        streamReader.nextLine();

        readToHeaderMap(streamReader);

        // check if websocket request
        if (hasHeaderMapKey("Sec-WebSocket-Key")){
            isWebSocket_ = true;
        }

    }

    private void readToHeaderMap(Scanner streamReader) {
        headerMap = new HashMap();

        // read remainder of request header and write to map
        while (streamReader.hasNextLine()) {

            String line = streamReader.nextLine();
            System.out.println(line);
            if (line.isEmpty()){
                break;
            }

            // split line at first ": "
            String[] headerLinePieces = line.split(": ", 2);

            String key = headerLinePieces[0];
            String value = headerLinePieces[1];

            headerMap.put(key, value);
        }
    }

    public String getCmd() {
        return cmd_;
    }

    public String getFileName() {
        return fileName_;
    }

    public String getProt() {
        return prot_;
    }

    public String getHeaderMapValue(String key){
        return (String) headerMap.get(key);
    }

    public boolean hasHeaderMapKey(String key){
        return headerMap.containsKey(key);
    }

    public boolean isWebSocket() {
        return isWebSocket_;
    }
}

