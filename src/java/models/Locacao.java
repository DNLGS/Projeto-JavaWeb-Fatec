package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Locacao{
    
    private Pessoa cliente;
    private final List<Filme> filmes = new ArrayList<>();
    private int qtd_dias;
    public int getQtd_dias() {
        return qtd_dias;
    }

    public void setQtd_dias(int qtd_dias) {
        this.qtd_dias = qtd_dias;
    }

    private final LocalDate data_cadastro = LocalDate.now();
    private LocalDate data_devolucao;
    private int id;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getData_devolucao() {
        return data_devolucao;
    }

    public void setData_devolucao(LocalDate data_devolucao) {
        this.data_devolucao = data_devolucao;
    }

    public Pessoa getCliente() {
        return cliente;
    }

    public void setCliente(Pessoa cliente) {
        this.cliente = cliente;
    }

    public List<Filme> getFilmes() {
        return filmes;
    }

    public void addFilme(Filme fime) {
        this.filmes.add(fime);
    }

    public LocalDate getData_cadastro() {
        return data_cadastro;
    }
    
}
