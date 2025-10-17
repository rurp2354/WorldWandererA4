package flight;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

/*
 * A4 – JUnit 5 tests for FlightSearch.
 * C1..C11 use 2 rows each (22), + 4 happy rows, + 1 state-safety = 27 runs.
 * All inputs are lowercase as required.
 */
public class FlightSearchTest {

    private FlightSearch fs;
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    // quick date helpers
    private static String today()         { return LocalDate.now().format(DMY); }
    private static String tomorrow()      { return LocalDate.now().plusDays(1).format(DMY); }
    private static String plusDays(int d) { return LocalDate.now().plusDays(d).format(DMY); }

    // tiny wrapper to build argument rows
    private static Arguments args(
            String depDate, String depAir, boolean emergency,
            String retDate, String dstAir, String seat,
            int adults, int children, int infants, boolean expectedValid) {
        return Arguments.of(depDate, depAir, emergency, retDate, dstAir, seat, adults, children, infants, expectedValid);
    }

    @BeforeEach
    void setup() {
        fs = new FlightSearch();
    }

    // run and check both return value + object state (per Note 7)
    private void runAndAssert(
            String dep, String depAir, boolean er,
            String ret, String dst, String seat,
            int a, int c, int i, boolean expectedValid) {

        // Pre-condition: all fields should be default
        assertNull(fs.getDepartureDate());
        assertNull(fs.getDepartureAirportCode());
        assertFalse(fs.isEmergencyRowSeating());
        assertNull(fs.getReturnDate());
        assertNull(fs.getDestinationAirportCode());
        assertNull(fs.getSeatingClass());
        assertEquals(0, fs.getAdultPassengerCount());
        assertEquals(0, fs.getChildPassengerCount());
        assertEquals(0, fs.getInfantPassengerCount());

        boolean actual = fs.runFlightSearch(dep, depAir, er, ret, dst, seat, a, c, i);
        assertEquals(expectedValid, actual, "return value mismatch");

        if (expectedValid) {
            // Post-condition: attributes must match parameters
            assertEquals(dep,  fs.getDepartureDate());
            assertEquals(depAir, fs.getDepartureAirportCode());
            assertEquals(er,   fs.isEmergencyRowSeating());
            assertEquals(ret,  fs.getReturnDate());
            assertEquals(dst,  fs.getDestinationAirportCode());
            assertEquals(seat, fs.getSeatingClass());
            assertEquals(a,    fs.getAdultPassengerCount());
            assertEquals(c,    fs.getChildPassengerCount());
            assertEquals(i,    fs.getInfantPassengerCount());
        } else {
            // Post-condition: no change after invalid call
            assertNull(fs.getDepartureDate());
            assertNull(fs.getDepartureAirportCode());
            assertFalse(fs.isEmergencyRowSeating());
            assertNull(fs.getReturnDate());
            assertNull(fs.getDestinationAirportCode());
            assertNull(fs.getSeatingClass());
            assertEquals(0, fs.getAdultPassengerCount());
            assertEquals(0, fs.getChildPassengerCount());
            assertEquals(0, fs.getInfantPassengerCount());
        }
    }


    // C1..C11 (each has 2 rows)

