package com.jfplastic.controller;

import com.jfplastic.model.Cliente;
import com.jfplastic.model.Pedido;
import com.jfplastic.model.Produto;
import com.jfplastic.service.ClienteService;
import com.jfplastic.service.PedidoService;
import com.jfplastic.service.ProdutoService;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PedidoController {

    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Produto> comboProduto;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;
    @FXML private TextField txtValorTotal;
    @FXML private DatePicker dateDataPedido;
    @FXML private DatePicker dateDataEntrega;
    @FXML private TextField txtObservacoes;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private PedidoService pedidoService = new PedidoService();
    private ClienteService clienteService = new ClienteService();
    private ProdutoService produtoService = new ProdutoService();

    private Pedido pedidoEditando;
    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private ObservableList<Produto> listaProdutos = FXCollections.observableArrayList();

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Configurar combos
        comboCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente != null ? cliente.getNome() : "";
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });

        comboProduto.setConverter(new StringConverter<Produto>() {
            @Override
            public String toString(Produto produto) {
                return produto != null ? produto.getNome() + " - R$ " + produto.getPrecoUnitario() : "";
            }

            @Override
            public Produto fromString(String string) {
                return null;
            }
        });

        // Carregar dados dos combos
        carregarCombos();

        // Listener para preencher preço unitário ao selecionar produto
        comboProduto.valueProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                txtValorUnitario.setText(String.valueOf(novo.getPrecoUnitario()));
                calcularTotal();
            }
        });

        // Listeners para calcular total
        txtQuantidade.textProperty().addListener(this::calcularTotalListener);
        txtValorUnitario.textProperty().addListener(this::calcularTotalListener);

        // Data atual como padrão
        dateDataPedido.setValue(LocalDate.now());
        dateDataEntrega.setValue(LocalDate.now().plusDays(7));
    }

    private void carregarCombos() {
        listaClientes.setAll(clienteService.listarTodos());
        comboCliente.setItems(listaClientes);

        listaProdutos.setAll(produtoService.listarTodos());
        comboProduto.setItems(listaProdutos);
    }

    private void calcularTotal() {
        try {
            String qtdText = txtQuantidade.getText().trim();
            String valorText = txtValorUnitario.getText().trim().replace(",", ".");
            if (qtdText.isEmpty() || valorText.isEmpty()) {
                txtValorTotal.setText("");
                return;
            }
            int quantidade = Integer.parseInt(qtdText);
            double valorUnitario = Double.parseDouble(valorText);
            double total = quantidade * valorUnitario;
            txtValorTotal.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            // Ignora
        }
    }

    private void calcularTotalListener(ObservableValue<? extends String> obs, String old, String novo) {
        calcularTotal();
    }

    @FXML
    private void salvar() {
        try {
            Cliente cliente = comboCliente.getValue();
            if (cliente == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Selecione um cliente.");
                return;
            }

            Produto produto = comboProduto.getValue();
            if (produto == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Selecione um produto.");
                return;
            }

            int quantidade;
            try {
                quantidade = Integer.parseInt(txtQuantidade.getText().trim());
                if (quantidade <= 0) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Quantidade deve ser maior que zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Quantidade inválida.");
                return;
            }

            double valorUnitario;
            try {
                valorUnitario = Double.parseDouble(txtValorUnitario.getText().trim().replace(",", "."));
                if (valorUnitario <= 0) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Valor unitário deve ser maior que zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Valor unitário inválido.");
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
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "A data de entrega não pode ser anterior à data do pedido.");
                return;
            }

            double valorTotal = quantidade * valorUnitario;

            Pedido pedido = (pedidoEditando != null) ? pedidoEditando : new Pedido();
            pedido.setCliente(cliente);
            pedido.setProduto(produto);
            pedido.setQuantidade(quantidade);
            pedido.setValorUnitario(valorUnitario);
            pedido.setValorTotal(valorTotal);
            pedido.setDataPedido(dataPedido);
            pedido.setDataEntrega(dataEntrega);
            pedido.setObservacoes(txtObservacoes.getText().trim());

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
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // Método para carregar pedido para edição (chamado pela tela principal)
    public void setPedidoParaEdicao(Pedido pedido) {
        this.pedidoEditando = pedido;
        comboCliente.setValue(pedido.getCliente());
        comboProduto.setValue(pedido.getProduto());
        txtQuantidade.setText(String.valueOf(pedido.getQuantidade()));
        txtValorUnitario.setText(String.valueOf(pedido.getValorUnitario()));
        txtValorTotal.setText(String.valueOf(pedido.getValorTotal()));
        dateDataPedido.setValue(pedido.getDataPedido());
        dateDataEntrega.setValue(pedido.getDataEntrega());
        txtObservacoes.setText(pedido.getObservacoes());
        calcularTotal();
    }
}