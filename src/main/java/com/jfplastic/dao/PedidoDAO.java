package com.jfplastic.dao;

import com.jfplastic.database.DatabaseConnection;
import com.jfplastic.model.Cliente;
import com.jfplastic.model.Pedido;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private ClienteDAO clienteDAO = new ClienteDAO();
    private PedidoItemDAO pedidoItemDAO = new PedidoItemDAO();

    public void inserir(Pedido pedido) {
        String sql = "INSERT INTO pedidos (cliente_id, data_pedido, data_entrega, observacoes, entregue, valor_total) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedido.getCliente().getId());
            stmt.setString(2, pedido.getDataPedido().toString());
            stmt.setString(3, pedido.getDataEntrega().toString());
            stmt.setString(4, pedido.getObservacoes());
            stmt.setInt(5, pedido.isEntregue() ? 1 : 0);
            stmt.setDouble(6, pedido.getValorTotal());
            stmt.executeUpdate();

            // Obtém o ID gerado usando last_insert_rowid()
            try (Statement stmtId = conn.createStatement();
                 ResultSet rs = stmtId.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    pedido.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inserir pedido: " + e.getMessage());
        }
    }

    public void atualizar(Pedido pedido) {
        String sql = "UPDATE pedidos SET cliente_id=?, data_pedido=?, data_entrega=?, observacoes=?, entregue=?, valor_total=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedido.getCliente().getId());
            stmt.setString(2, pedido.getDataPedido().toString());
            stmt.setString(3, pedido.getDataEntrega().toString());
            stmt.setString(4, pedido.getObservacoes());
            stmt.setInt(5, pedido.isEntregue() ? 1 : 0);
            stmt.setDouble(6, pedido.getValorTotal());
            stmt.setInt(7, pedido.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar pedido: " + e.getMessage());
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM pedidos WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao deletar pedido: " + e.getMessage());
        }
    }

    public Pedido buscarPorId(int id) {
        String sql = "SELECT * FROM pedidos WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Pedido pedido = montarPedido(rs);
                pedido.setItens(pedidoItemDAO.buscarPorPedido(id));
                return pedido;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Pedido> buscarTodos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedidos ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Pedido pedido = montarPedido(rs);
                pedido.setItens(pedidoItemDAO.buscarPorPedido(pedido.getId()));
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    private Pedido montarPedido(ResultSet rs) throws SQLException {
        int clienteId = rs.getInt("cliente_id");
        Cliente cliente = clienteDAO.buscarPorId(clienteId);

        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        pedido.setCliente(cliente);
        pedido.setDataPedido(LocalDate.parse(rs.getString("data_pedido")));
        pedido.setDataEntrega(LocalDate.parse(rs.getString("data_entrega")));
        pedido.setObservacoes(rs.getString("observacoes"));
        pedido.setEntregue(rs.getInt("entregue") == 1);
        pedido.setValorTotal(rs.getDouble("valor_total"));
        return pedido;
    }
}