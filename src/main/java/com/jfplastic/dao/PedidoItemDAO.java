package com.jfplastic.dao;

import com.jfplastic.database.DatabaseConnection;
import com.jfplastic.model.PedidoItem;
import com.jfplastic.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoItemDAO {

    private ProdutoDAO produtoDAO = new ProdutoDAO();

    public void inserir(PedidoItem item) {
        String sql = "INSERT INTO pedido_itens (pedido_id, produto_id, quantidade, valor_unitario, valor_total) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getPedidoId());
            stmt.setInt(2, item.getProduto().getId());
            stmt.setInt(3, item.getQuantidade());
            stmt.setDouble(4, item.getValorUnitario());
            stmt.setDouble(5, item.getValorTotal());
            stmt.executeUpdate();

            // Obtém o ID gerado
            try (Statement stmtId = conn.createStatement();
                 ResultSet rs = stmtId.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inserir item do pedido: " + e.getMessage());
        }
    }

    public void atualizar(PedidoItem item) {
        String sql = "UPDATE pedido_itens SET produto_id=?, quantidade=?, valor_unitario=?, valor_total=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getProduto().getId());
            stmt.setInt(2, item.getQuantidade());
            stmt.setDouble(3, item.getValorUnitario());
            stmt.setDouble(4, item.getValorTotal());
            stmt.setInt(5, item.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar item do pedido: " + e.getMessage());
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM pedido_itens WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao deletar item do pedido: " + e.getMessage());
        }
    }

    public void deletarPorPedido(int pedidoId) {
        String sql = "DELETE FROM pedido_itens WHERE pedido_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao deletar itens do pedido: " + e.getMessage());
        }
    }

    public List<PedidoItem> buscarPorPedido(int pedidoId) {
        List<PedidoItem> itens = new ArrayList<>();
        String sql = "SELECT * FROM pedido_itens WHERE pedido_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                itens.add(montarItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itens;
    }

    private PedidoItem montarItem(ResultSet rs) throws SQLException {
        int produtoId = rs.getInt("produto_id");
        Produto produto = produtoDAO.buscarPorId(produtoId);

        PedidoItem item = new PedidoItem();
        item.setId(rs.getInt("id"));
        item.setPedidoId(rs.getInt("pedido_id"));
        item.setProduto(produto);
        item.setQuantidade(rs.getInt("quantidade"));
        item.setValorUnitario(rs.getDouble("valor_unitario"));
        item.setValorTotal(rs.getDouble("valor_total"));
        return item;
    }
}