    static Stream<Arguments> c1_totalPassengersBounds() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            // NEW: negative child count -> invalid
            args(dep, "mel", false, ret, "pvg", "economy", 2, -1, 0, false),
            // NEW: negative infant count -> invalid
            args(dep, "mel", false, ret, "pvg", "economy", 2, 0, -1, false),
            // Existing rows:
            args(dep, "mel", false, ret, "pvg", "economy", 0, 0, 0, false),
            args(dep, "mel", false, ret, "pvg", "economy", 9, 1, 0, false)
        );
    }

    @ParameterizedTest @MethodSource("c1_totalPassengersBounds")
    @DisplayName("C1: total passengers must be 1..9")
    void c1(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c2_childrenRestrictions() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            args(dep, "mel", true,  ret, "pvg", "economy", 1, 1, 0, false),   // child + emergency
            args(dep, "mel", false, ret, "pvg", "first",   1, 1, 0, false)    // child + first
        );
    }
    @ParameterizedTest @MethodSource("c2_childrenRestrictions")
    @DisplayName("C2: children not allowed in emergency row nor first class")
    void c2(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c3_infantRestrictions() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            args(dep, "mel", true,  ret, "pvg", "economy", 1, 0, 1, false),    // infant + emergency
            args(dep, "mel", false, ret, "pvg", "business", 1, 0, 1, false)    // infant + business
        );
    }
    @ParameterizedTest @MethodSource("c3_infantRestrictions")
    @DisplayName("C3: infants not allowed in emergency row nor business class")
    void c3(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c4_childrenPerAdult() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            args(dep, "mel", false, ret, "pvg", "economy", 1, 3, 0, false),    // >2 kids per adult
            args(dep, "mel", false, ret, "pvg", "economy", 1, 2, 0, true)      // 2 kids per adult
        );
    }
    @ParameterizedTest @MethodSource("c4_childrenPerAdult")
    @DisplayName("C4: ≤2 children per adult")
    void c4(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c5_infantPerAdult() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            args(dep, "mel", false, ret, "pvg", "economy", 1, 0, 2, false),    // >1 infant per adult
            args(dep, "mel", false, ret, "pvg", "economy", 1, 0, 1, true)
        );
    }
    @ParameterizedTest @MethodSource("c5_infantPerAdult")
    @DisplayName("C5: ≤1 infant per adult")
    void c5(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c6_departureNotPast() {
        String depPast = LocalDate.now().minusDays(1).format(DMY);
        String depToday = today(), ret = plusDays(5);
        return Stream.of(
            args(depPast,  "mel", false, ret, "pvg", "economy", 1, 0, 0, false),
            args(depToday, "mel", false, ret, "pvg", "economy", 1, 0, 0, true)
        );
    }
    @ParameterizedTest @MethodSource("c6_departureNotPast")
    @DisplayName("C6: departure date not in the past")
    void c6(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c7_strictDateValidation() {
        return Stream.of(
            args("29/02/2026", "mel", false, "05/03/2026", "pvg", "economy", 1, 0, 0, false), // invalid date
            args("28/02/2026", "mel", false, "05/03/2026", "pvg", "economy", 1, 0, 0, true)
        );
    }
    @ParameterizedTest @MethodSource("c7_strictDateValidation")
    @DisplayName("C7: strict DD/MM/YYYY with real calendar")
    void c7(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c8_returnAfterOrEqualDeparture() {
        String dep = plusDays(10);
        return Stream.of(
            args(dep, "mel", false, plusDays(9),  "pvg", "economy", 1, 0, 0, false), // return before dep
            args(dep, "mel", false, plusDays(10), "pvg", "economy", 1, 0, 0, true)   // equal
        );
    }
    @ParameterizedTest @MethodSource("c8_returnAfterOrEqualDeparture")
    @DisplayName("C8: return date cannot be before departure")
    void c8(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c9_allowedSeatingClass() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            args(dep, "mel", false, ret, "pvg", "ultra",   1, 0, 0, false),    // unknown class
            args(dep, "mel", false, ret, "pvg", "economy", 1, 0, 0, true)
        );
    }
    @ParameterizedTest @MethodSource("c9_allowedSeatingClass")
    @DisplayName("C9: seating class must be one of allowed values")
    void c9(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c10_emergencyRowOnlyEconomy() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            args(dep, "syd", true,  ret, "cdg", "economy", 2, 0, 0, true),     // economy can be emergency
            args(dep, "mel", true,  ret, "pvg", "business", 2, 0, 0, false)     // other classes cannot
        );
    }
    @ParameterizedTest @MethodSource("c10_emergencyRowOnlyEconomy")
    @DisplayName("C10: only economy class seating can have an emergency row")
    void c10(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    static Stream<Arguments> c11_airportsAllowedAndDifferent() {
        String dep = tomorrow(), ret = plusDays(5);
        return Stream.of(
            args(dep, "xxx", false, ret, "pvg", "economy", 1, 0, 0, false),    // unknown airport
            args(dep, "syd", false, ret, "lax", "economy", 1, 0, 0, true)
        );
    }
    @ParameterizedTest @MethodSource("c11_airportsAllowedAndDifferent")
    @DisplayName("C11: airports must be allowed and different")
    void c11(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    // Happy paths (4 rows)

    static Stream<Arguments> happyRows() {
        return Stream.of(
            args(tomorrow(),  "mel", false, plusDays(7),  "pvg", "economy",          2, 2, 1, true),
            args(tomorrow(),  "syd", true,  plusDays(14), "cdg", "economy",          2, 0, 0, true),
            args(plusDays(3), "mel", false, plusDays(10), "pvg", "premium economy",  2, 4, 0, true),
            args(tomorrow(),  "lax", false, plusDays(10), "cdg", "business",         2, 0, 0, true)
        );
    }
    @ParameterizedTest @MethodSource("happyRows")
    @DisplayName("Happy paths: four valid combinations (attributes initialised)")
    void happyPaths(String dep, String depAir, boolean er, String ret, String dst, String seat, int a, int c, int i, boolean ok) {
        runAndAssert(dep, depAir, er, ret, dst, seat, a, c, i, ok);
    }

    // invalid call should leave fields unchanged
    @Test
    @DisplayName("Invalid input does not change object state (Note 7)")
    void invalidDoesNotChangeState() {
        runAndAssert(tomorrow(), "mel", false, plusDays(7), "mel", "economy", 1, 0, 0, false);
    }
}
