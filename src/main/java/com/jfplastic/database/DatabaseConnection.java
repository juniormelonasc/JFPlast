package com.jfplastic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:jfplast.db";

    // Retorna uma nova conexão. Em caso de erro, lança RuntimeException.
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            criarTabelasSeNaoExistirem(conn);
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
    }

    private static void criarTabelasSeNaoExistirem(Connection conn) {
        String sqlClientes = """
            CREATE TABLE IF NOT EXISTS clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                telefone TEXT,
                cidade TEXT,
                endereco TEXT,
                observacoes TEXT,
                UNIQUE(nome)
            );
        """;

        String sqlProdutos = """
            CREATE TABLE IF NOT EXISTS produtos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                descricao TEXT,
                preco_unitario REAL NOT NULL,
                observacoes TEXT,
                UNIQUE(nome)
            );
        """;

        String sqlPedidos = """
            CREATE TABLE IF NOT EXISTS pedidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                produto_id INTEGER NOT NULL,
                quantidade INTEGER NOT NULL,
                valor_unitario REAL NOT NULL,
                valor_total REAL NOT NULL,
                data_pedido TEXT NOT NULL,
                data_entrega TEXT NOT NULL,
                observacoes TEXT,
                FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT,
                FOREIGN KEY (produto_id) REFERENCES produtos(id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlClientes);
            stmt.execute(sqlProdutos);
            stmt.execute(sqlPedidos);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao criar tabelas: " + e.getMessage());
        }
    }
}