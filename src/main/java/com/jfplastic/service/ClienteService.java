package com.jfplastic.service;

import com.jfplastic.dao.ClienteDAO;
import com.jfplastic.model.Cliente;

import java.util.List;

public class ClienteService {
    private ClienteDAO clienteDAO = new ClienteDAO();

    public void salvar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }

        // Verifica duplicidade (se for novo cliente)
        if (cliente.getId() == 0) {
            List<Cliente> existentes = clienteDAO.buscarPorNome(cliente.getNome());
            if (!existentes.isEmpty()) {
                throw new RuntimeException("Já existe um cliente com o nome \"" + cliente.getNome() + "\".");
            }
        }

        if (cliente.getId() == 0) {
            clienteDAO.inserir(cliente);
        } else {
            clienteDAO.atualizar(cliente);
        }
    }

    public void excluir(int id) {
        clienteDAO.deletar(id);
    }

    public Cliente buscarPorId(int id) {
        return clienteDAO.buscarPorId(id);
    }

    public List<Cliente> listarTodos() {
        return clienteDAO.buscarTodos();
    }

    public List<Cliente> pesquisarPorNome(String nome) {
        return clienteDAO.buscarPorNome(nome);
    }
}