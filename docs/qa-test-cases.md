# MotorPH Payroll System — QA test cases

Traceability uses **Test Case ID** (`QA-XX`) and section headings **`QA_TEST_XX_Name`**.

Fill **Actual result** and **Test result** (`Pass` / `Fail`) when you execute each case.

| Automation | IDs |
|------------|-----|
| Manual (console) | QA-01 – QA-13 |
| JUnit (`mvn test`) | QA-14, QA-15 (see [Unit Tests](../README.md#unit-tests) in README) |

---

## QA_TEST_01_LoginWithValidCredentials

- **Scenario:** Login  
- **Test Case ID:** QA-01  
- **Test case:** Login with valid credentials  
- **Input:** Username: `admin`; Password: `motorph123`  
- **Steps:**
  1. Run program  
  2. Enter valid username  
  3. Enter valid password  
- **Expected result:** User is authenticated and main menu is shown  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_02_LoginLocksAfterThreeInvalidAttempts

- **Scenario:** Login  
- **Test Case ID:** QA-02  
- **Test case:** Login with invalid credentials three times  
- **Input:** Username/password: invalid values for 3 attempts  
- **Steps:**
  1. Run program  
  2. Enter invalid credentials 3 times  
- **Expected result:** System denies access after third failed attempt  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_03_EmployeeCsvLoadsWithCompleteFields

- **Scenario:** Data Loading  
- **Test Case ID:** QA-03  
- **Test case:** Employee data load from CSV  
- **Input:** File: `data/employees.csv`  
- **Steps:**
  1. Run program  
  2. Navigate to employee list  
  3. Review loaded rows  
- **Expected result:** Employee records load with complete expected fields  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_04_AttendanceCsvLoadsAndDisplays

- **Scenario:** Data Loading  
- **Test Case ID:** QA-04  
- **Test case:** Attendance data load from CSV  
- **Input:** File: `data/attendance.csv`  
- **Steps:**
  1. Run program  
  2. Open attendance view  
  3. Select an existing employee  
- **Expected result:** Attendance records load and display correctly  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_05_EmployeeCrudAndAutoId

- **Scenario:** Employee CRUD  
- **Test Case ID:** QA-05  
- **Test case:** Employee CRUD operations  
- **Input:** New employee details; existing employee number  
- **Steps:**
  1. Create employee  
  2. View details  
  3. Update fields  
  4. Delete record  
- **Expected result:** Create, view, update, and delete actions work correctly; ID auto-generates on create  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_06_AttendanceCrud

- **Scenario:** Attendance CRUD  
- **Test Case ID:** QA-06  
- **Test case:** Attendance CRUD operations  
- **Input:** Employee no.; date; login; logout  
- **Steps:**
  1. Add attendance entry  
  2. View entry  
  3. Update entry  
  4. Delete entry  
- **Expected result:** Add, view, update, and delete attendance operations work correctly  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_07_MonthlyPayslipSingleEmployee

- **Scenario:** Payroll  
- **Test Case ID:** QA-07  
- **Test case:** Monthly payslip (single employee)  
- **Input:** Employee no.: `10001`; Month: `6`  
- **Steps:**
  1. Open payroll menu  
  2. Select single employee payslip  
  3. Enter employee no. and month  
- **Expected result:** Payslip shows weekly attendance breakdown and monthly deductions  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_08_MonthlyPayslipAllEmployees

- **Scenario:** Payroll  
- **Test Case ID:** QA-08  
- **Test case:** Monthly payslip (all employees)  
- **Input:** Month: `6`  
- **Steps:**
  1. Open payroll menu  
  2. Select all employees payslip  
  3. Enter month  
- **Expected result:** System generates payslips for all employees with matching attendance  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_09_PayrollNonExistentEmployee

- **Scenario:** Payroll  
- **Test Case ID:** QA-09  
- **Test case:** Non-existent employee in payroll menu  
- **Input:** Employee no.: `99999`; Month: `6`  
- **Steps:**
  1. Open payroll menu  
  2. Select single employee payslip  
  3. Enter non-existent employee no.  
- **Expected result:** System displays `Employee not found.`  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_10_PayrollNoAttendanceForMonth

- **Scenario:** Payroll  
- **Test Case ID:** QA-10  
- **Test case:** No attendance for selected month  
- **Input:** Employee no.: existing employee; Month: month with no records  
- **Steps:**
  1. Open payroll menu  
  2. Select single employee payslip  
  3. Enter month with no attendance data  
- **Expected result:** System displays no-record message for selected month  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_11_TimeInDuplicateSameDayBlocked

- **Scenario:** Time-In/Time-Out  
- **Test Case ID:** QA-11  
- **Test case:** Employee time-in with duplicate check  
- **Input:** Employee no.: `10001`; date: current date  
- **Steps:**
  1. Choose Time-In  
  2. Enter employee no.  
  3. Repeat Time-In on same date  
- **Expected result:** First time-in is saved; second same-day time-in is blocked  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_12_TimeOutDuplicateSameDayBlocked

- **Scenario:** Time-In/Time-Out  
- **Test Case ID:** QA-12  
- **Test case:** Employee time-out with duplicate check  
- **Input:** Employee no.: `10001`; date: current date  
- **Steps:**
  1. Choose Time-Out  
  2. Enter employee no.  
  3. Repeat Time-Out on same date  
- **Expected result:** First time-out updates CSV; second same-day time-out is blocked  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_13_TimeInOutOutsideAllowedWindowRejected

- **Scenario:** Time-In/Time-Out  
- **Test Case ID:** QA-13  
- **Test case:** Time-in/time-out outside 05:00–21:00 window  
- **Input:** Employee no.: `10001`; time outside allowed range  
- **Steps:**
  1. Attempt time-in/out outside allowed window  
  2. Submit entry  
- **Expected result:** Entry is rejected with validation message  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_14_DeductionFormulasValidation

- **Scenario:** Deductions  
- **Test Case ID:** QA-14  
- **Test case:** Deduction formulas validation  
- **Input:** Monthly gross samples for SSS / PhilHealth / Pag-IBIG / tax brackets  
- **Steps:**
  1. Run `mvn test` (JUnit exercises `PayrollSystem` deduction helpers)  
  2. Optionally compare computed values against reference rules / payslip output  
- **Expected result:** Deductions follow implemented logic for all four functions  
- **Actual result:** _  
- **Test result:** _  

---

## QA_TEST_15_ColumnIndexConstantsUsage

- **Scenario:** Code Quality  
- **Test Case ID:** QA-15  
- **Test case:** Column-index constants usage  
- **Input:** `src/main/java/PayrollSystem.java` index references  
- **Steps:**
  1. Run `mvn test` (JUnit scans source for disallowed `emp[digit]` / `row[digit]` patterns)  
  2. Review employee and attendance index access for consistent use of `EMP_*` / `ATT_*` constants  
- **Expected result:** Constants are used in place of magic numbers for key indexes  
- **Actual result:** _  
- **Test result:** _  
