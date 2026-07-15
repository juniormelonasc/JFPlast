package com.jfplastic.model;

public class Cliente {
    private int id;
    private String nome;
    private String telefone;
    private String cidade;
    private String endereco;
    private String observacoes;
    private String cpfCnpj; // NOVO CAMPO

    public Cliente() { }

    public Cliente(int id, String nome, String telefone, String cidade, String endereco, String observacoes, String cpfCnpj) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.cidade = cidade;
        this.endereco = endereco;
        this.observacoes = observacoes;
        this.cpfCnpj = cpfCnpj;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    @Override
    public String toString() {
        return nome;
    }
}