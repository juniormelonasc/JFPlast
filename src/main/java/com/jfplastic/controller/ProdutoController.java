package com.jfplastic.controller;

import com.jfplastic.model.Produto;
import com.jfplastic.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProdutoController {

    @FXML private TextField txtPesquisa;
    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TextField txtNome;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtPrecoUnitario;
    @FXML private TextField txtObservacoes;

    private ProdutoService produtoService = new ProdutoService();
    private ObservableList<Produto> listaProdutos = FXCollections.observableArrayList();
    private Produto produtoSelecionado;
    private boolean isEditando = false;

    @FXML
    public void initialize() {
        tabelaProdutos.getColumns().clear();

        TableColumn<Produto, Integer> colId = new TableColumn<>("Código");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Produto, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        TableColumn<Produto, Double> colPreco = new TableColumn<>("Preço Unitário");
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colPreco.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Produto, String> colObservacoes = new TableColumn<>("Observações");
        colObservacoes.setCellValueFactory(new PropertyValueFactory<>("observacoes"));

        tabelaProdutos.getColumns().addAll(colId, colNome, colDescricao, colPreco, colObservacoes);

        carregarTabela();

        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                selecionarProduto(novo);
            }
        });

        setCamposEditaveis(false);
    }

    private void carregarTabela() {
        listaProdutos.setAll(produtoService.listarTodos());
        tabelaProdutos.setItems(listaProdutos);
    }

    private void selecionarProduto(Produto produto) {
        produtoSelecionado = produto;
        txtNome.setText(produto.getNome());
        txtDescricao.setText(produto.getDescricao());
        txtPrecoUnitario.setText(String.valueOf(produto.getPrecoUnitario()));
        txtObservacoes.setText(produto.getObservacoes());
        if (!isEditando) {
            setCamposEditaveis(false);
        }
    }

    private void setCamposEditaveis(boolean editavel) {
        txtNome.setEditable(editavel);
        txtNome.setDisable(!editavel);
        txtDescricao.setEditable(editavel);
        txtDescricao.setDisable(!editavel);
        txtPrecoUnitario.setEditable(editavel);
        txtPrecoUnitario.setDisable(!editavel);
        txtObservacoes.setEditable(editavel);
        txtObservacoes.setDisable(!editavel);
    }

    private void limparCampos() {
        txtNome.clear();
        txtDescricao.clear();
        txtPrecoUnitario.clear();
        txtObservacoes.clear();
        produtoSelecionado = null;
        isEditando = false;
        setCamposEditaveis(false);
        tabelaProdutos.getSelectionModel().clearSelection();
    }

    @FXML
    private void pesquisar() {
        String termo = txtPesquisa.getText().trim();
        if (termo.isEmpty()) {
            carregarTabela();
        } else {
            listaProdutos.setAll(produtoService.pesquisarPorNome(termo));
            tabelaProdutos.setItems(listaProdutos);
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
        if (produtoSelecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um produto para editar.");
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

            String precoStr = txtPrecoUnitario.getText().trim().replace(",", ".");
            double preco;
            try {
                preco = Double.parseDouble(precoStr);
                if (preco <= 0) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro", "O preço deve ser maior que zero.");
                    txtPrecoUnitario.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Preço inválido. Use formato 99.99.");
                txtPrecoUnitario.requestFocus();
                return;
            }

            Produto produto = (produtoSelecionado != null && produtoSelecionado.getId() > 0)
                    ? produtoSelecionado
                    : new Produto();

            produto.setNome(nome);
            produto.setDescricao(txtDescricao.getText().trim());
            produto.setPrecoUnitario(preco);
            produto.setObservacoes(txtObservacoes.getText().trim());

            produtoService.salvar(produto);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto salvo com sucesso!");
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
        if (produtoSelecionado != null) {
            selecionarProduto(produtoSelecionado);
        }
    }

    @FXML
    private void excluir() {
        if (produtoSelecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um produto para excluir.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Tem certeza que deseja excluir o produto \"" + produtoSelecionado.getNome() + "\"?");
        confirm.setContentText("Esta ação não pode ser desfeita.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    produtoService.excluir(produtoSelecionado.getId());
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto excluído com sucesso.");
                    carregarTabela();
                    limparCampos();
                } catch (Exception e) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível excluir. O produto pode ter pedidos associados.");
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