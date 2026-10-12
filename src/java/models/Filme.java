package models;

public class Filme {
    private int id;
    private String titulo;
    private String genero; 
    private double nota_imb;
    private String observacoes;
    private double valor_dia;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getGenero() {
        return genero;
    }
    public void setGenero(String genero) {
        this.genero = genero;
    }
    public double getNota_imb() {
        return nota_imb;
    }
    public void setNota_imb(double nota_imb) {
        this.nota_imb = nota_imb;
    }
    public String getObservacoes() {
        return observacoes;
    }
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    public double getValor_dia() {
        return valor_dia;
    }
    public void setValor_dia(double valor_dia) {
        this.valor_dia = valor_dia;
    }
    
}
