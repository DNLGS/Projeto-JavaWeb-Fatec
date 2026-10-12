package models;

public class Funcionario extends Pessoa{
    
    private int id;
    private String cargo;
    private int nivel_acesso;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCargo() {
        return cargo;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
    public int getNivel_acesso() {
        return nivel_acesso;
    }
    public void setNivel_acesso(int nivel_acesso) {
        this.nivel_acesso = nivel_acesso;
    }
    
}
