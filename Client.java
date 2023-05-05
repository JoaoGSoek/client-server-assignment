import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client{

    private static String hostname;
    private static boolean isExiting = false;
    private static boolean isRunning = true;
    private static final String clearCL = "\033[H\033[2J";

    private static Socket server;
    private static MessageRetrievalThread retrieval;

    private static class MessageRetrievalThread extends Thread{

        private static Socket inputSocket;

        public synchronized void run(){

            try{

                inputSocket = new Socket(hostname, 041001);

                while(isRunning){

                    ObjectInputStream input = new ObjectInputStream(inputSocket.getInputStream());
                    Response response = (Response) input.readObject();
                    
                    if(isExiting){

                        if(response.successfull()){

                            System.out.println("Até mais!");
                            server.close();

                        }else{

                            System.out.println("Algo deu errado...");

                        }

                    }else{

                        System.out.print(clearCL);
                        System.out.println(response.getData());
                    
                    }

                }

            }catch(Exception e){

                System.out.println("Message Retrieval ERROR: ");
                e.printStackTrace();

            }

        }

    }

    private static class MessageCaptureThread extends Thread{
    
        public void run(){
            
            System.out.print(clearCL);
            System.out.println("Digite sua mensagem: ");

            while(isRunning){

                try{

                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String message = reader.readLine().toString();

                    isExiting = (message.equals("<exit>"));
                    isRunning = !isExiting;
                    int requestType = isExiting ? Request.Header.DISCONNECT : Request.Header.MESSAGE;

                    Request request = new Request(requestType);
                    request.data = message;

                    ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());
                    // output.flush();
                    output.writeObject(request);
                    // output.close();

                    System.out.println((isExiting) ? "Saindo" : "Enviando...");

                }catch(Exception e){

                    System.out.println("Message capture ERROR: ");
                    e.printStackTrace();

                }
            
            }

        }

    }

    public static void main(String[] args){
        
        try{

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Digite o endereço do servidor: ");
            String stringAddr = reader.readLine().toString();

            System.out.print("Digite seu apelido: ");
            String nickname = reader.readLine().toString();

            System.out.println("Estabelecendo conexão com o servidor...");
            InetAddress addr = InetAddress.getByName(stringAddr);
            hostname = addr.getHostName();
            server = new Socket(hostname, 17601); // Opening socket

            Request request = new Request(Request.Header.CONNECT);
            request.data = nickname;

            ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());
            // output.flush();
            output.writeObject(request);
            
            ObjectInputStream input = new ObjectInputStream(server.getInputStream());
            Response response = (Response) input.readObject();

            if(response.successfull()){

                System.out.print(clearCL);

                MessageCaptureThread capture = new MessageCaptureThread();
                retrieval = new MessageRetrievalThread();

                retrieval.start();
                capture.start();

            }

            // server.close();

        }catch(Exception e){

            System.out.println("Client ERROR: ");
            e.printStackTrace();

        }

    }
    
}
