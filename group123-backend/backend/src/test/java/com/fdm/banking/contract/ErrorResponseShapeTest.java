//package com.fdm.banking.contract;
//
//import com.fdm.banking.dto.response.ErrorResponse;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Validates the ErrorResponse shape matches contract spec. (T116)
// * API contracts reference: specs/spec/group3-spec/contracts/
// */
//class ErrorResponseShapeTest {
//
//    @Test
//    void errorResponse_hasAllRequiredFields() {
//        ErrorResponse response = new ErrorResponse("ERR_TEST", "Test error message", "fieldName");
//        response.setTimestamp(LocalDateTime.now());
//
//        assertThat(response.getCode()).isEqualTo("ERR_TEST");
//        assertThat(response.getMessage()).isEqualTo("Test error message");
//        assertThat(response.getField()).isEqualTo("fieldName");
//        assertThat(response.getTimestamp()).isNotNull();
//    }
//
//    @Test
//    void errorResponse_withNullField_isValid() {
//        ErrorResponse response = new ErrorResponse("ERR_CODE", "Some message", null);
//        assertThat(response.getCode()).isEqualTo("ERR_CODE");
//        assertThat(response.getMessage()).isEqualTo("Some message");
//        assertThat(response.getField()).isNull();
//    }
//}
