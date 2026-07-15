package com.jfplastic.util;

import com.jfplastic.model.Pedido;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PDFGenerator {

    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void gerarPDF(Pedido pedido, String caminhoArquivo) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(caminhoArquivo));
        document.open();

        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font fontSubtitulo = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        Font fontNegrito = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        // Cabeçalho
        Paragraph titulo = new Paragraph("JF Plast", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Informações do pedido
        Paragraph info = new Paragraph("Pedido Nº: " + pedido.getId(), fontSubtitulo);
        info.setSpacingAfter(10);
        document.add(info);

        Paragraph emissao = new Paragraph("Data de Emissão: " + LocalDate.now().format(dateFormatter), fontNormal);
        emissao.setSpacingAfter(20);
        document.add(emissao);

        // Dados do cliente (incluindo CPF/CNPJ)
        Paragraph clienteTitulo = new Paragraph("Dados do Cliente", fontSubtitulo);
        clienteTitulo.setSpacingAfter(10);
        document.add(clienteTitulo);

        Paragraph clienteNome = new Paragraph("Nome: " + pedido.getCliente().getNome(), fontNormal);
        document.add(clienteNome);

        if (pedido.getCliente().getCpfCnpj() != null && !pedido.getCliente().getCpfCnpj().isEmpty()) {
            Paragraph clienteCpf = new Paragraph("CPF/CNPJ: " + pedido.getCliente().getCpfCnpj(), fontNormal);
            document.add(clienteCpf);
        }

        Paragraph clienteTelefone = new Paragraph("Telefone: " + pedido.getCliente().getTelefone(), fontNormal);
        clienteTelefone.setSpacingAfter(20);
        document.add(clienteTelefone);

        // Itens do pedido
        Paragraph itensTitulo = new Paragraph("Itens do Pedido", fontSubtitulo);
        itensTitulo.setSpacingAfter(10);
        document.add(itensTitulo);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);

        String[] headers = {"Produto", "Quantidade", "Preço Unitário", "Valor Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, fontNegrito));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (var item : pedido.getItens()) {
            table.addCell(new Paragraph(item.getProduto().getNome(), fontNormal));
            table.addCell(new Paragraph(String.valueOf(item.getQuantidade()), fontNormal));
            table.addCell(new Paragraph(currencyFormat.format(item.getValorUnitario()), fontNormal));
            table.addCell(new Paragraph(currencyFormat.format(item.getValorTotal()), fontNormal));
        }

        document.add(table);

        // Total do pedido
        Paragraph total = new Paragraph("Total do Pedido: " + currencyFormat.format(pedido.getValorTotal()), fontNegrito);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingAfter(20);
        document.add(total);

        // Local de entrega (observações)
        if (pedido.getObservacoes() != null && !pedido.getObservacoes().isEmpty()) {
            Paragraph obsTitulo = new Paragraph("Local de Entrega:", fontNegrito);
            obsTitulo.setSpacingAfter(5);
            document.add(obsTitulo);
            Paragraph obs = new Paragraph(pedido.getObservacoes(), fontNormal);
            obs.setSpacingAfter(20);
            document.add(obs);
        }

        // Rodapé
        Paragraph rodape = new Paragraph("Obrigado pela preferência.", fontNormal);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(30);
        document.add(rodape);

        document.close();
    }
}