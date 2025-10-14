package flight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Set;

/*
 * FlightSearch validation + state set on success.
 * All string inputs are lowercase (per spec).
 */
public class FlightSearch {

    // fields only set when input is valid
    private String  departureDate;
    private String  departureAirportCode;
    private boolean emergencyRowSeating;
    private String  returnDate;
    private String  destinationAirportCode;
    private String  seatingClass;
    private int     adultPassengerCount;
    private int     childPassengerCount;
    private int     infantPassengerCount;

    // constants / helpers
    private static final Set<String> ALLOWED_AIRPORTS =
            Set.of("syd","mel","lax","cdg","del","pvg","doh");

    private static final Set<String> ALLOWED_CLASSES =
            Set.of("economy","premium economy","business","first");

    private static final DateTimeFormatter STRICT_DMY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                             .withResolverStyle(ResolverStyle.STRICT);

    private static boolean isValidDateFormat(String dmy) {
        try {
            LocalDate.parse(dmy, STRICT_DMY);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static LocalDate toDate(String dmy) {
        return LocalDate.parse(dmy, STRICT_DMY);
    }

    /*
     * Returns true when all C1..C11 rules pass and sets fields.
     * Returns false and leaves fields unchanged otherwise.
     */
    public boolean runFlightSearch(String departureDate,
                                   String departureAirportCode,
                                   boolean emergencyRowSeating,
                                   String returnDate,
                                   String destinationAirportCode,
                                   String seatingClass,
                                   int adultPassengerCount,
                                   int childPassengerCount,
                                   int infantPassengerCount) {

        // C1: total 1..9
        int total = adultPassengerCount + childPassengerCount + infantPassengerCount;
        if (total < 1 || total > 9) return false;

        // C9: class allowed
        if (seatingClass == null || !ALLOWED_CLASSES.contains(seatingClass)) return false;

        // C11: airports allowed & different
        if (departureAirportCode == null || destinationAirportCode == null) return false;
        if (!ALLOWED_AIRPORTS.contains(departureAirportCode)) return false;
        if (!ALLOWED_AIRPORTS.contains(destinationAirportCode)) return false;
        if (departureAirportCode.equals(destinationAirportCode)) return false;

        // C7: strict date format for both
        if (departureDate == null || returnDate == null) return false;
        if (!isValidDateFormat(departureDate) || !isValidDateFormat(returnDate)) return false;

        LocalDate dep = toDate(departureDate);
        LocalDate ret = toDate(returnDate);

        // C6: dep not in past (today OK)
        if (dep.isBefore(LocalDate.now())) return false;

        // C8: return >= dep
        if (ret.isBefore(dep)) return false;

        // C2: children not in emergency rows or first class
        if (childPassengerCount > 0) {
            if (emergencyRowSeating) return false;
            if ("first".equals(seatingClass)) return false;
        }

        // C3: infants not in emergency rows or business class
        if (infantPassengerCount > 0) {
            if (emergencyRowSeating) return false;
            if ("business".equals(seatingClass)) return false;
        }

        // C4: ≤2 children per adult
        if (adultPassengerCount < 0 || childPassengerCount < 0 || infantPassengerCount < 0) return false;
        if (childPassengerCount > 2 * adultPassengerCount) return false;

        // C5: ≤1 infant per adult
        if (infantPassengerCount > adultPassengerCount) return false;

        // C10: only economy can be emergency row
        if (emergencyRowSeating && !"economy".equals(seatingClass)) return false;

        // all good → set state
        this.departureDate          = departureDate;
        this.departureAirportCode   = departureAirportCode;
        this.emergencyRowSeating    = emergencyRowSeating;
        this.returnDate             = returnDate;
        this.destinationAirportCode = destinationAirportCode;
        this.seatingClass           = seatingClass;
        this.adultPassengerCount    = adultPassengerCount;
        this.childPassengerCount    = childPassengerCount;
        this.infantPassengerCount   = infantPassengerCount;

        return true;
    }

    // getters (used by tests)
    public String  getDepartureDate()          { return departureDate; }
    public String  getDepartureAirportCode()   { return departureAirportCode; }
    public boolean isEmergencyRowSeating()     { return emergencyRowSeating; }
    public String  getReturnDate()             { return returnDate; }
    public String  getDestinationAirportCode() { return destinationAirportCode; }
    public String  getSeatingClass()           { return seatingClass; }
    public int     getAdultPassengerCount()    { return adultPassengerCount; }
    public int     getChildPassengerCount()    { return childPassengerCount; }
    public int     getInfantPassengerCount()   { return infantPassengerCount; }
}
