package com.jfplastic.model;

public class PedidoItem {
    private int id;
    private int pedidoId;
    private Produto produto;
    private int quantidade;
    private double valorUnitario;
    private double valorTotal;

    public PedidoItem() { }

    public PedidoItem(int id, int pedidoId, Produto produto, int quantidade, double valorUnitario, double valorTotal) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
}