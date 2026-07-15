package com.jfplastic.controller;

import com.jfplastic.model.Pedido;
import com.jfplastic.model.PedidoItem;
import com.jfplastic.service.PedidoService;
import com.jfplastic.util.PDFGenerator; // Importação correta com "o"
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    @FXML private TableColumn<Pedido, String> colLocalEntrega; // Corrigido aqui
    @FXML private TableColumn<Pedido, Integer> colStatus;

    @FXML private TextField txtPesquisa;
    @FXML private ComboBox<String> comboFiltro;
    @FXML private Label lblTotalPedidos;
    @FXML private Label lblAtrasados;
    @FXML private Label lblHoje;
    @FXML private Label lblEstaSemana;
    @FXML private Label lblValorTotal;

    private PedidoService pedidoService = new PedidoService();
    private ObservableList<Pedido> listaPedidos = FXCollections.observableArrayList();

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final NumberFormat integerFormat = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        carregarPedidos();
        atualizarDashboard();
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDataPedido.setCellValueFactory(new PropertyValueFactory<>("dataPedido"));
        colDataEntrega.setCellValueFactory(new PropertyValueFactory<>("dataEntrega"));
        colLocalEntrega.setCellValueFactory(new PropertyValueFactory<>("localEntrega")); // Configurado aqui

        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colValorTotal.setCellFactory(column -> new TableCell<Pedido, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        colCliente.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCliente() != null) {
                return new SimpleStringProperty(cellData.getValue().getCliente().getNome());
            }
            return new SimpleStringProperty("");
        });

        colProduto.setCellValueFactory(param -> {
            Pedido p = param.getValue();
            if (p.getItens() == null || p.getItens().isEmpty()) {
                return new SimpleStringProperty("");
            } else if (p.getItens().size() == 1) {
                return new SimpleStringProperty(p.getItens().get(0).getProduto().getNome());
            } else {
                return new SimpleStringProperty(p.getItens().size() + " produtos");
            }
        });

        colQuantidade.setCellValueFactory(param -> {
            Pedido p = param.getValue();
            if (p.getItens() == null) return new SimpleObjectProperty<>(0);
            int total = p.getItens().stream().mapToInt(PedidoItem::getQuantidade).sum();
            return new SimpleObjectProperty<>(total);
        });
        colQuantidade.setCellFactory(column -> new TableCell<Pedido, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(integerFormat.format(item));
                }
            }
        });

        colValorUnitario.setCellValueFactory(param -> {
            Pedido p = param.getValue();
            if (p.getItens() == null || p.getItens().isEmpty()) {
                return new SimpleObjectProperty<>(0.0);
            }
            return new SimpleObjectProperty<>(p.getItens().get(0).getValorUnitario());
        });
        colValorUnitario.setCellFactory(column -> new TableCell<Pedido, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        colStatus.setCellValueFactory(param -> {
            Pedido p = param.getValue();
            int dias;
            if (p.isEntregue()) {
                dias = Integer.MAX_VALUE;
            } else if (p.getDataEntrega() == null) {
                dias = 0;
            } else {
                dias = (int) ChronoUnit.DAYS.between(LocalDate.now(), p.getDataEntrega());
            }
            return new SimpleObjectProperty<>(dias);
        });

        colStatus.setCellFactory(column -> new TableCell<Pedido, Integer>() {
            @Override
            protected void updateItem(Integer dias, boolean empty) {
                super.updateItem(dias, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Pedido p = getTableRow().getItem();
                    String[] status = p.calcularStatus();
                    setText(status[1]);
                    String cor = status[0];
                    switch (cor) {
                        case "VERDE": setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); break;
                        case "AMARELO": setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;"); break;
                        case "VERMELHO": setStyle("-fx-background-color: #f44336; -fx-text-fill: white;"); break;
                        case "ROXO": setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;"); break;
                        case "PRETO": setStyle("-fx-background-color: #000000; -fx-text-fill: white;"); break;
                        default: setStyle("");
                    }
                }
            }
        });

        colStatus.setComparator((d1, d2) -> {
            if (d1.equals(d2)) return 0;
            if (d1 == Integer.MAX_VALUE) return 1;
            if (d2 == Integer.MAX_VALUE) return -1;
            return d1.compareTo(d2);
        });
        colStatus.setSortable(true);
        colStatus.setSortType(TableColumn.SortType.ASCENDING);
        tabelaPedidos.getSortOrder().add(colStatus);
    }

    private void configurarFiltros() {
        comboFiltro.setItems(FXCollections.observableArrayList(
                "Todos", "Entregues", "Pendentes", "Atrasados",
                "Hoje", "Esta Semana", "Este Mês"
        ));
        comboFiltro.getSelectionModel().selectFirst();

        txtPesquisa.textProperty().addListener((obs, old, novo) -> carregarPedidos());
        comboFiltro.valueProperty().addListener((obs, old, novo) -> carregarPedidos());
    }

    private void carregarPedidos() {
        String termo = txtPesquisa.getText() == null ? "" : txtPesquisa.getText().trim().toLowerCase();
        String filtro = comboFiltro.getValue();

        if (filtro == null) {
            filtro = "Todos";
        }

        List<Pedido> todos = pedidoService.listarTodos();

        if (todos == null) return;

        if (!termo.isEmpty()) {
            todos = todos.stream()
                    .filter(p -> (p.getCliente() != null && p.getCliente().getNome().toLowerCase().contains(termo)) ||
                            (p.getItens() != null && p.getItens().stream().anyMatch(i -> i.getProduto().getNome().toLowerCase().contains(termo))) ||
                            String.valueOf(p.getId()).contains(termo))
                    .collect(Collectors.toList());
        }

        if (!filtro.equals("Todos")) {
            LocalDate hoje = LocalDate.now();
            switch (filtro) {
                case "Entregues":
                    todos = todos.stream().filter(Pedido::isEntregue).collect(Collectors.toList());
                    break;
                case "Pendentes":
                    todos = todos.stream().filter(p -> !p.isEntregue()).collect(Collectors.toList());
                    break;
                case "Atrasados":
                    todos = todos.stream()
                            .filter(p -> !p.isEntregue() && p.getDataEntrega() != null && p.getDataEntrega().isBefore(hoje))
                            .collect(Collectors.toList());
                    break;
                case "Hoje":
                    todos = todos.stream()
                            .filter(p -> !p.isEntregue() && p.getDataEntrega() != null && p.getDataEntrega().equals(hoje))
                            .collect(Collectors.toList());
                    break;
                case "Esta Semana":
                    LocalDate inicioSemana = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1);
                    LocalDate fimSemana = inicioSemana.plusDays(6);
                    todos = todos.stream()
                            .filter(p -> !p.isEntregue() && p.getDataEntrega() != null && !p.getDataEntrega().isBefore(inicioSemana) && !p.getDataEntrega().isAfter(fimSemana))
                            .collect(Collectors.toList());
                    break;
                case "Este Mês":
                    LocalDate inicioMes = hoje.withDayOfMonth(1);
                    LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());
                    todos = todos.stream()
                            .filter(p -> !p.isEntregue() && p.getDataEntrega() != null && !p.getDataEntrega().isBefore(inicioMes) && !p.getDataEntrega().isAfter(fimMes))
                            .collect(Collectors.toList());
                    break;
            }
        }

        listaPedidos.setAll(todos);
        tabelaPedidos.setItems(listaPedidos);
    }

    private void atualizarDashboard() {
        List<Pedido> todos = pedidoService.listarTodos();
        if (todos == null) return;

        List<Pedido> pendentes = todos.stream().filter(p -> !p.isEntregue()).collect(Collectors.toList());

        LocalDate hoje = LocalDate.now();
        long total = pendentes.size();
        long atrasados = pendentes.stream().filter(p -> p.getDataEntrega() != null && p.getDataEntrega().isBefore(hoje)).count();
        long hojeCount = pendentes.stream().filter(p -> p.getDataEntrega() != null && p.getDataEntrega().equals(hoje)).count();

        LocalDate inicioSemana = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        long semana = pendentes.stream()
                .filter(p -> p.getDataEntrega() != null && !p.getDataEntrega().isBefore(inicioSemana) && !p.getDataEntrega().isAfter(fimSemana))
                .count();

        double valorTotal = pendentes.stream().mapToDouble(Pedido::getValorTotal).sum();

        lblTotalPedidos.setText("Total Pedidos: " + total);
        lblAtrasados.setText("Atrasados: " + atrasados);
        lblHoje.setText("Hoje: " + hojeCount);
        lblEstaSemana.setText("Esta Semana: " + semana);
        lblValorTotal.setText("Valor Total: " + currencyFormat.format(valorTotal));
    }

    @FXML private void novoPedido() { abrirTelaPedido(null); }
    @FXML private void editarPedido() { abrirTelaPedido(obterSelecionado()); }

    private void abrirTelaPedido(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pedido.fxml"));
            Scene scene = new Scene(loader.load());
            PedidoController controller = loader.getController();
            if (pedido != null) {
                controller.setPedidoParaEdicao(pedido);
            }
            Stage stage = new Stage();
            stage.setTitle(pedido == null ? "Novo Pedido" : "Editar Pedido");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
            carregarPedidos();
            atualizarDashboard();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de pedido.");
        }
    }

    @FXML private void excluirPedido() {
        Pedido selecionado = obterSelecionado();
        if (selecionado == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Excluir pedido");
        confirm.setHeaderText("Tem certeza que deseja excluir o pedido #" + selecionado.getId() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                pedidoService.excluir(selecionado.getId());
                carregarPedidos();
                atualizarDashboard();
            }
        });
    }

    @FXML private void marcarEntregue() {
        Pedido selecionado = obterSelecionado();
        if (selecionado == null) return;
        if (selecionado.isEntregue()) {
            mostrarErro("Este pedido já está marcado como entregue.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Marcar como entregue");
        confirm.setHeaderText("Confirmar entrega do pedido #" + selecionado.getId() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                selecionado.setEntregue(true);
                pedidoService.salvar(selecionado);
                carregarPedidos();
                atualizarDashboard();
            }
        });
    }

    @FXML private void atualizar() {
        carregarPedidos();
        atualizarDashboard();
    }

    @FXML private void gerarPDF() {
        Pedido selecionado = obterSelecionado();
        if (selecionado == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar PDF do Pedido");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos PDF (*.pdf)", "*.pdf")
        );
        String nomeSugerido = String.format("Pedido_%04d.pdf", selecionado.getId());
        fileChooser.setInitialFileName(nomeSugerido);

        File arquivo = fileChooser.showSaveDialog(tabelaPedidos.getScene().getWindow());
        if (arquivo == null) return;

        try {
            PDFGenerator.gerarPDF(selecionado, arquivo.getAbsolutePath()); // Chamada correta da classe
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Gerado");
            alert.setHeaderText(null);
            alert.setContentText("PDF salvo com sucesso em:\n" + arquivo.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    @FXML private void abrirClientes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/clientes.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Clientes");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir clientes.");
        }
    }

    @FXML private void abrirProdutos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produtos.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Produtos");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir produtos.");
        }
    }

    private Pedido obterSelecionado() {
        Pedido p = tabelaPedidos.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarErro("Selecione um pedido na lista.");
        }
        return p;
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}