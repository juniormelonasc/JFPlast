package com.jfplastic.controller;

import com.jfplastic.model.Cliente;
import com.jfplastic.service.ClienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClienteController {

    @FXML private TextField txtPesquisa;
    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TextField txtNome;
    @FXML private TextField txtCpfCnpj;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtCidade;
    @FXML private TextField txtEndereco;
    @FXML private TextField txtObservacoes;

    private ClienteService clienteService = new ClienteService();
    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private Cliente clienteSelecionado;
    private boolean isEditando = false;

    @FXML
    public void initialize() {
        tabelaClientes.getColumns().clear();

        TableColumn<Cliente, Integer> colId = new TableColumn<>("Código");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Cliente, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Cliente, String> colCpfCnpj = new TableColumn<>("CPF/CNPJ");
        colCpfCnpj.setCellValueFactory(new PropertyValueFactory<>("cpfCnpj"));

        TableColumn<Cliente, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        TableColumn<Cliente, String> colCidade = new TableColumn<>("Cidade");
        colCidade.setCellValueFactory(new PropertyValueFactory<>("cidade"));

        TableColumn<Cliente, String> colEndereco = new TableColumn<>("Endereço");
        colEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));

        TableColumn<Cliente, String> colObservacoes = new TableColumn<>("Observações");
        colObservacoes.setCellValueFactory(new PropertyValueFactory<>("observacoes"));

        tabelaClientes.getColumns().addAll(colId, colNome, colCpfCnpj, colTelefone, colCidade, colEndereco, colObservacoes);

        carregarTabela();

        tabelaClientes.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                selecionarCliente(novo);
            }
        });

        setCamposEditaveis(false);
    }

    private void carregarTabela() {
        listaClientes.setAll(clienteService.listarTodos());
        tabelaClientes.setItems(listaClientes);
    }

    private void selecionarCliente(Cliente cliente) {
        clienteSelecionado = cliente;
        txtNome.setText(cliente.getNome());
        txtCpfCnpj.setText(cliente.getCpfCnpj());
        txtTelefone.setText(cliente.getTelefone());
        txtCidade.setText(cliente.getCidade());
        txtEndereco.setText(cliente.getEndereco());
        txtObservacoes.setText(cliente.getObservacoes());
        if (!isEditando) {
            setCamposEditaveis(false);
        }
    }

    private void setCamposEditaveis(boolean editavel) {
        txtNome.setEditable(editavel);
        txtNome.setDisable(!editavel);
        txtCpfCnpj.setEditable(editavel);
        txtCpfCnpj.setDisable(!editavel);
        txtTelefone.setEditable(editavel);
        txtTelefone.setDisable(!editavel);
        txtCidade.setEditable(editavel);
        txtCidade.setDisable(!editavel);
        txtEndereco.setEditable(editavel);
        txtEndereco.setDisable(!editavel);
        txtObservacoes.setEditable(editavel);
        txtObservacoes.setDisable(!editavel);
    }

    private void limparCampos() {
        txtNome.clear();
        txtCpfCnpj.clear();
        txtTelefone.clear();
        txtCidade.clear();
        txtEndereco.clear();
        txtObservacoes.clear();
        clienteSelecionado = null;
        isEditando = false;
        setCamposEditaveis(false);
        tabelaClientes.getSelectionModel().clearSelection();
    }

    @FXML
    private void pesquisar() {
        String termo = txtPesquisa.getText().trim();
        if (termo.isEmpty()) {
            carregarTabela();
        } else {
            listaClientes.setAll(clienteService.pesquisarPorNome(termo));
            tabelaClientes.setItems(listaClientes);
        }
    }

    @FXML
    private void cadastrar() {
        limparCampos();
        isEditando = true;
        setCamposEditaveis(true);
        txtNome.requestFocus();
    }

    @FXML
    private void editar() {
        if (clienteSelecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um cliente para editar.");
            return;
        }
        isEditando = true;
        setCamposEditaveis(true);
        txtNome.requestFocus();
    }

    @FXML
    private void salvar() {
        try {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "O nome é obrigatório.");
                txtNome.requestFocus();
                return;
            }

            Cliente cliente = (clienteSelecionado != null && clienteSelecionado.getId() > 0)
                    ? clienteSelecionado
                    : new Cliente();

            cliente.setNome(nome);
            cliente.setCpfCnpj(txtCpfCnpj.getText().trim());
            cliente.setTelefone(txtTelefone.getText().trim());
            cliente.setCidade(txtCidade.getText().trim());
            cliente.setEndereco(txtEndereco.getText().trim());
            cliente.setObservacoes(txtObservacoes.getText().trim());

            clienteService.salvar(cliente);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Cliente salvo com sucesso!");
            carregarTabela();
            limparCampos();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        limparCampos();
        if (clienteSelecionado != null) {
            selecionarCliente(clienteSelecionado);
        }
    }

    @FXML
    private void excluir() {
        if (clienteSelecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um cliente para excluir.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Tem certeza que deseja excluir o cliente \"" + clienteSelecionado.getNome() + "\"?");
        confirm.setContentText("Esta ação não pode ser desfeita.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    clienteService.excluir(clienteSelecionado.getId());
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Cliente excluído com sucesso.");
                    carregarTabela();
                    limparCampos();
                } catch (Exception e) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível excluir. O cliente pode ter pedidos associados.");
                }
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}