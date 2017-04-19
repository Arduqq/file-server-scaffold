import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import javax.activation.MimetypesFileTypeMap;
import java.util.Scanner;

/**
 * Request handler for HTTP/1.1 GET requests.
 */
public class FileRequestHandler {

    private final Path documentRoot;
    private static final String NEW_LINE = System.lineSeparator();

    public FileRequestHandler(Path documentRoot) {
        this.documentRoot = documentRoot;
    }

    /**
     * Called to handle an HTTP/1.1 GET request: first, the status code of the
     * request is determined and a corresponding response header is sent.
     * If the status code is <200>, the requested document root path is sent
     * back to the client. In case the path points to a file, the file is sent,
     * and in case the path points to a directory, a listing of the contained
     * files is sent.
     *
     * @param request Client request
     * @param response Server response
     */
    public void handle(String request, OutputStream response)
    throws IOException {
        String[] parse_array = request.split(" ");
        if (parse_array.length==3){
            String method = parse_array[0];
            String request_URI = parse_array[1];
            String version = parse_array[2];
            DateFormat dateFormat = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss zzz");

            /*
            --- Checklist
            [X] HTTP/1.1 200 OK
            [X] HTTP/1.1 400 Bad Request
            [X] HTTP/1.1 404 Not Found
            [X] HTTP/1.1 501 Not Implemented
            [X] HTTP/1.1 505 HTTP Version Not Supported
            [X] Date: Wed, 23 Nov 2016 10:03:50 CET
            [X] Content-Type: text/html
            [X] Content-Length: 182
            [X] Last-Modified: Tue, 25 Oct 2016 12:51:32 CEST
            */ 
            response.write(request_URI.getBytes());
            response.write(NEW_LINE.getBytes());
            switch (method) {
                // switch for numerous methods
                case "GET":
                    Path path = Paths.get(request_URI);
                    // check if path is valid
                    if (Files.exists(path)) {
                        // switch for numerous protocol versions
                        switch (version) {
                            case "HTTP/1.1":
                                response.write("HTTP/1.1 200 OK".getBytes());
                                response.write(NEW_LINE.getBytes());
                                // Date
                                response.write("Date: ".getBytes());
                                response.write(dateFormat.format(new Date()).getBytes());
                                response.write(NEW_LINE.getBytes());
                                // Content-Type from MIME
                                response.write("Content-Type: ".getBytes());
                                File index = path.toFile();
                                response.write(new MimetypesFileTypeMap().getContentType(index).getBytes());
                                response.write(NEW_LINE.getBytes());
                                // Content-Length by file length
                                response.write("Content-Length: ".getBytes());
                                String length = ""+index.length();
                                response.write(length.getBytes());
                                response.write(NEW_LINE.getBytes());
                                // LM-Date from file
                                response.write("Last-Modified: ".getBytes());
                                response.write(dateFormat.format(index.lastModified()).getBytes());
                                response.write(NEW_LINE.getBytes());
                                response.write(NEW_LINE.getBytes());
                                // Scanning and responding line by line (not too nicenstein)
                                Scanner input = new Scanner(index);
                                while (input.hasNextLine())
                                {
                                    response.write(input.nextLine().getBytes());
                                    response.write(NEW_LINE.getBytes());
                                }

                                break;
                            default:
                                response.write("HTTP/1.1 505 HTTP Version Not Supported".getBytes());

                                response.write(NEW_LINE.getBytes());
                                break;
                        }
                    } else {
                        response.write("HTTP/1.1 404 Not Found".getBytes());
                        response.write(NEW_LINE.getBytes());
                    }
                    
                    break;
                default: 
                    response.write("HTTP/1.1 501 Not Implemented".getBytes());
                    response.write(NEW_LINE.getBytes());
                    break;
            }
            response.write(NEW_LINE.getBytes());

        } else {
            response.write("HTTP/1.1 400 Bad Request".getBytes());
            response.write(NEW_LINE.getBytes());
        }
        
        /*
         * (a) Determine status code of the request and write proper status
         * line to the response output stream.
         *
         * Only continue if the request can be processed (status code 200).
         * In case the path points to a file (b) or a directory (c) write the
         * appropriate header fields and …
         *
         * (b) … the content of the file …
         * (c) … a listing of the directory contents …
         *
         * … to the response output stream.
         */
    }
}
