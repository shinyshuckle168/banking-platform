//package com.fdm.banking.service;
//
//import com.fdm.banking.entity.Transaction;
//import com.fdm.banking.entity.TransactionStatus;
//import com.fdm.banking.entity.TransactionDirection;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Unit tests for PdfStatementService. (T031)
// */
//class PdfStatementServiceTest {
//
//    private final PdfStatementService service = new PdfStatementService();
//
//    @Test
//    void generatePdf_withEmptyTransactions_returnsPdfBytes() {
//        byte[] pdf = service.buildPdf(1L, LocalDate.now().minusDays(30),
//                LocalDate.now(), List.of());
//        assertThat(pdf).isNotNull();
//        assertThat(pdf.length).isGreaterThan(0);
//        // PDF magic bytes
//        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
//    }
//
//    @Test
//    void generatePdf_withTransactions_returnsPdfBytes() {
//        Transaction item = new Transaction();
//        item.setTransactionId(1L);
//        item.setAmount(new BigDecimal("100.00"));
//        item.setDirection(TransactionDirection.TRANSFER);
//        item.setStatus(TransactionStatus.SUCCESS);
//        item.setTimestamp(LocalDateTime.now().minusDays(1));
//        item.setDescription("Test payment");
//
//        byte[] pdf = service.buildPdf(1L, LocalDate.now().minusDays(30),
//                LocalDate.now(), List.of(item));
//        assertThat(pdf).isNotNull();
//        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
//    }
//}
