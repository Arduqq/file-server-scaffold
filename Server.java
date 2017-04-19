import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A simple filer server implementation using Sockets. For the assignment,
 * implement the functions in `FileRequestHandler.java`. Test your
 * implementation with telnet and with your web browser.
 */
public class Server {
    private final Path documentRoot;
    private final int port;

    public Server(int port) throws FileNotFoundException {
        this.documentRoot = Paths.get("www-root");
        this.port = port;

        if (!(Files.exists(documentRoot) &&
            Files.isDirectory(documentRoot))) {
            throw new FileNotFoundException(
                "Document root does not exist or is not a directory."
            );
        }
    }

    /**
     * Start a passive socket and wait for a connection.
     *
     * This is described in detail in Networks, Protocols, Services, WT:II-61
     * Implementation based on Networks, Protocols, Services, WT:II-63
     */
    public void startListening() throws IOException {
        // Start passive socket on the server (step 1a)
        ServerSocket passiveSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port + ".");

        // Create a handler that processes any request sent by the client.
        FileRequestHandler handler = new FileRequestHandler(documentRoot);

        // The passive socket is now listening for an incoming request (step 1b)
        boolean listen = true;
        while (listen) {
            // When a client connects, a new connection socket regarding the
            // client is initialized by the server (step 3b)
            Socket connectionSocket = passiveSocket.accept();
            handleConnection(handler, connectionSocket);
        }

        passiveSocket.close();
    }

    /**
     * Handle a new connection by retrieving a request from the client which
     * will then be processed by a request handler.
     *
     * Implementation based on Networks, Protocols, Services, WT:II-64
     */
    public void handleConnection(FileRequestHandler handler,
        Socket connectionSocket) throws IOException {
        OutputStream responseStream = connectionSocket.getOutputStream();

        // From the connection socket, get the input stream and create a
        // buffered reader. BufferedReader implements `readLine()` which
        // allows entering the request on the client side.
        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(connectionSocket.getInputStream())
        );

        // Wait until the next line reaches the stream (i.e. the user hit
        // return and completed their request).
        String requestString = bufferedReader.readLine();

        // Use the socket IP and try to determine a host name
        // E.g. on my system that would be `RAUMSTATION`.
        String clientHost = connectionSocket.getInetAddress().getHostName();
        System.out.println(clientHost + ": " + requestString);

        // The request was received and can now be processed.
        handler.handle(requestString, responseStream);

        bufferedReader.close();
        responseStream.close();
    }

    /**
     * Start the file server with a provided document root and port.
     */
    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);

        try {
            server.startListening();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
