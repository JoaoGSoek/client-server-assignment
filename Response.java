import java.io.Serializable;

public class Response implements Serializable{

    /*
     * Resposta
     * Implementação simples do modelo de resposta do servidor
     */

    private boolean success; // Indicação de sucesso na conexão
    private String data; // Resposta em si

    Response(String data){

        this.data = data;

    }

    Response(boolean success){

        this.success = success;

    }

    public String getData(){return data;}
    public boolean successfull(){return success;}
    
}