//package com.fdm.banking.util;
//
//import com.fdm.banking.config.BankingCategoriesProperties;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Unit tests for CategoryResolver. (T100)
// */
//class CategoryResolverTest {
//
//    private final CategoryResolver resolver;
//
//    CategoryResolverTest() {
//        Map<String, List<String>> categories = Map.of(
//                "Food & Drink", List.of("mcdonald", "starbucks", "grocery"),
//                "Transport", List.of("uber", "bus", "train"),
//                "Housing", List.of("rent", "mortgage")
//        );
//        BankingCategoriesProperties props = new BankingCategoriesProperties();
//        props.setCategories(categories);
//        resolver = new CategoryResolver(props);
//    }
//
//    @Test
//    void matchesKeyword_returnsCategory() {
//        assertThat(resolver.resolve("McDonald's payment")).isEqualTo("Food & Drink");
//    }
//
//    @Test
//    void caseInsensitiveMatch() {
//        assertThat(resolver.resolve("UBER TRIP")).isEqualTo("Transport");
//    }
//
//    @Test
//    void noMatch_returnsNull() {
//        assertThat(resolver.resolve("random merchant xyz")).isNull();
//    }
//
//    @Test
//    void null_returnsNull() {
//        assertThat(resolver.resolve(null)).isNull();
//    }
//
//    @Test
//    void emptyString_returnsNull() {
//        assertThat(resolver.resolve("")).isNull();
//    }
//}
