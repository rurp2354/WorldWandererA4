
---

## Implementation Summary

### `FlightSearch.java`
The `runFlightSearch()` method checks 11 validation conditions:
1. Passenger count must be between 1 and 9.  
2. Children cannot be seated in emergency rows or first class.  
3. Infants cannot be seated in emergency rows or business class.  
4. Maximum of 2 children per adult.  
5. Maximum of 1 infant per adult.  
6. Departure date cannot be in the past.  
7. Strict date format **DD/MM/YYYY** with real calendar validation (`ResolverStyle.STRICT`).  
8. Return date cannot be before departure date.  
9. Seating class must be one of: *economy*, *premium economy*, *business*, *first*.  
10. Only **economy class** can have emergency-row seating (updated rule).  
11. Departure and destination airports must be from the approved list and must differ.

If all validations pass, the method initialises the class attributes and returns `true`.  
If any rule fails, it returns `false` and does **not** modify the object state (Note 7 requirement).

---

## Testing Summary

### `FlightSearchTest.java`
- Implemented in **JUnit 5** using `@ParameterizedTest` with `@MethodSource`.  
- Each of the 11 conditions (C1–C11) has **2 data rows** → 22 test runs.  
- A **Happy Path** test adds **4 valid combinations** → +4 runs.  
- A **state-safety test** confirms that invalid inputs do **not** change object attributes → +1 run.  
- **Total = 27 test invocations (all passing).**

### Example Test Breakdown
| Test ID | Description | Valid Rows | Invalid Rows |
|----------|-------------|-------------|---------------|
| C1 | Total passengers between 1 and 9 | 2 | – |
| C3 | Infants not in emergency or business | 1 | 1 |
| C10 | Only economy can have emergency-row seating | 1 | 1 |
| Happy Paths | Four valid combinations (different seat/airport cases) | 4 | – |
| State Safety | Invalid input does not change state | 1 | – |

All tests execute successfully with **0 errors, 0 failures**.

---

## Tools & Environment

- **Java version:** 21  
- **JUnit version:** 5.10+  
- **IDE:** Eclipse 2024-09  
- **Build system:** Eclipse default project structure (no Maven/Gradle)  

---

## How to Run the Tests

1. Open the project in **Eclipse**.  
2. Expand `test/flight`.  
3. Right-click `FlightSearchTest.java` → **Run As → JUnit Test**.  
4. You should see: Runs: 27/27 Errors: 0 Failures: 0
5. If desired, click individual test names to inspect parameter values.

---

## Notes for Markers

- The project follows the updated **Condition 10 wording** and **Note 7** from the teaching staff.  
- Tests verify both the return value (`true`/`false`) and attribute initialization.  
- The repository is private and access has been granted to the timetabled tutor.  
- The exported `.zip` file (for Canvas submission) includes this repo with full source and test folders.

---

## Video Demonstration (Activity 1.4)

The recorded 4-minute video shows:
1. The implementation of `runFlightSearch()` and its validation logic.  
2. The parameterized JUnit test setup (27 invocations).  
3. Running all tests with successful results.  
4. Verification that the repo is live on GitHub.

---

**Author:** Raphael Urpani (s4094828@student.rmit.edu.au)  
**Course:** COSC2639 / Software Engineering  
**Institution:** RMIT University  
**Date:** October 2025


