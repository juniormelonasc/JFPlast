package com.jfplastic.dao;

import com.jfplastic.database.DatabaseConnection;
import com.jfplastic.model.Cliente;
import com.jfplastic.model.Pedido;
import com.jfplastic.model.Produto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private ClienteDAO clienteDAO = new ClienteDAO();
    private ProdutoDAO produtoDAO = new ProdutoDAO();

    public void inserir(Pedido pedido) {
        String sql = "INSERT INTO pedidos (cliente_id, produto_id, quantidade, valor_unitario, valor_total, data_pedido, data_entrega, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedido.getCliente().getId());
            stmt.setInt(2, pedido.getProduto().getId());
            stmt.setInt(3, pedido.getQuantidade());
            stmt.setDouble(4, pedido.getValorUnitario());
            stmt.setDouble(5, pedido.getValorTotal());
            stmt.setString(6, pedido.getDataPedido().toString());
            stmt.setString(7, pedido.getDataEntrega().toString());
            stmt.setString(8, pedido.getObservacoes());
            stmt.executeUpdate();

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
        String sql = "UPDATE pedidos SET cliente_id=?, produto_id=?, quantidade=?, valor_unitario=?, valor_total=?, data_pedido=?, data_entrega=?, observacoes=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedido.getCliente().getId());
            stmt.setInt(2, pedido.getProduto().getId());
            stmt.setInt(3, pedido.getQuantidade());
            stmt.setDouble(4, pedido.getValorUnitario());
            stmt.setDouble(5, pedido.getValorTotal());
            stmt.setString(6, pedido.getDataPedido().toString());
            stmt.setString(7, pedido.getDataEntrega().toString());
            stmt.setString(8, pedido.getObservacoes());
            stmt.setInt(9, pedido.getId());
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
                return montarPedido(rs);
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
                pedidos.add(montarPedido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    // Busca com filtros (para pesquisa e filtros da tela principal)
    public List<Pedido> buscarComFiltros(String termo, String filtro) {
        List<Pedido> pedidos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.* FROM pedidos p ");
        sql.append("JOIN clientes c ON p.cliente_id = c.id ");
        sql.append("JOIN produtos pr ON p.produto_id = pr.id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (termo != null && !termo.trim().isEmpty()) {
            sql.append("AND (c.nome LIKE ? OR pr.nome LIKE ? OR p.id LIKE ?) ");
            String like = "%" + termo + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        // Filtros: "Entregues", "Pendentes", "Atrasados", "Hoje", "Esta Semana", "Este Mês"
        LocalDate hoje = LocalDate.now();
        if (filtro != null && !filtro.isEmpty() && !filtro.equals("Todos")) {
            switch (filtro) {
                case "Entregues":
                    sql.append("AND p.data_entrega < ? ");
                    params.add(hoje.toString());
                    break;
                case "Pendentes":
                    sql.append("AND p.data_entrega >= ? ");
                    params.add(hoje.toString());
                    break;
                case "Atrasados":
                    sql.append("AND p.data_entrega < ? ");
                    params.add(hoje.toString());
                    break;
                case "Hoje":
                    sql.append("AND p.data_entrega = ? ");
                    params.add(hoje.toString());
                    break;
                case "Esta Semana":
                    LocalDate inicioSemana = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1);
                    LocalDate fimSemana = inicioSemana.plusDays(6);
                    sql.append("AND p.data_entrega BETWEEN ? AND ? ");
                    params.add(inicioSemana.toString());
                    params.add(fimSemana.toString());
                    break;
                case "Este Mês":
                    LocalDate inicioMes = hoje.withDayOfMonth(1);
                    LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());
                    sql.append("AND p.data_entrega BETWEEN ? AND ? ");
                    params.add(inicioMes.toString());
                    params.add(fimMes.toString());
                    break;
            }
        }

        sql.append(" ORDER BY p.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pedidos.add(montarPedido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    // Método auxiliar para montar objeto Pedido a partir do ResultSet
    private Pedido montarPedido(ResultSet rs) throws SQLException {
        int clienteId = rs.getInt("cliente_id");
        int produtoId = rs.getInt("produto_id");

        Cliente cliente = clienteDAO.buscarPorId(clienteId);
        Produto produto = produtoDAO.buscarPorId(produtoId);

        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        pedido.setCliente(cliente);
        pedido.setProduto(produto);
        pedido.setQuantidade(rs.getInt("quantidade"));
        pedido.setValorUnitario(rs.getDouble("valor_unitario"));
        pedido.setValorTotal(rs.getDouble("valor_total"));
        pedido.setDataPedido(LocalDate.parse(rs.getString("data_pedido")));
        pedido.setDataEntrega(LocalDate.parse(rs.getString("data_entrega")));
        pedido.setObservacoes(rs.getString("observacoes"));
        return pedido;
    }
}