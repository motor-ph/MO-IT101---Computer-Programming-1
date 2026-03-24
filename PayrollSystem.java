import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * MotorPH Payroll System.
 * Data is loaded from CSV files in the data/ folder and processed in-memory
 * using primitive arrays and collections.
 */
public class PayrollSystem {
    private static final String EMPLOYEE_FILE = "data/employees.csv";
    private static final String ATTENDANCE_FILE = "data/attendance.csv";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "motorph123";
    private static final int LOGIN_ATTEMPTS = 3;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");
    private static final LocalTime OFFICE_START = LocalTime.of(8, 0);
    private static final LocalTime GRACE_CUTOFF = LocalTime.of(8, 10);
    private static final LocalTime OFFICE_END = LocalTime.of(17, 0);
    private static final LocalTime EARLIEST_ALLOWED = LocalTime.of(5, 0);
    private static final LocalTime LATEST_ALLOWED = LocalTime.of(21, 0);
    private static final int LUNCH_BREAK_MINUTES = 60;
    private static final WeekFields WEEK_FIELDS = WeekFields.ISO;

    // Employee CSV column indexes (data/employees.csv)
    private static final int EMP_ID = 0;
    private static final int EMP_LAST_NAME = 1;
    private static final int EMP_FIRST_NAME = 2;
    private static final int EMP_BIRTHDAY = 3;
    private static final int EMP_ADDRESS = 4;
    private static final int EMP_PHONE = 5;
    private static final int EMP_SSS = 6;
    private static final int EMP_PHILHEALTH = 7;
    private static final int EMP_TIN = 8;
    private static final int EMP_PAGIBIG = 9;
    private static final int EMP_STATUS = 10;
    private static final int EMP_POSITION = 11;
    private static final int EMP_SUPERVISOR = 12;
    private static final int EMP_BASIC_SALARY = 13;
    private static final int EMP_RICE_SUBSIDY = 14;
    private static final int EMP_PHONE_ALLOWANCE = 15;
    private static final int EMP_CLOTHING_ALLOWANCE = 16;
    private static final int EMP_GROSS_SEMI_MONTHLY = 17;
    private static final int EMP_HOURLY_RATE = 18;
    private static final int EMP_FIELD_COUNT = 19;

    // Attendance in-memory column indexes
    private static final int ATT_EMP_ID = 0;
    private static final int ATT_DATE = 1;
    private static final int ATT_LOG_IN = 2;
    private static final int ATT_LOG_OUT = 3;
    private static final int ATT_FIELD_COUNT = 4;
    private static final int WEEK_SUMMARY_HOURS = 0;
    private static final int WEEK_SUMMARY_DAYS = 1;

    // Attendance CSV source indexes (EmpNum, LastName, FirstName, Date, LogIn, LogOut)
    private static final int CSV_ATT_EMP_ID = 0;
    private static final int CSV_ATT_DATE = 3;
    private static final int CSV_ATT_LOG_IN = 4;
    private static final int CSV_ATT_LOG_OUT = 5;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Startup flow: load data files, authenticate, then launch the menu.
            Map<String, String[]> employees = loadEmployees(EMPLOYEE_FILE);
            List<String[]> attendance = loadAttendance(ATTENDANCE_FILE);

            if (employees.isEmpty() || attendance.isEmpty()) {
                System.out.println("Unable to continue. Employee or attendance data is empty.");
                return;
            }

            if (!login(scanner)) {
                System.out.println("Access denied. Exiting program.");
                return;
            }

