import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class client {
    static String err;
    int n_port_int; // n_port_int stores the value of n_port after parsing into integer
    int req_code_int; // req_code_int stores the value of req_code after parsing into integer
    int r_port_int; // r_port_int stores the value of r_port after parsing into integer
    String reversedSentence; // msg reversed by server and sent back to client
    String server_address; // argv[0]
    String n_port; // argv[1]
    String req_code; // argv[2]
    String msg; // argv[3]
    String r_port; // r_port number from server

    // constructor of a client with four required arguments
    client(String argv[]) throws Exception {
        // if args length is not 4, then we throw an error
        if (argv.length != 4) {
            err = "ERROR: there should be 4 args: <server_address> <n_port> <req_code> <msg>";
            throw new IllegalArgumentException(err);
            // if args length is 4, then we pass these args to specified variables
        } else {
            server_address = argv[0];
            n_port = argv[1];
            req_code = argv[2];
            msg = argv[3];
            // parse n_port and req_code to integer
            n_port_int = Integer.parseInt(n_port);
            req_code_int = Integer.parseInt(req_code);
        }
    }

    // Stage 1: Negotiation using TCP sockets on client
    static void ClientNegotiation(client c) throws Exception {
        err = "ERROR: TCP connection failed (server_address or n_port does not match)!";
        Socket TCPClientSocket = new Socket(c.server_address, c.n_port_int);
        DataOutputStream outToServer = new DataOutputStream(TCPClientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(TCPClientSocket.getInputStream()));
        err = "ERROR: req_code doea not match!";
        outToServer.writeBytes(c.req_code + '\n');
        c.r_port = inFromServer.readLine();
        // parse r_port to integer
        c.r_port_int = Integer.parseInt(c.r_port);
        // after receiving r_port from server, the client closes the TCP connection with the server
        TCPClientSocket.close();
    }

    // Stage 2: Transaction using UDP sockets on client
    // After we complete the TCP connection and get the r_port successfully, then the client
    // can send data to and receive data from the server through UDP
    static void ClientTransaction(client c) throws Exception {
        DatagramSocket UDPClientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(c.server_address);
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        sendData = c.msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, c.r_port_int);
        UDPClientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        UDPClientSocket.receive(receivePacket);
        c.reversedSentence = new String(receivePacket.getData());
        UDPClientSocket.close();
    }

    public static void main(String argv[]) {
        try {
            client Client = new client(argv);
            ClientNegotiation(Client);
            ClientTransaction(Client);
            // print the reversed sentence
            System.out.println(Client.reversedSentence);
        } catch (Exception e) {
            System.out.println(err);
        }
    }
}
