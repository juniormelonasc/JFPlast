package com.jfplastic.controller;

import com.jfplastic.model.Pedido;
import com.jfplastic.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PrincipalController {

    @FXML private TableView<Pedido> tabelaPedidos;
    @FXML private TableColumn<Pedido, Integer> colId;
    @FXML private TableColumn<Pedido, String> colCliente;
    @FXML private TableColumn<Pedido, String> colProduto;
    @FXML private TableColumn<Pedido, Integer> colQuantidade;
    @FXML private TableColumn<Pedido, Double> colValorUnitario;
    @FXML private TableColumn<Pedido, Double> colValorTotal;
    @FXML private TableColumn<Pedido, String> colDataPedido;
    @FXML private TableColumn<Pedido, String> colDataEntrega;
    @FXML private TableColumn<Pedido, String> colStatus;
    @FXML private TableColumn<Pedido, String> colObservacoes;

    @FXML private TextField txtPesquisa;
    @FXML private ComboBox<String> comboFiltro;

    // Dashboard labels
    @FXML private Label lblTotalPedidos;
    @FXML private Label lblAtrasados;
    @FXML private Label lblHoje;
    @FXML private Label lblEstaSemana;
    @FXML private Label lblValorTotal;

    private PedidoService pedidoService = new PedidoService();
    private ObservableList<Pedido> listaPedidos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar colunas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCliente().getNome()));
        colProduto.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colValorUnitario.setCellValueFactory(new PropertyValueFactory<>("valorUnitario"));
        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colDataPedido.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDataPedido().toString()));
        colDataEntrega.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDataEntrega().toString()));
        colObservacoes.setCellValueFactory(new PropertyValueFactory<>("observacoes"));

        // Coluna de status com cores
        colStatus.setCellFactory(column -> new TableCell<Pedido, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Pedido pedido = getTableRow().getItem();
                    String[] status = pedido.calcularStatus();
                    setText(status[1]);
                    String cor = status[0];
                    switch (cor) {
                        case "VERDE":
                            setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                            break;
                        case "AMARELO":
                            setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
                            break;
                        case "VERMELHO":
                            setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                            break;
                        case "ROXO":
                            setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
                            break;
                        case "PRETO":
                            setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Configurar filtros
        ObservableList<String> filtros = FXCollections.observableArrayList(
                "Todos", "Entregues", "Pendentes", "Atrasados",
                "Hoje", "Esta Semana", "Este Mês"
        );
        comboFiltro.setItems(filtros);
        comboFiltro.getSelectionModel().selectFirst();

        // Listeners para pesquisa e filtro
        txtPesquisa.textProperty().addListener((obs, old, novo) -> carregarPedidos());
        comboFiltro.valueProperty().addListener((obs, old, novo) -> carregarPedidos());

        // Carregar dados
        carregarPedidos();
        atualizarDashboard();

        // Selecionar pedido para editar/excluir
        tabelaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            // Podemos habilitar/desabilitar botões se necessário
        });
    }

    private void carregarPedidos() {
        String termo = txtPesquisa.getText().trim();
        String filtro = comboFiltro.getValue();
        listaPedidos.setAll(pedidoService.pesquisar(termo, filtro));
        tabelaPedidos.setItems(listaPedidos);
    }

    private void atualizarDashboard() {
        lblTotalPedidos.setText("Total Pedidos: " + pedidoService.contarTotal());
        lblAtrasados.setText("Atrasados: " + pedidoService.contarAtrasados());
        lblHoje.setText("Hoje: " + pedidoService.contarParaHoje());
        lblEstaSemana.setText("Esta Semana: " + pedidoService.contarEstaSemana());
        lblValorTotal.setText("Valor Total: R$ " + String.format("%.2f", pedidoService.somarValorTotal()));
    }

    // Abrir tela de novo pedido
    @FXML
    private void novoPedido() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pedido.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("JF Plast - Novo Pedido");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
            // Atualizar após fechar
            carregarPedidos();
            atualizarDashboard();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de pedido.");
        }
    }

    // Abrir para editar pedido selecionado
    @FXML
    private void editarPedido() {
        Pedido selecionado = tabelaPedidos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Selecione um pedido para editar.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pedido.fxml"));
            Scene scene = new Scene(loader.load());
            PedidoController controller = loader.getController();
            controller.setPedidoParaEdicao(selecionado);

            Stage stage = new Stage();
            stage.setTitle("JF Plast - Editar Pedido");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
            carregarPedidos();
            atualizarDashboard();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de edição.");
        }
    }

    // Excluir pedido selecionado
    @FXML
    private void excluirPedido() {
        Pedido selecionado = tabelaPedidos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Selecione um pedido para excluir.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Excluir pedido #" + selecionado.getId() + "?");
        confirm.setContentText("Esta ação não pode ser desfeita.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    pedidoService.excluir(selecionado.getId());
                    carregarPedidos();
                    atualizarDashboard();
                } catch (Exception e) {
                    mostrarErro("Erro ao excluir pedido.");
                }
            }
        });
    }

    // Atualizar manualmente
    @FXML
    private void atualizar() {
        carregarPedidos();
        atualizarDashboard();
    }

    // Gerar PDF (será implementado depois)
    @FXML
    private void gerarPDF() {
        Pedido selecionado = tabelaPedidos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Selecione um pedido para gerar o PDF.");
            return;
        }
        // TODO: Implementar geração de PDF (próximo passo)
        mostrarErro("Funcionalidade em desenvolvimento.");
    }

    // Abrir clientes
    @FXML
    private void abrirClientes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/clientes.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("JF Plast - Cadastro de Clientes");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de clientes.");
        }
    }

    // Abrir produtos
    @FXML
    private void abrirProdutos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produtos.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("JF Plast - Cadastro de Produtos");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de produtos.");
        }
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}