            runMenu(scanner, employees, attendance);
        } catch (IOException ex) {
            System.out.println("Failed to load data files: " + ex.getMessage());
        }
    }

    private static boolean login(Scanner scanner) {
        System.out.println("========================================");
        System.out.println("        MotorPH Payroll System Login");
        System.out.println("========================================");

        for (int attempt = 1; attempt <= LOGIN_ATTEMPTS; attempt++) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                System.out.println("Login successful.\n");
                return true;
            }
            int remaining = LOGIN_ATTEMPTS - attempt;
            if (remaining > 0) {
                System.out.println("Invalid credentials. Attempts remaining: " + remaining);
            }
        }
        return false;
    }

    // ===== Menu Methods =====
    private static void runMenu(Scanner scanner, Map<String, String[]> employees, List<String[]> attendance) {
        boolean running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println("       MotorPH Payroll System");
            System.out.println("========================================");
            System.out.println("1) Manage Employees");
            System.out.println("2) Manage Attendance");
            System.out.println("3) Manage Payroll");
            System.out.println("4) Employee Time-In/Time-Out");
            System.out.println("5) About MotorPH");
            System.out.println("6) Exit");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    manageEmployeesMenu(scanner, employees);
                    break;
                case "2":
                    manageAttendanceMenu(scanner, attendance);
                    break;
                case "3":
                    managePayrollMenu(scanner, employees, attendance);
                    break;
                case "4":
                    employeeTimeMenu(scanner, employees, attendance);
                    break;
                case "5":
                    showAboutMotorPH();
                    break;
                case "6":
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void managePayrollMenu(Scanner scanner, Map<String, String[]> employees, List<String[]> attendance) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Manage Payroll ---");
            System.out.println("1) Generate Monthly Payslip (Single Employee)");
            System.out.println("2) Generate Monthly Payslip (All Employees)");
            System.out.println("3) Back");
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    generatePayrollSingle(scanner, employees, attendance);
                    break;
                case "2":
                    generatePayrollAll(scanner, employees, attendance);
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void employeeTimeMenu(Scanner scanner, Map<String, String[]> employees, List<String[]> attendance) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Employee Time-In/Time-Out ---");
            System.out.println("1) Time-In");
            System.out.println("2) Time-Out");
            System.out.println("3) Back");
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    employeeTimeIn(scanner, employees, attendance);
                    break;
                case "2":
                    employeeTimeOut(scanner, employees, attendance);
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void manageEmployeesMenu(Scanner scanner, Map<String, String[]> employees) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Manage Employees ---");
            System.out.println("1) View All Employees");
            System.out.println("2) View Employee Details");
            System.out.println("3) Create New Employee");
            System.out.println("4) Update Employee");
            System.out.println("5) Delete Employee");
            System.out.println("6) Employee Form Guide");
            System.out.println("7) Back");
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllEmployees(employees);
                    break;
                case "2":
                    viewEmployeeDetails(scanner, employees);
                    break;
                case "3":
                    createEmployee(scanner, employees);
                    break;
                case "4":
                    updateEmployee(scanner, employees);
                    break;
                case "5":
                    deleteEmployee(scanner, employees);
                    break;
                case "6":
                    showEmployeeFormGuide();
                    break;
                case "7":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void manageAttendanceMenu(Scanner scanner, List<String[]> attendance) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Manage Attendance ---");
            System.out.println("1) View Employee Attendance");
            System.out.println("2) Add Attendance Record");
            System.out.println("3) Update Attendance Record");
            System.out.println("4) Delete Attendance Record");
            System.out.println("5) Attendance Form Guide");
            System.out.println("6) Back");
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewEmployeeAttendance(scanner, attendance);
                    break;
                case "2":
                    addAttendance(scanner, attendance);
                    break;
                case "3":
                    updateAttendance(scanner, attendance);
                    break;
                case "4":
                    deleteAttendance(scanner, attendance);
                    break;
                case "5":
                    showAttendanceFormGuide();
                    break;
                case "6":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void generatePayrollSingle(Scanner scanner, Map<String, String[]> employees, List<String[]> attendance) {
        System.out.print("Enter Employee Number: ");
        String empNum = scanner.nextLine().trim();
        String[] emp = employees.get(empNum);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }

        showMonthChoices();
        int month = readIntInRange(scanner, "Select month (1-12): ", 1, 12);
        List<Integer> weeks = availableWeeks(attendance, empNum, month);
        if (weeks.isEmpty()) {
            System.out.println("No attendance records found for that employee/month.");
            return;
        }
        printMonthlyPayslip(emp, attendance, month, weeks);
    }

    private static void generatePayrollAll(Scanner scanner, Map<String, String[]> employees, List<String[]> attendance) {
        showMonthChoices();
        int month = readIntInRange(scanner, "Select month (1-12): ", 1, 12);
        int printed = 0;
        for (String[] emp : employees.values()) {
            List<Integer> weeks = availableWeeks(attendance, emp[EMP_ID], month);
            if (!weeks.isEmpty()) {
                printMonthlyPayslip(emp, attendance, month, weeks);
                printed++;
            }
        }
        if (printed == 0) {
            System.out.println("No attendance records found for the selected month.");
        } else {
            System.out.println("Generated payslips: " + printed);
        }
    }

    private static void printMonthlyPayslip(String[] emp, List<String[]> attendance, int month, List<Integer> weeks) {
        // Build weekly breakdown first, then compute monthly gross and monthly deductions.
        double hourlyRate = Double.parseDouble(normalizeNumber(emp[EMP_HOURLY_RATE]));
        int[] weekNumbers = new int[weeks.size()];
        int[] weekDays = new int[weeks.size()];
        double[] weekHours = new double[weeks.size()];
        double[] weekGross = new double[weeks.size()];
        int totalDays = 0;
        double totalHours = 0.0;
        double monthlyGross = 0.0;

        for (int i = 0; i < weeks.size(); i++) {
            int weekNo = weeks.get(i);
            double[] week = summarizeWeek(attendance, emp[EMP_ID], month, weekNo);
            double hours = week[WEEK_SUMMARY_HOURS];
            int days = (int) week[WEEK_SUMMARY_DAYS];
            double gross = hours * hourlyRate;
            weekNumbers[i] = weekNo;
            weekDays[i] = days;
            weekHours[i] = hours;
            weekGross[i] = gross;
            totalDays += days;
            totalHours += hours;
            monthlyGross += gross;
        }

        double sss = computeSss(monthlyGross);
        double philHealth = computePhilHealth(monthlyGross);
        double pagIbig = computePagIbig(monthlyGross);
        double taxableIncome = monthlyGross - (sss + philHealth + pagIbig);
        double withholdingTax = computeWithholdingTax(taxableIncome);
        double totalDeductions = sss + philHealth + pagIbig + withholdingTax;
        double netMonthly = monthlyGross - totalDeductions;

        System.out.println("\n==========================================================");
        System.out.println("                 MONTHLY PAYROLL REPORT");
        System.out.println("==========================================================");
        System.out.printf("Employee #: %-12s Name: %s %s%n", emp[EMP_ID], emp[EMP_FIRST_NAME], emp[EMP_LAST_NAME]);
        System.out.printf("Birthday  : %-12s Position: %s%n", emp[EMP_BIRTHDAY], emp[EMP_POSITION]);
        System.out.printf("Pay Period: %s           Hourly Rate: %s%n", monthName(month), money(hourlyRate));
        System.out.println("----------------------------------------------------------");
        System.out.println("             WEEKLY ATTENDANCE BREAKDOWN");
        System.out.println("----------------------------------------------------------");
        System.out.printf("%-6s %-6s %-10s %-16s%n", "Week", "Days", "Hours", "Weekly Gross");
        for (int i = 0; i < weekNumbers.length; i++) {
            System.out.printf("%-6d %-6d %-10.2f %-16s%n",
                weekNumbers[i], weekDays[i], weekHours[i], money(weekGross[i]));
        }
        System.out.println("----------------------------------------------------------");
        System.out.printf("%-6s %-6d %-10.2f %-16s%n", "Total", totalDays, totalHours, money(monthlyGross));
        System.out.println("==========================================================");
        System.out.println("                   MONTHLY DEDUCTIONS");
        System.out.println("----------------------------------------------------------");
        System.out.printf("SSS               : %s%n", money(sss));
        System.out.printf("PhilHealth        : %s%n", money(philHealth));
        System.out.printf("Pag-IBIG          : %s%n", money(pagIbig));
        System.out.printf("Withholding Tax   : %s%n", money(withholdingTax));
        System.out.printf("Total Deductions  : %s%n", money(totalDeductions));
        System.out.println("----------------------------------------------------------");
        System.out.printf("GROSS MONTHLY PAY : %s%n", money(monthlyGross));
        System.out.printf("NET MONTHLY SALARY: %s%n", money(netMonthly));
        System.out.println("==========================================================");
    }

    // ===== Employee CRUD =====
    private static void viewAllEmployees(Map<String, String[]> employees) {
        System.out.println("\n========================================");
        System.out.println("           All Employees");
        System.out.println("========================================");
        System.out.printf("%-10s %-28s %-12s %-20s %-12s%n", "Emp #", "Name", "Birthday", "Position", "Status");
        System.out.println("----------------------------------------------------------");
        for (String[] emp : employees.values()) {
            System.out.printf("%-10s %-28s %-12s %-20s %-12s%n",
                emp[EMP_ID], (emp[EMP_FIRST_NAME] + " " + emp[EMP_LAST_NAME]),
                emp[EMP_BIRTHDAY], emp[EMP_POSITION], emp[EMP_STATUS]);
        }
    }

    private static void viewEmployeeDetails(Scanner scanner, Map<String, String[]> employees) {
        System.out.print("Enter Employee Number: ");
        String empNo = scanner.nextLine().trim();
        String[] emp = employees.get(empNo);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }
        String[] labels = employeeFieldLabels();
        System.out.println("\n========================================");
        System.out.println("         Employee Full Details");
        System.out.println("========================================");
        for (int i = 0; i < EMP_FIELD_COUNT; i++) {
            System.out.printf("%-22s : %s%n", labels[i], emp[i]);
        }
    }

    private static void createEmployee(Scanner scanner, Map<String, String[]> employees) {
        String[] labels = employeeFieldLabels();
        String[] guides = employeeFieldGuides();
        String[] emp = new String[EMP_FIELD_COUNT];
        emp[EMP_ID] = generateNextEmployeeId(employees);
        System.out.println("Auto-generated Employee Number: " + emp[EMP_ID]);
        for (int i = 1; i < EMP_FIELD_COUNT; i++) {
            System.out.print(labels[i] + " " + guides[i] + ": ");
            emp[i] = scanner.nextLine().trim();
        }
        employees.put(emp[EMP_ID], emp);
        System.out.println("Employee created successfully.");
    }

    private static void updateEmployee(Scanner scanner, Map<String, String[]> employees) {
        System.out.print("Enter Employee Number to update: ");
        String empNo = scanner.nextLine().trim();
        String[] emp = employees.get(empNo);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }
        String[] labels = employeeFieldLabels();
        for (int i = 1; i < EMP_FIELD_COUNT; i++) {
            System.out.print(labels[i] + " [" + emp[i] + "] (Enter to keep): ");
            String input = scanner.nextLine();
            if (!input.trim().isEmpty()) {
                emp[i] = input.trim();
            }
        }
        employees.put(empNo, emp);
        System.out.println("Employee updated successfully.");
    }

    private static void deleteEmployee(Scanner scanner, Map<String, String[]> employees) {
        System.out.print("Enter Employee Number to delete: ");
        String empNo = scanner.nextLine().trim();
        if (!employees.containsKey(empNo)) {
            System.out.println("Employee not found.");
            return;
        }
        System.out.print("Are you sure? Type YES to confirm: ");
        String confirm = scanner.nextLine().trim();
        if ("YES".equals(confirm)) {
            employees.remove(empNo);
            System.out.println("Employee deleted.");
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    private static void showEmployeeFormGuide() {
        System.out.println("\n--- Employee Form Guide ---");
        String[] labels = employeeFieldLabels();
        String[] guides = employeeFieldGuides();
        for (int i = 1; i < EMP_FIELD_COUNT; i++) {
            System.out.println("- " + labels[i] + " " + guides[i]);
        }
    }

    // ===== Attendance CRUD =====
    private static void viewEmployeeAttendance(Scanner scanner, List<String[]> attendance) {
        System.out.print("Enter Employee Number to view attendance: ");
        String empNo = scanner.nextLine().trim();
        System.out.println("\n========================================");
        System.out.println("         Employee Attendance");
        System.out.println("========================================");
        System.out.printf("%-12s %-8s %-8s%n", "Date", "LogIn", "LogOut");
        System.out.println("----------------------------------------");
        int count = 0;
        for (String[] row : attendance) {
            if (row[ATT_EMP_ID].equals(empNo)) {
                System.out.printf("%-12s %-8s %-8s%n", row[ATT_DATE], row[ATT_LOG_IN], row[ATT_LOG_OUT]);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No attendance records found for employee " + empNo + ".");
        } else {
            System.out.println("----------------------------------------");
            System.out.println("Total records: " + count);
        }
    }

    private static void addAttendance(Scanner scanner, List<String[]> attendance) {
        String[] row = new String[ATT_FIELD_COUNT];
        System.out.print("Employee Number: ");
        row[ATT_EMP_ID] = scanner.nextLine().trim();
        System.out.print("Date (M/D/YYYY): ");
        row[ATT_DATE] = scanner.nextLine().trim();
        System.out.print("LogIn (H:MM): ");
        row[ATT_LOG_IN] = scanner.nextLine().trim();
        System.out.print("LogOut (H:MM): ");
        row[ATT_LOG_OUT] = scanner.nextLine().trim();
        attendance.add(row);
        System.out.println("Attendance record added.");
    }

    private static void updateAttendance(Scanner scanner, List<String[]> attendance) {
        System.out.print("Employee Number: ");
        String empNo = scanner.nextLine().trim();
        System.out.print("Date to update (M/D/YYYY): ");
        String date = scanner.nextLine().trim();
        for (String[] row : attendance) {
            if (row[ATT_EMP_ID].equals(empNo) && row[ATT_DATE].equals(date)) {
                System.out.print("New LogIn [" + row[ATT_LOG_IN] + "] (Enter to keep): ");
                String newLogIn = scanner.nextLine().trim();
                System.out.print("New LogOut [" + row[ATT_LOG_OUT] + "] (Enter to keep): ");
                String newLogOut = scanner.nextLine().trim();
                if (!newLogIn.isEmpty()) {
                    row[ATT_LOG_IN] = newLogIn;
                }
                if (!newLogOut.isEmpty()) {
                    row[ATT_LOG_OUT] = newLogOut;
                }
                System.out.println("Attendance updated.");
                return;
            }
        }
        System.out.println("Attendance record not found.");
    }

    private static void deleteAttendance(Scanner scanner, List<String[]> attendance) {
        System.out.print("Employee Number: ");
        String empNo = scanner.nextLine().trim();
        System.out.print("Date to delete (M/D/YYYY): ");
        String date = scanner.nextLine().trim();
        for (int i = 0; i < attendance.size(); i++) {
            String[] row = attendance.get(i);
            if (row[ATT_EMP_ID].equals(empNo) && row[ATT_DATE].equals(date)) {
                System.out.print("Type YES to confirm delete: ");
                String confirm = scanner.nextLine().trim();
                if ("YES".equals(confirm)) {
                    attendance.remove(i);
                    System.out.println("Attendance deleted.");
                } else {
                    System.out.println("Delete cancelled.");
                }
                return;
            }
        }
        System.out.println("Attendance record not found.");
    }

    private static void showAttendanceFormGuide() {
        System.out.println("\n--- Attendance Form Guide ---");
        System.out.println("- Employee Number: existing employee ID (e.g. 10001)");
        System.out.println("- Date: M/D/YYYY (e.g. 6/3/2024)");
        System.out.println("- LogIn: H:MM 24-hour format (e.g. 8:59)");
        System.out.println("- LogOut: H:MM 24-hour format (e.g. 17:30)");
    }

    // ===== Employee Time-In/Time-Out =====
    private static void employeeTimeIn(Scanner scanner, Map<String, String[]> employees, List<String[]> attendance) {
        // Time-in: validate employee, enforce allowed time window, reject duplicates,
        // then persist both in-memory and CSV.
        System.out.print("Enter Employee Number: ");
        String empNo = scanner.nextLine().trim();
        String[] emp = employees.get(empNo);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }

        LocalTime nowTime = LocalTime.now().withSecond(0).withNano(0);
        if (!isWithinAllowedClockWindow(nowTime)) {
            System.out.println("Time-in not allowed right now. Allowed time is 05:00 to 21:00 only.");
            return;
        }

        String today = LocalDate.now().format(DATE_FMT);
        for (String[] row : attendance) {
            if (row[ATT_EMP_ID].equals(empNo) && row[ATT_DATE].equals(today)) {
                System.out.println("You have already timed in today.");
                return;
            }
        }

        String[] row = new String[] {empNo, today, nowTime.format(TIME_FMT), ""};
        attendance.add(row);
        try {
            appendAttendanceCsv(row, emp);
            System.out.println("Time-in recorded at " + row[ATT_LOG_IN] + ".");
        } catch (IOException ex) {
            System.out.println("Time-in saved in memory but failed to write CSV: " + ex.getMessage());
        }
    }

    private static void employeeTimeOut(Scanner scanner, Map<String, String[]> employees, List<String[]> attendance) {
        // Time-out: find same-day open record, set log-out once, then rewrite CSV
        // to persist the updated value for that specific entry.
        System.out.print("Enter Employee Number: ");
        String empNo = scanner.nextLine().trim();
        String[] emp = employees.get(empNo);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }

        LocalTime nowTime = LocalTime.now().withSecond(0).withNano(0);
        if (!isWithinAllowedClockWindow(nowTime)) {
            System.out.println("Time-out not allowed right now. Allowed time is 05:00 to 21:00 only.");
            return;
        }

        String today = LocalDate.now().format(DATE_FMT);
        for (String[] row : attendance) {
            if (row[ATT_EMP_ID].equals(empNo) && row[ATT_DATE].equals(today)) {
                if (row[ATT_LOG_OUT] != null && !row[ATT_LOG_OUT].trim().isEmpty()) {
                    System.out.println("You have already timed out today.");
                    return;
                }
                row[ATT_LOG_OUT] = nowTime.format(TIME_FMT);
                try {
                    rewriteAttendanceCsv(attendance, employees);
                    System.out.println("Time-out recorded at " + row[ATT_LOG_OUT] + ".");
                } catch (IOException ex) {
                    System.out.println("Time-out saved in memory but failed to write CSV: " + ex.getMessage());
                }
                return;
            }
        }
        System.out.println("No time-in record found for today.");
    }

    private static boolean isWithinAllowedClockWindow(LocalTime time) {
        return !time.isBefore(EARLIEST_ALLOWED) && !time.isAfter(LATEST_ALLOWED);
    }

    // ===== CSV Write Helpers =====
    private static void appendAttendanceCsv(String[] row, String[] emp) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
            Path.of(ATTENDANCE_FILE),
            StandardOpenOption.APPEND
        )) {
            writer.newLine();
            writer.write(csv(row[ATT_EMP_ID]) + "," + csv(emp[EMP_LAST_NAME]) + "," + csv(emp[EMP_FIRST_NAME]) + ","
                + csv(row[ATT_DATE]) + "," + csv(row[ATT_LOG_IN]) + "," + csv(row[ATT_LOG_OUT]));
        }
    }

    private static void rewriteAttendanceCsv(List<String[]> attendance, Map<String, String[]> employees) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
            Path.of(ATTENDANCE_FILE),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )) {
            writer.write("EmpNum,LastName,FirstName,Date,LogIn,LogOut");
            for (String[] row : attendance) {
                writer.newLine();
                String[] emp = employees.get(row[ATT_EMP_ID]);
                String lastName = emp == null ? "" : emp[EMP_LAST_NAME];
                String firstName = emp == null ? "" : emp[EMP_FIRST_NAME];
                writer.write(csv(row[ATT_EMP_ID]) + "," + csv(lastName) + "," + csv(firstName) + ","
                    + csv(row[ATT_DATE]) + "," + csv(row[ATT_LOG_IN]) + "," + csv(row[ATT_LOG_OUT]));
            }
        }
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    private static void showAboutMotorPH() {
        System.out.println("\n==============================================================");
        System.out.println(" __  __       _             ____  _   _ ");
        System.out.println("|  \\/  | ___ | |_ ___  _ __|  _ \\| | | |");
        System.out.println("| |\\/| |/ _ \\| __/ _ \\| '__| |_) | |_| |");
        System.out.println("| |  | | (_) | || (_) | |  |  __/|  _  |");
        System.out.println("|_|  |_|\\___/ \\__\\___/|_|  |_|   |_| |_|");
        System.out.println("==============================================================");
        System.out.println("About Us");
        System.out.println("MotorPH was established in 2020 to provide private transportation");
        System.out.println("options to Filipinos. Our goal is to be the first choice for");
        System.out.println("Filipinos searching for competitive and affordable motorcycles.");
        System.out.println();
        System.out.println("Know Our Story");
        System.out.println("This video is intended to provide you with an overview of the");
        System.out.println("company profile, business model and objectives.");
        System.out.println();
        System.out.println("Business Operations");
        System.out.println();
        System.out.println("Generic Overview: Payroll System");
        System.out.println("This video is intended to depict a generic overview of our");
        System.out.println("processes when it comes to the payroll of our employees.");
        System.out.println();
        System.out.println("Finance Department Payroll Processes");
        System.out.println("This video is intended to depict the different processes of the");
        System.out.println("finance department in the processing of the payroll of employees.");
        System.out.println();
        System.out.println("Meet Our Team");
        System.out.println("Our company is composed of a small group of uniquely talented");
        System.out.println("individuals who are dedicated in achieving our business goals");
        System.out.println("and objectives.");
        System.out.println("==============================================================");
    }

    // ===== CSV Read Helpers =====
    private static Map<String, String[]> loadEmployees(String filePath) throws IOException {
        // Loads all 19 employee columns directly from CSV (no hardcoded employee list).
        Map<String, String[]> employees = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] cols = parseCsvLine(line);
                if (cols.length < EMP_FIELD_COUNT) {
                    continue;
                }
                String[] emp = new String[EMP_FIELD_COUNT];
                for (int i = 0; i < EMP_FIELD_COUNT; i++) {
                    emp[i] = cols[i].trim();
                }
                // Salary/rate fields are normalized so commas/quotes do not break math parsing.
                emp[EMP_BASIC_SALARY] = normalizeNumber(emp[EMP_BASIC_SALARY]);
                emp[EMP_RICE_SUBSIDY] = normalizeNumber(emp[EMP_RICE_SUBSIDY]);
                emp[EMP_PHONE_ALLOWANCE] = normalizeNumber(emp[EMP_PHONE_ALLOWANCE]);
                emp[EMP_CLOTHING_ALLOWANCE] = normalizeNumber(emp[EMP_CLOTHING_ALLOWANCE]);
                emp[EMP_GROSS_SEMI_MONTHLY] = normalizeNumber(emp[EMP_GROSS_SEMI_MONTHLY]);
                emp[EMP_HOURLY_RATE] = normalizeNumber(emp[EMP_HOURLY_RATE]);
                employees.put(emp[EMP_ID], emp);
            }
        }
        return employees;
    }

    private static List<String[]> loadAttendance(String filePath) throws IOException {
        // Attendance CSV columns are: EmpNum, LastName, FirstName, Date, LogIn, LogOut.
        // We keep only EmpNum/Date/LogIn/LogOut in memory.
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] cols = parseCsvLine(line);
                if (cols.length < 6) {
                    continue;
                }
                rows.add(new String[] {
                    cols[CSV_ATT_EMP_ID].trim(),
                    cols[CSV_ATT_DATE].trim(),
                    cols[CSV_ATT_LOG_IN].trim(),
                    cols[CSV_ATT_LOG_OUT].trim()
                });
            }
        }
        return rows;
    }

    private static List<Integer> availableWeeks(List<String[]> attendance, String empNum, int month) {
        List<Integer> weeks = new ArrayList<>();
        for (String[] row : attendance) {
            if (!row[ATT_EMP_ID].equals(empNum)) {
                continue;
            }
            try {
                LocalDate date = LocalDate.parse(row[ATT_DATE], DATE_FMT);
                if (date.getMonthValue() != month) {
                    continue;
                }
                int weekNo = date.get(WEEK_FIELDS.weekOfMonth());
                if (!weeks.contains(weekNo)) {
                    weeks.add(weekNo);
                }
            } catch (DateTimeParseException ignored) {
                // skip bad row
            }
        }
        return weeks;
    }

    private static double[] summarizeWeek(List<String[]> attendance, String empNum, int month, int weekOfMonth) {
        // Payroll timeframe rules:
        // - Log-ins up to 8:10 count as 8:00 start (grace period)
        // - Work beyond 5:00 PM is excluded
        // - 1 hour lunch break is deducted for worked periods over 1 hour
        double totalHours = 0.0;
        int workedDays = 0;
        for (String[] row : attendance) {
            if (!row[ATT_EMP_ID].equals(empNum)) {
                continue;
            }
            try {
                LocalDate date = LocalDate.parse(row[ATT_DATE], DATE_FMT);
                if (date.getMonthValue() != month || date.get(WEEK_FIELDS.weekOfMonth()) != weekOfMonth) {
                    continue;
                }
                LocalTime logIn = LocalTime.parse(row[ATT_LOG_IN], TIME_FMT);
                LocalTime logOut = LocalTime.parse(row[ATT_LOG_OUT], TIME_FMT);
                LocalTime effectiveStart = logIn.isAfter(GRACE_CUTOFF) ? logIn : OFFICE_START;
                LocalTime effectiveEnd = logOut.isAfter(OFFICE_END) ? OFFICE_END : logOut;
                long minutes = Duration.between(effectiveStart, effectiveEnd).toMinutes();
                if (minutes > 0) {
                    if (minutes > LUNCH_BREAK_MINUTES) {
                        minutes -= LUNCH_BREAK_MINUTES;
                    }
                    totalHours += minutes / 60.0;
                    workedDays++;
                }
            } catch (DateTimeParseException ignored) {
                // skip bad row
            }
        }
        return new double[] {totalHours, workedDays};
    }

    private static String[] parseCsvLine(String line) {
        // Handles commas inside quoted CSV fields and escaped quotes ("").
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private static String normalizeNumber(String value) {
        return value.replace(",", "").replace("\"", "").trim();
    }

    // ===== Utility Methods =====
    private static String generateNextEmployeeId(Map<String, String[]> employees) {
        int maxId = 10000;
        for (String key : employees.keySet()) {
            try {
                int id = Integer.parseInt(key.trim());
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException ignored) {
                // skip malformed ids
            }
        }
        return String.valueOf(maxId + 1);
    }

    private static String[] employeeFieldLabels() {
        String[] labels = new String[EMP_FIELD_COUNT];
        labels[EMP_ID] = "Employee #";
        labels[EMP_LAST_NAME] = "Last Name";
        labels[EMP_FIRST_NAME] = "First Name";
        labels[EMP_BIRTHDAY] = "Birthday";
        labels[EMP_ADDRESS] = "Address";
        labels[EMP_PHONE] = "Phone Number";
        labels[EMP_SSS] = "SSS #";
        labels[EMP_PHILHEALTH] = "Philhealth #";
        labels[EMP_TIN] = "TIN #";
        labels[EMP_PAGIBIG] = "Pag-ibig #";
        labels[EMP_STATUS] = "Status";
        labels[EMP_POSITION] = "Position";
        labels[EMP_SUPERVISOR] = "Immediate Supervisor";
        labels[EMP_BASIC_SALARY] = "Basic Salary";
        labels[EMP_RICE_SUBSIDY] = "Rice Subsidy";
        labels[EMP_PHONE_ALLOWANCE] = "Phone Allowance";
        labels[EMP_CLOTHING_ALLOWANCE] = "Clothing Allowance";
        labels[EMP_GROSS_SEMI_MONTHLY] = "Gross Semi-monthly Rate";
        labels[EMP_HOURLY_RATE] = "Hourly Rate";
        return labels;
    }

    private static String[] employeeFieldGuides() {
        String[] guides = new String[EMP_FIELD_COUNT];
        guides[EMP_ID] = "(auto-generated)";
        guides[EMP_LAST_NAME] = "(e.g. Garcia)";
        guides[EMP_FIRST_NAME] = "(e.g. Manuel III)";
        guides[EMP_BIRTHDAY] = "(MM/DD/YYYY)";
        guides[EMP_ADDRESS] = "(full address)";
        guides[EMP_PHONE] = "(e.g. 966-860-270)";
        guides[EMP_SSS] = "(XX-XXXXXXX-X)";
        guides[EMP_PHILHEALTH] = "(numeric string)";
        guides[EMP_TIN] = "(XXX-XXX-XXX-000)";
        guides[EMP_PAGIBIG] = "(numeric string)";
        guides[EMP_STATUS] = "(Regular/Probationary)";
        guides[EMP_POSITION] = "(job title)";
        guides[EMP_SUPERVISOR] = "(e.g. Lim, Antonio)";
        guides[EMP_BASIC_SALARY] = "(e.g. 52670)";
        guides[EMP_RICE_SUBSIDY] = "(e.g. 1500)";
        guides[EMP_PHONE_ALLOWANCE] = "(e.g. 1000)";
        guides[EMP_CLOTHING_ALLOWANCE] = "(e.g. 1000)";
        guides[EMP_GROSS_SEMI_MONTHLY] = "(e.g. 26335)";
        guides[EMP_HOURLY_RATE] = "(e.g. 313.51)";
        return guides;
    }

    private static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(raw);
                if (num >= min && num <= max) {
                    return num;
                }
            } catch (NumberFormatException ignored) {
                // keep prompting
            }
            System.out.println("Invalid input. Enter a number between " + min + " and " + max + ".");
        }
    }

    private static void showMonthChoices() {
        System.out.println("Available Month Options (hardcoded):");
        System.out.println("1=Jan  2=Feb  3=Mar  4=Apr  5=May  6=Jun");
        System.out.println("7=Jul  8=Aug  9=Sep 10=Oct 11=Nov 12=Dec");
    }

    private static String money(double value) {
        return String.format("PHP %,.2f", value);
    }

    private static String monthName(int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "Unknown";
        }
    }

    // ===== Government Deduction Calculations =====
    private static double computeSss(double basicSalary) {
        // SSS uses bracketed contribution amounts with salary floors/caps.
        if (basicSalary < 3250) {
            return 135.0;
        }
        if (basicSalary >= 24750) {
            return 1125.0;
        }
        int bracket = (int) Math.floor((basicSalary - 3250) / 500.0);
        return 157.5 + (bracket * 22.5);
    }

    private static double computePhilHealth(double monthlyGross) {
        // Reference logic: 300 min total contribution, 1800 cap, employee share is half.
        double totalContribution = 300.0;
        if (monthlyGross > 10000) {
            if (monthlyGross >= 60000) {
                totalContribution = 1800.0;
            } else {
                totalContribution = monthlyGross * 0.03;
            }
        }
        return totalContribution / 2.0;
    }

    private static double computePagIbig(double basicSalary) {
        // 1% up to 1500 salary, otherwise 2%, capped at 100 contribution.
        double rate = basicSalary <= 1500 ? 0.01 : 0.02;
        return Math.min(basicSalary * rate, 100.0);
    }

    private static double computeWithholdingTax(double taxableMonthlyIncome) {
        // Progressive monthly tax brackets from MotorPH reference requirements.
        if (taxableMonthlyIncome <= 20832) {
            return 0;
        } else if (taxableMonthlyIncome <= 33332) {
            return (taxableMonthlyIncome - 20833) * 0.20;
        } else if (taxableMonthlyIncome <= 66666) {
            return 2500 + (taxableMonthlyIncome - 33333) * 0.25;
        } else if (taxableMonthlyIncome <= 166666) {
            return 10833 + (taxableMonthlyIncome - 66667) * 0.30;
        } else if (taxableMonthlyIncome <= 666666) {
            return 40833.33 + (taxableMonthlyIncome - 166667) * 0.32;
        } else {
            return 200833.33 + (taxableMonthlyIncome - 666667) * 0.35;
        }
    }
}
