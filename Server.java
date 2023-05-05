import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class Server {

    private static boolean hasNewMessage = false;

    private static ServerSocket serverIn;
    private static ServerSocket serverOut;

    private static ArrayList<Socket> clients = new ArrayList<Socket>();
    private static ArrayList<String> messages = new ArrayList<String>();

    // Sends messages to all users
    private static class ClientMessagingHandler extends Thread{

        public void run(){

            while(true){

                try{

                    if(hasNewMessage){

                        Iterator<Socket> clientIterator = clients.iterator();
                        String messageResponse = String.join("\n", messages);

                        while(clientIterator.hasNext()){ // Will break if user disconnects

                            Socket client = clientIterator.next();

                            Response response = new Response(messageResponse);
                            ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());

                            // output.reset();
                            // output.flush();
                            output.writeObject(response);
                            // output.close();

                        }

                        hasNewMessage = false;

                    }
                    
                    Thread.sleep(1000);

                }catch(Exception e){

                    System.out.println("Message sending ERROR: ");
                    e.printStackTrace();

                }
            
            }
        
        }

    }
    
    private static class ConnectedClientRequestHandler extends Thread{

        private Socket clientIn;
        private Socket clientOut;

        private String username;

        ConnectedClientRequestHandler(Socket clientIn){

            this.clientIn = clientIn;
            // this.username = client.getInetAddress().getHostName();
            
        }

        public void run(){

            boolean isConnected = true;

            try{

                while(isConnected){

                    boolean hasDataStream = (new DataInputStream(clientIn.getInputStream())).available() > 0;
                    if(hasDataStream){

                        ObjectInputStream input = new ObjectInputStream(clientIn.getInputStream());
                        Request request = (Request) input.readObject();
                        int type = request.getType();

                        switch(type){

                            case Request.Header.CONNECT:
                                
                                this.username = request.data;
                                System.out.println(this.username + " CONECTADO");

                                messages.add(username + " CONECTADO");
                                hasNewMessage = true;

                                ObjectOutputStream connectOutput = new ObjectOutputStream(clientIn.getOutputStream());
                                connectOutput.writeObject(new Response(true));

                            break;
                            case Request.Header.MESSAGE:

                                System.out.println(this.username + " ENVIOU UMA MENSAGEM");

                                messages.add(username + " diz: " + request.data); // Adding message to message list
                                hasNewMessage = true;

                            break;
                            case Request.Header.DISCONNECT:

                                System.out.println("DESCONECTANDO " + this.username);

                                clients.remove(this.clientOut);
                                isConnected = false;
                                messages.add(username + " DESCONECTADO");

                                System.out.println(this.username + " DESCONECTADO");
                                hasNewMessage = true;

                                ObjectOutputStream disconnectOutput = new ObjectOutputStream(clientOut.getOutputStream());
                                disconnectOutput.writeObject(new Response(true));
                                

                            break;
                            default:

                                ObjectOutputStream badRequestOutput = new ObjectOutputStream(clientIn.getOutputStream());
                                badRequestOutput.writeObject(new Response(false));

                            break;
                        
                        }
                    
                    }
                
                }

            }catch (Exception e){

                System.out.println("Connection ERROR:");
                e.printStackTrace();

            }

        }

        public void setClientOut(Socket clientOut) {

            this.clientOut = clientOut;

        }

    }

    public static void main(String[] args){

        try {

            InetAddress addr = null;

            // https://stackoverflow.com/questions/30419386/how-can-i-get-my-lan-ip-address-using-java
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {

                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> addrs = nic.getInetAddresses();

                while (addrs.hasMoreElements()) {

                    addr = addrs.nextElement();
                    if (!nic.isLoopback()) break;

                }

                if(addr != null) break;

            }

            serverIn = new ServerSocket(17601, 50, addr); // Socket de entrada
            serverOut = new ServerSocket(041001, 50, addr); // Socket de saida

            System.out.println("SERVIDOR ABERTO");
            System.out.println("IP: " + serverIn.getInetAddress().getHostAddress());

            ClientMessagingHandler messagingThread = new ClientMessagingHandler();

            // Connecting new user
            while(true){
                
                Socket clientIn = serverIn.accept();
                Socket clientOut = null;

                ConnectedClientRequestHandler requestThread = new ConnectedClientRequestHandler(clientIn);
                requestThread.start();

                clientOut = serverOut.accept();
                requestThread.setClientOut(clientOut);
                clients.add(clientOut);

                if(!messagingThread.isAlive()) messagingThread.start();

            }

        } catch (Exception e) {

            System.out.println("Server ERROR: ");
            e.printStackTrace();

        }

    }

}
