package models;

import java.time.LocalDate;

public class Pessoa {
    private String nome;
    private String documento;
    private Endereco endereco;
    private LocalDate dt_nascimento;
    private final LocalDate dt_cadastro = LocalDate.now();

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getDocumento() {
        return documento;
    }
    public void setDocumento(String documento) {
        this.documento = documento;
    }
    public Endereco getEndereco() {
        return endereco;
    }
    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }    
    public LocalDate getDt_nascimento() {
        return dt_nascimento;
    }
    public void setDt_nascimento(LocalDate dt_nascimento) {
        this.dt_nascimento = dt_nascimento;
    }
    public LocalDate getDt_cadastro() {
        return dt_cadastro;
    }

}
