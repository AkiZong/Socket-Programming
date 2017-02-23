import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;

public class server {
    static String err;
    int n_port;
    int r_port;
    int req_code_Server_int;
    int req_code_Client_int;
    String req_code_Server; // req_code on server
    String req_code_Client; // req_code from client
    String sentence; // the original sentence from client
    String reversedSentence; // reversed sentence
    ServerSocket welcomeSocket;
    DatagramSocket UDPServerSocket;

    // construction of a server with one required argument
    server (String argv[]) throws Exception {
        if (argv.length != 1) {
            err = "ERROR: there should be 1 arg: <n_port>";
            throw new IllegalArgumentException(err);
        } else {
            req_code_Server = argv[0];
            // parse req_code on server to integer
            req_code_Server_int = Integer.parseInt(req_code_Server);
            // create a new TCP socket on server, and server will generate a random n_port as the
            // negotiation port
            welcomeSocket = new ServerSocket(0);
            // get and store the negotiation in the variable called n_port
            n_port = welcomeSocket.getLocalPort();
            // print n_port on the screen
            System.out.println("SERVER_PORT=" + n_port);
        }
    }

    // Stage 1: Negotiation using TCP sockets on server
    static boolean ServerNegotiation (server s) throws Exception {
        Socket connectionSocket = s.welcomeSocket. accept();
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        s.req_code_Client = inFromClient.readLine();
        // parse req_code on client to integer
        s.req_code_Client_int = Integer.parseInt(s.req_code_Client);
        // the client sends the intended req_code
        if (s.req_code_Client_int == s.req_code_Server_int) {
            // create a new UDP socket on server, and server will generate a random r_port where
            // it will be listening for the actual request
            s.UDPServerSocket = new DatagramSocket(0);
            // get and store the r_port number in the variable called r_port
            s.r_port = s.UDPServerSocket.getLocalPort();
            // add "" to convert r_port to string
            outToClient.writeBytes("" + s.r_port + '\n');
            return true;
        // the client fails to send the intended req_code
        } else {
            err = "ERR: server close TCP connection!";
            // serve close the TCP connection since client's req_code is wrong
            connectionSocket.close();
            return false;
        }
    }

    // Stage 2: Transaction using UDP sockets on server
    // After we complete the TCP connection and get the r_port successfully, then the server
    // can reveive and send data to the client through UDP
    static void ServerTransaction (server s) throws Exception {
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        s.UDPServerSocket.receive(receivePacket);
        s.sentence = new String(receivePacket.getData());
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        s.reversedSentence = new StringBuffer(s.sentence).reverse().toString();
        sendData = s.reversedSentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        s.UDPServerSocket.send(sendPacket);
    }

    public static void main(String argv[]) throws Exception {
        try {
            server Server = new server(argv);
            while (true) {
                // TCP connection fails!
                if (ServerNegotiation(Server) == false) {
                    err = "ERR: TCP connection failed!";
                    continue;
                }
                ServerTransaction(Server);
            }
        } catch (Exception e) {
            System.out.println(err);
        }
    }
}

