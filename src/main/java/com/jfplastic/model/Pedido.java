package com.jfplastic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Pedido {
    private int id;
    private Cliente cliente;
    private Produto produto;
    private int quantidade;
    private double valorUnitario;
    private double valorTotal;
    private LocalDate dataPedido;
    private LocalDate dataEntrega;
    private String observacoes;

    // Construtores
    public Pedido() { }

    public Pedido(int id, Cliente cliente, Produto produto, int quantidade,
                  double valorUnitario, double valorTotal, LocalDate dataPedido,
                  LocalDate dataEntrega, String observacoes) {
        this.id = id;
        this.cliente = cliente;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
        this.dataPedido = dataPedido;
        this.dataEntrega = dataEntrega;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public LocalDate getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDate dataPedido) { this.dataPedido = dataPedido; }

    public LocalDate getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(LocalDate dataEntrega) { this.dataEntrega = dataEntrega; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    /**
     * Calcula o status do pedido.
     * Retorna um array: [cor, texto]
     */
    public String[] calcularStatus() {
        LocalDate hoje = LocalDate.now();
        long dias = ChronoUnit.DAYS.between(hoje, dataEntrega);

        String cor;
        String texto;

        if (dataEntrega.isBefore(hoje)) {
            cor = "PRETO";
            long atraso = Math.abs(dias);
            texto = "Atrasado há " + atraso + (atraso == 1 ? " dia" : " dias");
        } else if (dias == 0) {
            cor = "ROXO";
            texto = "Entrega hoje";
        } else if (dias == 1) {
            cor = "ROXO";
            texto = "Entrega amanhã";
        } else if (dias <= 5) {
            cor = "VERMELHO";
            texto = "Faltam " + dias + " dias";
        } else if (dias <= 15) {
            cor = "AMARELO";
            texto = "Faltam " + dias + " dias";
        } else {
            cor = "VERDE";
            texto = "Faltam " + dias + " dias";
        }
        return new String[]{cor, texto};
    }
}