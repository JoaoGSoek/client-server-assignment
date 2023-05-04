import java.io.Serializable;

public class Request implements Serializable{
    
    /*
     * Requisição
     * Toda transferência de dados com o servidor
     * deve ser feita através de um objeto Request
     */

    public class Header implements Serializable{

        /*
         * Cabeçalho da requisição
         * Para Se comunicar com o servidor, o cliente deve definir o tipo de conexão que deseja estabelecer.
         * Nossa arquitetura funcionará com o uso de 3 tipos:
         * CONNECT: Conexão em que o cliente busca estabelecer um canal para transferência de dados
         * MESSAGE: Conexão para o envio de mensagens para o servidor
         * DISCONNECT: Desconecta o cliente do servidor e finaliza a execução das threads do cliente
         */

        private int type;

        public static final int CONNECT = 0;
        public static final int MESSAGE = 1;
        public static final int DISCONNECT = 2;

        public void setType(int type) throws Exception{

            if(type == Header.CONNECT || type == Header.MESSAGE || type == Header.DISCONNECT){
                
                this.type = type;
                return;

            }
                
            throw new Exception("Illegal Type Exception", null);

        }

        public int getType(){return type;}

    }
    
    Request(String data, int type) throws Exception{

        this.data = data;
        this.setType(type);

    }
    
    Request(int type) throws Exception{

        this.setType(type);

    }

    public String data; // Dados da requisição

    private final Header header = new Header(); // Cabeçalho da requisição
    public void setType(int type) throws Exception{header.setType(type);}
    public int getType(){return header.getType();}

}
