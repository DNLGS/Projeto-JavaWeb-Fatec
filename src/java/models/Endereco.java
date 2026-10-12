package models;

public class Endereco {

    private int id;
    private String logradouro;
    private String bairro;
    private String numero;
    private int ibge;
    private int cep;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getLogradouro() {
        return logradouro;
    }
    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }
    public String getBairro() {
        return bairro;
    }
    public void setBairro(String bairro) {
        this.bairro = bairro;
    }
    public String getNumero() {
        return numero;
    }
    public void setNumero(String numero) {
        this.numero = numero;
    }
    public int getIbge() {
        return ibge;
    }
    public void setIbge(int ibge) {
        this.ibge = ibge;
    }
    public int getCep() {
        return cep;
    }
    public void setCep(int cep) {
        this.cep = cep;
    }

    
}
