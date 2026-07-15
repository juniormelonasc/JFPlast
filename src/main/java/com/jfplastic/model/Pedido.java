package com.jfplastic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int id;
    private Cliente cliente;
    private LocalDate dataPedido;
    private LocalDate dataEntrega;
    private String observacoes;   // Mantido para o DAO e PDFGenerator
    private String localEntrega;  // Mantido para a coluna FXML
    private boolean entregue;
    private double valorTotal;
    private List<PedidoItem> itens = new ArrayList<>();

    public Pedido() { }

    public Pedido(int id, Cliente cliente, LocalDate dataPedido, LocalDate dataEntrega,
                  String observacoes, String localEntrega, boolean entregue, double valorTotal) {
        this.id = id;
        this.cliente = cliente;
        this.dataPedido = dataPedido;
        this.dataEntrega = dataEntrega;
        this.observacoes = observacoes;
        this.localEntrega = localEntrega;
        this.entregue = entregue;
        this.valorTotal = valorTotal;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDate getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDate dataPedido) { this.dataPedido = dataPedido; }

    public LocalDate getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(LocalDate dataEntrega) { this.dataEntrega = dataEntrega; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getLocalEntrega() { return localEntrega; }
    public void setLocalEntrega(String localEntrega) { this.localEntrega = localEntrega; }

    public boolean isEntregue() { return entregue; }
    public void setEntregue(boolean entregue) { this.entregue = entregue; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public List<PedidoItem> getItens() { return itens; }
    public void setItens(List<PedidoItem> itens) { this.itens = itens; }

    public void adicionarItem(PedidoItem item) {
        itens.add(item);
        recalcularTotal();
    }

    public void removerItem(PedidoItem item) {
        itens.remove(item);
        recalcularTotal();
    }

    private void recalcularTotal() {
        valorTotal = itens.stream().mapToDouble(PedidoItem::getValorTotal).sum();
    }

    public String[] calcularStatus() {
        if (entregue) {
            return new String[]{"VERDE", "Entregue"};
        }

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