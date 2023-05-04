import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

        private Socket client;
        private String username;

        ConnectedClientRequestHandler(Socket client){

            this.client = client;
            this.username = client.getInetAddress().getHostName();
            
        }

        public void run(){

            boolean isConnected = true;

            try{

                while(isConnected){

                    boolean hasDataStream = (new DataInputStream(client.getInputStream())).available() > 0;
                    if(hasDataStream){

                        ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                        Request request = (Request) input.readObject();
                        int type = request.getType();

                        switch(type){

                            case Request.Header.CONNECT:
                                
                                ObjectOutputStream connectOutput = new ObjectOutputStream(client.getOutputStream());
                                connectOutput.writeObject(new Response(true));
                                hasNewMessage = true;

                            break;
                            case Request.Header.MESSAGE:

                                messages.add(username + " diz: " + request.data); // Adding message to message list
                                hasNewMessage = true;

                            break;
                            case Request.Header.DISCONNECT:

                                System.out.println("Requisição de saida recebida");
                                Iterator<Socket> clientIterator = clients.iterator();

                                while(clientIterator.hasNext()){

                                    Socket clientOut = clientIterator.next();
                                    String coUsername = clientOut.getInetAddress().getHostName();

                                    if(coUsername.equals(username)){

                                        ObjectOutputStream disconnectOutput = new ObjectOutputStream(clientOut.getOutputStream());
                                        disconnectOutput.writeObject(new Response(true));

                                        clients.remove(clientOut);

                                        isConnected = false;
                                        break;

                                    }

                                }

                            break;
                            default:

                                ObjectOutputStream badRequestOutput = new ObjectOutputStream(client.getOutputStream());
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

    }

    public static void main(String[] args){

        try {

            serverIn = new ServerSocket(17601, 50, Inet4Address.getLocalHost()); // Socket de entrada
            serverOut = new ServerSocket(041001, 50, Inet4Address.getLocalHost()); // Socket de saida

            System.out.println("SERVIDOR ABERTO");
            System.out.println("IP: " + serverIn.getInetAddress().getHostAddress());
            System.out.println("NOME: " + serverIn.getInetAddress().getHostName());

            // Connecting new user
            while(true){

                Socket clientIn = serverIn.accept();
                System.out.println(clientIn.getInetAddress().getHostName());

                ConnectedClientRequestHandler requestThread = new ConnectedClientRequestHandler(clientIn);
                requestThread.start();

                Socket clientOut = serverOut.accept();
                clients.add(clientOut);

                ClientMessagingHandler messagingThread = new ClientMessagingHandler();
                messagingThread.start();

            }

        } catch (Exception e) {

            System.out.println("Server ERROR: ");
            e.printStackTrace();

        }

    }

}
