package com.fdm.banking.service;

import com.fdm.banking.entity.TransactionEntity;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF statement service using iText 7. (T027)
 */
@Service
public class PdfStatementService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Builds an in-memory PDF statement for the given account and transaction list.
     * @return PDF as byte array
     */
    public byte[] buildPdf(long accountId, LocalDate startDate, LocalDate endDate,
                            List<TransactionEntity> transactions) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            // Header
            String maskedAccountId = "****" + (accountId % 10000);
            doc.add(new Paragraph("Statement of Activity")
                    .setFontSize(18).setBold());
            doc.add(new Paragraph("Account: " + maskedAccountId)
                    .setFontSize(12));
            doc.add(new Paragraph("Period: " + startDate.format(DATE_FMT) + " to " + endDate.format(DATE_FMT))
                    .setFontSize(12));
            doc.add(new Paragraph("Generated: " + LocalDate.now().format(DATE_FMT))
                    .setFontSize(10));
            doc.add(new Paragraph(" "));

            // Transaction table
            float[] colWidths = {50f, 90f, 70f, 60f, 130f, 170f};
            Table table = new Table(UnitValue.createPercentArray(colWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header row
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Timestamp").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));

            // Data rows
            for (TransactionEntity t : transactions) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(t.getTransactionId()))));
                table.addCell(new Cell().add(new Paragraph(t.getAmount().toPlainString())));
                table.addCell(new Cell().add(new Paragraph(t.getType().name())));
                table.addCell(new Cell().add(new Paragraph(t.getStatus().name())));
                table.addCell(new Cell().add(new Paragraph(t.getTimestamp().format(DATETIME_FMT))));
                table.addCell(new Cell().add(new Paragraph(t.getDescription() != null ? t.getDescription() : "")));
            }

            if (transactions.isEmpty()) {
                table.addCell(new Cell(1, 6).add(new Paragraph("No transactions in the selected period.")));
            }

            doc.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF statement", e);
        }
        return baos.toByteArray();
    }
}
