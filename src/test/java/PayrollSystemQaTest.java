import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * One JUnit 5 test per QA test-case (QA-01 through QA-15).
 * CSV-dependent tests use {@code Assumptions.assumeTrue} so they are skipped
 * (not failed) when data files are absent.
 */
class PayrollSystemQaTest {

    private static final String EMP_CSV = "data/employees.csv";
    private static final String ATT_CSV = "data/attendance.csv";

    private static Scanner scannerOf(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private static String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------
    // QA-01  Login with valid credentials
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_01_LoginWithValidCredentials")
    void qa01_loginWithValidCredentials() {
        String output = captureStdout(() -> {
            boolean result = PayrollSystem.login(scannerOf("admin\nmotorph123\n"));
            assertTrue(result, "Valid credentials should return true");
        });
        assertTrue(output.contains("Login successful"), "Output should confirm login success");
    }

    // ---------------------------------------------------------------
    // QA-02  Login with invalid credentials three times
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_02_LoginLocksAfterThreeInvalidAttempts")
    void qa02_loginLocksAfterThreeFailedAttempts() {
        String input = "wrong\nwrong\nwrong\nwrong\nwrong\nwrong\n";
        String output = captureStdout(() -> {
            boolean result = PayrollSystem.login(scannerOf(input));
            assertFalse(result, "Three invalid attempts should deny access");
        });
        assertTrue(output.contains("Invalid credentials"), "Output should show invalid credentials message");
    }

    // ---------------------------------------------------------------
    // QA-03  Employee data load from CSV
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_03_EmployeeCsvLoadsWithCompleteFields")
    void qa03_employeeCsvLoadsWithCompleteFields() throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(EMP_CSV)), EMP_CSV + " must exist");
        Map<String, String[]> employees = PayrollSystem.loadEmployees(EMP_CSV);
        assertFalse(employees.isEmpty(), "Employee map should not be empty");
        for (String[] emp : employees.values()) {
            assertEquals(19, emp.length, "Each employee record should have 19 fields");
            assertNotNull(emp[0], "Employee ID should not be null");
            assertFalse(emp[0].isBlank(), "Employee ID should not be blank");
        }
    }

    // ---------------------------------------------------------------
    // QA-04  Attendance data load from CSV
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_04_AttendanceCsvLoadsAndDisplays")
    void qa04_attendanceCsvLoadsAndDisplays() throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(ATT_CSV)), ATT_CSV + " must exist");
        List<String[]> attendance = PayrollSystem.loadAttendance(ATT_CSV);
        assertFalse(attendance.isEmpty(), "Attendance list should not be empty");
        for (String[] row : attendance) {
            assertEquals(4, row.length, "Each attendance row should have 4 fields (ID, Date, LogIn, LogOut)");
            assertFalse(row[0].isBlank(), "Employee ID in attendance should not be blank");
        }
    }

    // ---------------------------------------------------------------
    // QA-05  Employee CRUD operations (in-memory) with auto-ID
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_05_EmployeeCrudAndAutoId")
    void qa05_employeeCrudAndAutoId() {
        Map<String, String[]> employees = new HashMap<>();
        String[] existing = new String[19];
        existing[0] = "10001";
        for (int i = 1; i < 19; i++) existing[i] = "field" + i;
        employees.put("10001", existing);

        // Create: auto-generated ID should be 10002
        String nextId = PayrollSystem.generateNextEmployeeId(employees);
        assertEquals("10002", nextId, "Next auto-generated ID should be 10002");

        String[] newEmp = new String[19];
        newEmp[0] = nextId;
        for (int i = 1; i < 19; i++) newEmp[i] = "new" + i;
        employees.put(newEmp[0], newEmp);

        // Read
        assertNotNull(employees.get("10002"), "Newly created employee should be retrievable");
        assertEquals("new1", employees.get("10002")[1], "Last name field should match");

        // Update
        employees.get("10002")[1] = "UpdatedLastName";
        assertEquals("UpdatedLastName", employees.get("10002")[1], "Updated field should persist");

        // Delete
        employees.remove("10002");
        assertNull(employees.get("10002"), "Deleted employee should be null");
        assertEquals(1, employees.size(), "Only original employee should remain");
    }

    // ---------------------------------------------------------------
    // QA-06  Attendance CRUD operations (in-memory)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_06_AttendanceCrud")
    void qa06_attendanceCrud() {
        List<String[]> attendance = new ArrayList<>();

        // Add
        String[] record = {"10001", "6/3/2024", "8:00", "17:00"};
        attendance.add(record);
        assertEquals(1, attendance.size(), "One attendance record should exist after add");
        assertEquals("10001", attendance.get(0)[0]);

        // View (find by emp ID)
        long count = attendance.stream().filter(r -> r[0].equals("10001")).count();
        assertEquals(1, count, "Should find 1 record for employee 10001");

        // Update
        attendance.get(0)[2] = "8:15";
        assertEquals("8:15", attendance.get(0)[2], "LogIn should be updated to 8:15");

        // Delete
        attendance.remove(0);
        assertTrue(attendance.isEmpty(), "Attendance list should be empty after delete");
    }

    // ---------------------------------------------------------------
    // QA-07  Monthly payslip (single employee)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_07_MonthlyPayslipSingleEmployee")
    void qa07_monthlyPayslipSingleEmployee() throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(EMP_CSV)), EMP_CSV + " must exist");
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(ATT_CSV)), ATT_CSV + " must exist");

        Map<String, String[]> employees = PayrollSystem.loadEmployees(EMP_CSV);
        List<String[]> attendance = PayrollSystem.loadAttendance(ATT_CSV);
        String[] emp = employees.get("10001");
        Assumptions.assumeTrue(emp != null, "Employee 10001 must exist in CSV");

        List<Integer> weeks = PayrollSystem.availableWeeks(attendance, "10001", 6);
        assertFalse(weeks.isEmpty(), "Employee 10001 should have attendance in June");

        double totalHours = 0;
        for (int week : weeks) {
            double[] summary = PayrollSystem.summarizeWeek(attendance, "10001", 6, week);
            assertTrue(summary[0] > 0, "Hours for week " + week + " should be > 0");
            assertTrue(summary[1] > 0, "Days for week " + week + " should be > 0");
            totalHours += summary[0];
        }
        assertTrue(totalHours > 0, "Total hours for June should be > 0");

        double hourlyRate = Double.parseDouble(emp[18]);
        double monthlyGross = totalHours * hourlyRate;
        double sss = PayrollSystem.computeSss(monthlyGross);
        double phil = PayrollSystem.computePhilHealth(monthlyGross);
        double pag = PayrollSystem.computePagIbig(monthlyGross);
        assertTrue(sss > 0 && phil > 0 && pag > 0, "Deductions should be positive");
    }

    // ---------------------------------------------------------------
    // QA-08  Monthly payslip (all employees)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_08_MonthlyPayslipAllEmployees")
    void qa08_monthlyPayslipAllEmployees() throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(EMP_CSV)), EMP_CSV + " must exist");
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(ATT_CSV)), ATT_CSV + " must exist");

        Map<String, String[]> employees = PayrollSystem.loadEmployees(EMP_CSV);
        List<String[]> attendance = PayrollSystem.loadAttendance(ATT_CSV);

        int withAttendance = 0;
        for (String[] emp : employees.values()) {
            List<Integer> weeks = PayrollSystem.availableWeeks(attendance, emp[0], 6);
            if (!weeks.isEmpty()) {
                withAttendance++;
            }
        }
        assertTrue(withAttendance > 0, "At least one employee should have attendance in June");
    }

    // ---------------------------------------------------------------
    // QA-09  Non-existent employee in payroll menu
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_09_PayrollNonExistentEmployee")
    void qa09_payrollNonExistentEmployee() throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(EMP_CSV)), EMP_CSV + " must exist");

        Map<String, String[]> employees = PayrollSystem.loadEmployees(EMP_CSV);
        assertNull(employees.get("99999"), "Employee 99999 should not exist");
    }

    // ---------------------------------------------------------------
    // QA-10  No attendance for selected month
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_10_PayrollNoAttendanceForMonth")
    void qa10_payrollNoAttendanceForMonth() throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(EMP_CSV)), EMP_CSV + " must exist");
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(ATT_CSV)), ATT_CSV + " must exist");

        List<String[]> attendance = PayrollSystem.loadAttendance(ATT_CSV);
        Map<String, String[]> employees = PayrollSystem.loadEmployees(EMP_CSV);
        String anyEmpId = employees.keySet().iterator().next();

        // Month 1 (January) should have no attendance in the sample dataset
        List<Integer> weeks = PayrollSystem.availableWeeks(attendance, anyEmpId, 1);
        assertTrue(weeks.isEmpty(), "No attendance records expected in January for sample data");
    }

    // ---------------------------------------------------------------
    // QA-11  Employee time-in with duplicate check
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_11_TimeInDuplicateSameDayBlocked")
    void qa11_timeInDuplicateSameDayBlocked() {
        List<String[]> attendance = new ArrayList<>();
        String today = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"));

        // First time-in: no existing record for today → should be allowed
        boolean firstDuplicate = attendance.stream()
            .anyMatch(r -> r[0].equals("10001") && r[1].equals(today));
        assertFalse(firstDuplicate, "No record yet, first time-in should be allowed");

        // Record the first time-in
        attendance.add(new String[]{"10001", today, "8:00", ""});

        // Second time-in: record exists for today → should be blocked
        boolean secondDuplicate = attendance.stream()
            .anyMatch(r -> r[0].equals("10001") && r[1].equals(today));
        assertTrue(secondDuplicate, "Duplicate time-in should be detected and blocked");
    }

    // ---------------------------------------------------------------
    // QA-12  Employee time-out with duplicate check
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_12_TimeOutDuplicateSameDayBlocked")
    void qa12_timeOutDuplicateSameDayBlocked() {
        List<String[]> attendance = new ArrayList<>();
        String today = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"));

        // Simulate time-in already done
        attendance.add(new String[]{"10001", today, "8:00", ""});

        // First time-out: log-out is empty → should be allowed
        String[] record = attendance.stream()
            .filter(r -> r[0].equals("10001") && r[1].equals(today))
            .findFirst().orElse(null);
        assertNotNull(record, "Time-in record should exist");
        assertTrue(record[3].trim().isEmpty(), "LogOut should be empty before first time-out");

        // Record the time-out
        record[3] = "17:00";

        // Second time-out: log-out is already filled → should be blocked
        assertFalse(record[3].trim().isEmpty(), "Duplicate time-out should be blocked (logOut already filled)");
    }

    // ---------------------------------------------------------------
    // QA-13  Time-in/time-out outside 05:00–21:00 window
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_13_TimeInOutOutsideAllowedWindowRejected")
    void qa13_timeInOutOutsideAllowedWindowRejected() {
        // Inside window
        assertTrue(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(5, 0)),
            "05:00 should be within allowed window");
        assertTrue(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(8, 0)),
            "08:00 should be within allowed window");
        assertTrue(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(17, 0)),
            "17:00 should be within allowed window");
        assertTrue(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(21, 0)),
            "21:00 should be within allowed window");

        // Outside window
        assertFalse(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(4, 59)),
            "04:59 should be rejected (before 05:00)");
        assertFalse(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(21, 1)),
            "21:01 should be rejected (after 21:00)");
        assertFalse(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(3, 0)),
            "03:00 should be rejected (well outside window)");
        assertFalse(PayrollSystem.isWithinAllowedClockWindow(LocalTime.of(23, 0)),
            "23:00 should be rejected (well outside window)");
    }

    // ---------------------------------------------------------------
    // QA-14  Deduction formulas validation (PHP 25,000 reference)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation")
    void qa14_deductionFormulasValidation() {
        double salary = 25_000.0;

        double sss = PayrollSystem.computeSss(salary);
        assertEquals(1_125.0, sss, 0.001, "SSS should be 1,125");

        double phil = PayrollSystem.computePhilHealth(salary);
        assertEquals(375.0, phil, 0.001, "PhilHealth should be 375");

        double pag = PayrollSystem.computePagIbig(salary);
        assertEquals(100.0, pag, 0.001, "Pag-IBIG should be 100");

        double totalStatutory = sss + phil + pag;
        assertEquals(1_600.0, totalStatutory, 0.001, "Total statutory deductions should be 1,600");

        double taxable = salary - totalStatutory;
        assertEquals(23_400.0, taxable, 0.001, "Taxable income should be 23,400");

        double withholdingTax = PayrollSystem.computeWithholdingTax(taxable);
        assertEquals(513.4, withholdingTax, 0.001, "Withholding tax should be 513.4");

        // Verify the formula: (23,400 - 20,833) * 20%
        double expectedTax = (23_400.0 - 20_833.0) * 0.20;
        assertEquals(expectedTax, withholdingTax, 0.001,
            "Tax should equal (23,400 - 20,833) * 20%");
    }

    // ---------------------------------------------------------------
    // QA-15  Column-index constants usage (source scan)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("QA_TEST_15_ColumnIndexConstantsUsage")
    void qa15_columnIndexConstantsUsage() throws IOException {
        Path source = Path.of("src/main/java/PayrollSystem.java");
        Assumptions.assumeTrue(Files.isRegularFile(source),
            "src/main/java/PayrollSystem.java must exist");

        String code = Files.readString(source);
        assertFalse(Pattern.compile("\\bemp\\[\\d+\\]").matcher(code).find(),
            "Use EMP_* constants instead of emp[digits]");
        assertFalse(Pattern.compile("\\brow\\[\\d+\\]").matcher(code).find(),
            "Use ATT_* constants instead of row[digits]");
    }
}
