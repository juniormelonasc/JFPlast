package com.jfplastic.service;

import com.jfplastic.dao.PedidoDAO;
import com.jfplastic.dao.PedidoItemDAO;
import com.jfplastic.model.Pedido;
import com.jfplastic.model.PedidoItem;

import java.util.List;

public class PedidoService {
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private PedidoItemDAO pedidoItemDAO = new PedidoItemDAO();

    public void salvar(Pedido pedido) {
        if (pedido.getCliente() == null) {
            throw new IllegalArgumentException("Selecione um cliente.");
        }
        if (pedido.getDataPedido() == null) {
            throw new IllegalArgumentException("Data do pedido é obrigatória.");
        }
        if (pedido.getDataEntrega() == null) {
            throw new IllegalArgumentException("Data de entrega é obrigatória.");
        }
        if (pedido.getDataEntrega().isBefore(pedido.getDataPedido())) {
            throw new IllegalArgumentException("Data de entrega não pode ser anterior à data do pedido.");
        }
        if (pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um produto ao pedido.");
        }

        pedido.setValorTotal(pedido.getItens().stream().mapToDouble(PedidoItem::getValorTotal).sum());

        if (pedido.getId() == 0) {
            pedidoDAO.inserir(pedido);
            // Agora que o pedido tem ID, salva os itens
            for (PedidoItem item : pedido.getItens()) {
                item.setPedidoId(pedido.getId());
                pedidoItemDAO.inserir(item);
            }
        } else {
            // Atualiza pedido
            pedidoDAO.atualizar(pedido);
            // Remove itens antigos e insere os novos
            pedidoItemDAO.deletarPorPedido(pedido.getId());
            for (PedidoItem item : pedido.getItens()) {
                item.setPedidoId(pedido.getId());
                pedidoItemDAO.inserir(item);
            }
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
}