package com.jfplastic.service;

import com.jfplastic.dao.ProdutoDAO;
import com.jfplastic.model.Produto;

import java.util.List;

public class ProdutoService {
    private ProdutoDAO produtoDAO = new ProdutoDAO();

    public void salvar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (produto.getPrecoUnitario() <= 0) {
            throw new IllegalArgumentException("O preço unitário deve ser maior que zero.");
        }

        // Verifica duplicidade para novo produto
        if (produto.getId() == 0) {
            List<Produto> existentes = produtoDAO.buscarPorNome(produto.getNome());
            if (!existentes.isEmpty()) {
                throw new RuntimeException("Já existe um produto com o nome \"" + produto.getNome() + "\".");
            }
        }

        if (produto.getId() == 0) {
            produtoDAO.inserir(produto);
        } else {
            produtoDAO.atualizar(produto);
        }
    }

    public void excluir(int id) {
        produtoDAO.deletar(id);
    }

    public Produto buscarPorId(int id) {
        return produtoDAO.buscarPorId(id);
    }

    public List<Produto> listarTodos() {
        return produtoDAO.buscarTodos();
    }

    public List<Produto> pesquisarPorNome(String nome) {
        return produtoDAO.buscarPorNome(nome);
    }
}