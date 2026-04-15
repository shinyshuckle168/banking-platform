//package com.fdm.banking.util;
//
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.Month;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Unit tests for CanadianHolidayService. (T022)
// */
//class CanadianHolidayServiceTest {
//
//    private final CanadianHolidayService service = new CanadianHolidayService();
//
//    @Test
//    void newYearsDay_isHoliday() {
//        assertThat(service.isHoliday(LocalDate.of(2024, Month.JANUARY, 1))).isTrue();
//    }
//
//    @Test
//    void canadaDay_isHoliday() {
//        assertThat(service.isHoliday(LocalDate.of(2024, Month.JULY, 1))).isTrue();
//    }
//
//    @Test
//    void christmasDay_isHoliday() {
//        assertThat(service.isHoliday(LocalDate.of(2024, Month.DECEMBER, 25))).isTrue();
//    }
//
//    @Test
//    void saturday_isWeekend() {
//        // Jan 6, 2024 is a Saturday
//        assertThat(service.isWeekend(LocalDate.of(2024, Month.JANUARY, 6))).isTrue();
//    }
//
//    @Test
//    void sunday_isWeekend() {
//        // Jan 7, 2024 is a Sunday
//        assertThat(service.isWeekend(LocalDate.of(2024, Month.JANUARY, 7))).isTrue();
//    }
//
//    @Test
//    void monday_isNotWeekend() {
//        assertThat(service.isWeekend(LocalDate.of(2024, Month.JANUARY, 8))).isFalse();
//    }
//
//    @Test
//    void nextBusinessDay_skipsWeekend() {
//        // If today is Friday, next business day should skip Sat/Sun
//        // Jan 5, 2024 is a Friday
//        LocalDateTime friday = LocalDateTime.of(2024, 1, 5, 23, 0);
//        LocalDateTime result = service.nextBusinessDay(friday);
//        assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 8)); // Monday
//    }
//
//    @Test
//    void regularWeekday_isNotHoliday() {
//        assertThat(service.isHoliday(LocalDate.of(2024, Month.MARCH, 15))).isFalse();
//    }
//}
