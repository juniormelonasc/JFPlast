package com.jfplastic.controller;

import com.jfplastic.model.Cliente;
import com.jfplastic.model.Pedido;
import com.jfplastic.model.PedidoItem;
import com.jfplastic.model.Produto;
import com.jfplastic.service.ClienteService;
import com.jfplastic.service.PedidoService;
import com.jfplastic.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

public class PedidoController {

    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Produto> comboProduto;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;
    @FXML private TextField txtValorTotalPedido;
    @FXML private DatePicker dateDataPedido;
    @FXML private DatePicker dateDataEntrega;
    @FXML private TextField txtObservacoes;
    @FXML private TableView<PedidoItem> tabelaItens;
    @FXML private TableColumn<PedidoItem, String> colProduto;
    @FXML private TableColumn<PedidoItem, Integer> colQuantidade;
    @FXML private TableColumn<PedidoItem, Double> colValorUnitario;
    @FXML private TableColumn<PedidoItem, Double> colValorTotalItem;

    private PedidoService pedidoService = new PedidoService();
    private ClienteService clienteService = new ClienteService();
    private ProdutoService produtoService = new ProdutoService();

    private Pedido pedidoEditando;
    private ObservableList<PedidoItem> itens = FXCollections.observableArrayList();

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        comboCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente != null ? cliente.getNome() : "";
            }
            @Override
            public Cliente fromString(String string) { return null; }
        });

        comboProduto.setConverter(new StringConverter<Produto>() {
            @Override
            public String toString(Produto produto) {
                return produto != null ? produto.getNome() + " - " + currencyFormat.format(produto.getPrecoUnitario()) : "";
            }
            @Override
            public Produto fromString(String string) { return null; }
        });

        carregarCombos();

        comboProduto.valueProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                txtValorUnitario.setText(String.valueOf(novo.getPrecoUnitario()));
            }
        });

        colProduto.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colValorUnitario.setCellValueFactory(new PropertyValueFactory<>("valorUnitario"));
        colValorTotalItem.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        colValorUnitario.setCellFactory(column -> new TableCell<PedidoItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        colValorTotalItem.setCellFactory(column -> new TableCell<PedidoItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        tabelaItens.setItems(itens);

        dateDataPedido.setValue(LocalDate.now());
        dateDataEntrega.setValue(LocalDate.now().plusDays(7));

        itens.addListener((javafx.collections.ListChangeListener.Change<? extends PedidoItem> c) -> {
            atualizarTotalPedido();
        });
    }

    private void carregarCombos() {
        comboCliente.setItems(FXCollections.observableArrayList(clienteService.listarTodos()));
        comboProduto.setItems(FXCollections.observableArrayList(produtoService.listarTodos()));
    }

    @FXML
    private void adicionarItem() {
        Produto produto = comboProduto.getValue();
        if (produto == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Selecione um produto.");
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            if (quantidade <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Quantidade inválida (deve ser maior que zero).");
            return;
        }

        double valorUnitario;
        try {
            valorUnitario = Double.parseDouble(txtValorUnitario.getText().trim().replace(",", "."));
            if (valorUnitario <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Valor unitário inválido.");
            return;
        }

        double valorTotal = quantidade * valorUnitario;

        PedidoItem item = new PedidoItem();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorUnitario);
        item.setValorTotal(valorTotal);

        itens.add(item);

        comboProduto.setValue(null);
        txtQuantidade.clear();
        txtValorUnitario.clear();

        atualizarTotalPedido();
    }

    @FXML
    private void removerItem() {
        PedidoItem selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um item para remover.");
            return;
        }
        itens.remove(selecionado);
        atualizarTotalPedido();
    }

    private void atualizarTotalPedido() {
        double total = itens.stream().mapToDouble(PedidoItem::getValorTotal).sum();
        txtValorTotalPedido.setText(currencyFormat.format(total));
    }

    @FXML
    private void salvar() {
        try {
            Cliente cliente = comboCliente.getValue();
            if (cliente == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Selecione um cliente.");
                return;
            }

            if (itens.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Adicione pelo menos um produto ao pedido.");
                return;
            }

            LocalDate dataPedido = dateDataPedido.getValue();
            if (dataPedido == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Selecione a data do pedido.");
                return;
            }

            LocalDate dataEntrega = dateDataEntrega.getValue();
            if (dataEntrega == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Selecione a data de entrega.");
                return;
            }

            if (dataEntrega.isBefore(dataPedido)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Data de entrega não pode ser anterior à data do pedido.");
                return;
            }

            Pedido pedido = (pedidoEditando != null) ? pedidoEditando : new Pedido();
            pedido.setCliente(cliente);
            pedido.setDataPedido(dataPedido);
            pedido.setDataEntrega(dataEntrega);
            pedido.setObservacoes(txtObservacoes.getText().trim());
            pedido.setItens(new ArrayList<>(itens));

            pedidoService.salvar(pedido);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Pedido salvo com sucesso!");
            fechar();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        fechar();
    }

    private void fechar() {
        txtObservacoes.getScene().getWindow().hide();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void setPedidoParaEdicao(Pedido pedido) {
        this.pedidoEditando = pedido;
        comboCliente.setValue(pedido.getCliente());
        dateDataPedido.setValue(pedido.getDataPedido());
        dateDataEntrega.setValue(pedido.getDataEntrega());
        txtObservacoes.setText(pedido.getObservacoes());
        itens.setAll(pedido.getItens());
        atualizarTotalPedido();
    }
}