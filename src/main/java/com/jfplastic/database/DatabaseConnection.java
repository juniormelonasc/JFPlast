package com.jfplastic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:jfplast.db";

    public static synchronized Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            criarTabelasSeNaoExistirem(conn);
            migrarBanco(conn);
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao conectar ao banco: " + e.getMessage());
        }
    }

    private static void criarTabelasSeNaoExistirem(Connection conn) {
        String sqlClientes = """
            CREATE TABLE IF NOT EXISTS clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL UNIQUE,
                telefone TEXT,
                cidade TEXT,
                endereco TEXT,
                observacoes TEXT,
                cpf_cnpj TEXT
            );
        """;

        String sqlProdutos = """
            CREATE TABLE IF NOT EXISTS produtos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL UNIQUE,
                descricao TEXT,
                preco_unitario REAL NOT NULL,
                observacoes TEXT
            );
        """;

        String sqlPedidos = """
            CREATE TABLE IF NOT EXISTS pedidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                data_pedido TEXT NOT NULL,
                data_entrega TEXT NOT NULL,
                observacoes TEXT,
                entregue INTEGER DEFAULT 0,
                valor_total REAL DEFAULT 0,
                FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT
            );
        """;

        String sqlPedidoItens = """
            CREATE TABLE IF NOT EXISTS pedido_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id INTEGER NOT NULL,
                produto_id INTEGER NOT NULL,
                quantidade INTEGER NOT NULL,
                valor_unitario REAL NOT NULL,
                valor_total REAL NOT NULL,
                FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
                FOREIGN KEY (produto_id) REFERENCES produtos(id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlClientes);
            stmt.execute(sqlProdutos);
            stmt.execute(sqlPedidos);
            stmt.execute(sqlPedidoItens);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    private static void migrarBanco(Connection conn) {
        // Adiciona coluna cpf_cnpj se não existir
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE clientes ADD COLUMN cpf_cnpj TEXT");
        } catch (SQLException e) {
            // Coluna já existe, ignora
        }
    }
}