package com.jfplastic.service;

import com.jfplastic.dao.PedidoDAO;
import com.jfplastic.model.Pedido;

import java.time.LocalDate;
import java.util.List;

public class PedidoService {
    private PedidoDAO pedidoDAO = new PedidoDAO();

    public void salvar(Pedido pedido) {
        // Validações
        if (pedido.getCliente() == null) {
            throw new IllegalArgumentException("Selecione um cliente.");
        }
        if (pedido.getProduto() == null) {
            throw new IllegalArgumentException("Selecione um produto.");
        }
        if (pedido.getQuantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (pedido.getDataPedido() == null) {
            throw new IllegalArgumentException("A data do pedido é obrigatória.");
        }
        if (pedido.getDataEntrega() == null) {
            throw new IllegalArgumentException("A data de entrega é obrigatória.");
        }
        if (pedido.getDataEntrega().isBefore(pedido.getDataPedido())) {
            throw new IllegalArgumentException("A data de entrega não pode ser anterior à data do pedido.");
        }

        // Calcula o valor total (garantia)
        pedido.setValorTotal(pedido.getQuantidade() * pedido.getValorUnitario());

        if (pedido.getId() == 0) {
            pedidoDAO.inserir(pedido);
        } else {
            pedidoDAO.atualizar(pedido);
        }
    }

    public void excluir(int id) {
        pedidoDAO.deletar(id);
    }

    public Pedido buscarPorId(int id) {
        return pedidoDAO.buscarPorId(id);
    }

    public List<Pedido> listarTodos() {
        return pedidoDAO.buscarTodos();
    }

    public List<Pedido> pesquisar(String termo, String filtro) {
        return pedidoDAO.buscarComFiltros(termo, filtro);
    }

    // Métodos para o dashboard
    public int contarTotal() {
        return listarTodos().size();
    }

    public int contarAtrasados() {
        LocalDate hoje = LocalDate.now();
        return (int) listarTodos().stream()
                .filter(p -> p.getDataEntrega().isBefore(hoje))
                .count();
    }

    public int contarParaHoje() {
        LocalDate hoje = LocalDate.now();
        return (int) listarTodos().stream()
                .filter(p -> p.getDataEntrega().equals(hoje))
                .count();
    }

    public int contarEstaSemana() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        return (int) listarTodos().stream()
                .filter(p -> !p.getDataEntrega().isBefore(inicioSemana) && !p.getDataEntrega().isAfter(fimSemana))
                .count();
    }

    public double somarValorTotal() {
        return listarTodos().stream()
                .mapToDouble(Pedido::getValorTotal)
                .sum();
    }
}