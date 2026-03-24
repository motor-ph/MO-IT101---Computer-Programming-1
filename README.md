# MotorPH Payroll System

A command-line Java payroll application for MO-IT101.  
The project follows the single-file requirement (`PayrollSystem.java`) and uses CSV files for employee and attendance data (`data/employees.csv` and `data/attendance.csv`).

---

## Features

- **Secure login** (`admin` / `motorph123`) with attempt limit.
- **Manage Employees** with full CRUD:
  - View all employees
  - View full employee details
  - Create new employee (auto-generated employee number)
  - Update employee
  - Delete employee
  - Employee form guide
- **Manage Attendance** with full CRUD:
  - View employee attendance
  - Add record
  - Update record
  - Delete record
  - Attendance form guide
- **Manage Payroll**:
  - Generate monthly payslip for one employee
  - Generate monthly payslip for all employees with attendance in selected month
  - Generate weekly payslip for one employee
  - Monthly payslip includes weekly attendance breakdown
- **Employee Time-In/Time-Out**:
  - One time-in and one time-out per employee per day
  - Prevents duplicate attendance actions
  - Persists attendance updates to `data/attendance.csv`
- **About MotorPH** informational screen.

---

## Main Menu

After successful login:

```text
1) Manage Employees
2) Manage Attendance
3) Manage Payroll
4) Employee Time-In/Time-Out
5) About MotorPH
6) Exit
```

### Manage Payroll Submenu

```text
1) Generate Monthly Payslip (Single Employee)
2) Generate Monthly Payslip (All Employees)
3) Generate Weekly Payslip (Single Employee)
4) Back
```

### Employee Time-In/Time-Out Submenu

```text
1) Time-In
2) Time-Out
3) Back
```

---

## Payroll Computation Rules

### Attendance timeframe rules

- **Grace period:** log-in at `8:10 AM` or earlier is treated as `8:00 AM`.
- **Late login:** after `8:10 AM`, actual log-in time is used.
- **End-of-day cap:** only work up to `5:00 PM` is counted; overtime is excluded from payroll computation.
- **Lunch deduction:** if worked minutes exceed 1 hour, 60 minutes are deducted.

### Monthly payslip model

- Payslip is generated per selected month.
- Output includes **weekly attendance breakdown** (Week, Days, Hours, Weekly Gross).
- Monthly gross is computed as the sum of all weekly gross values in that month.
- Deductions and tax are computed on **monthly gross**, then net monthly salary is shown.

### Deductions

- **SSS:** bracket-based contribution from monthly gross.
- **PhilHealth:** follows floor/cap logic (minimum and maximum employee share).
- **Pag-IBIG:** 1% or 2% rate with cap.
- **Withholding Tax:** computed from taxable income using progressive brackets.

Taxable income:

```text
Taxable Income = Monthly Gross - (SSS + PhilHealth + Pag-IBIG)
```

---

## Project Files

```text
MO-IT101---Computer-Programming-1/
├── PayrollSystem.java
├── README.md
└── data/
    ├── employees.csv
    └── attendance.csv
```

Notes:
- Employee data is loaded dynamically from `data/employees.csv`.
- Attendance data is loaded dynamically from `data/attendance.csv`.
- Run the program from the project root so relative CSV paths resolve correctly.

---

## How to Run

### 1) Prerequisites

- Java JDK 8+ installed
- Terminal can run `javac` and `java`

### 2) Compile

```bash
javac PayrollSystem.java
```

### 3) Run

```bash
java PayrollSystem
```

### 4) Login Credentials

- Username: `admin`
- Password: `motorph123`

---

## Sample Console Outputs

### Successful login + menu

```text
========================================
        MotorPH Payroll System
========================================
Username: admin
Password: motorph123
Login successful.

========================================
       MotorPH Payroll System
========================================
1) Manage Employees
2) Manage Attendance
3) Manage Payroll
4) Employee Time-In/Time-Out
5) About MotorPH
6) Exit
```

### Monthly payslip (single employee)

```text
==========================================================
                 MONTHLY PAYROLL REPORT
==========================================================
Employee #: 10001        Name: Manuel III Garcia
Birthday  : 10/11/1983   Position: Chief Executive Officer
Pay Period: June           Hourly Rate: PHP 535.71
----------------------------------------------------------
             WEEKLY ATTENDANCE BREAKDOWN
----------------------------------------------------------
Week   Days   Hours      Weekly Gross
1      5      30.98      PHP 16,598.08
2      5      33.85      PHP 18,133.78
3      5      34.23      PHP 18,339.14
4      5      32.58      PHP 17,455.22
----------------------------------------------------------
Total  20     131.65     PHP 70,526.22
==========================================================
                   MONTHLY DEDUCTIONS
----------------------------------------------------------
SSS               : PHP 1,125.00
PhilHealth        : PHP 900.00
Pag-IBIG          : PHP 100.00
Withholding Tax   : PHP 11,353.27
Total Deductions  : PHP 13,478.27
----------------------------------------------------------
GROSS MONTHLY PAY : PHP 70,526.22
NET MONTHLY SALARY: PHP 57,047.96
==========================================================
```

### Weekly payslip (single employee)

```text
==========================================================
                  WEEKLY PAYROLL REPORT
==========================================================
Employee #: 10001        Name: Manuel III Garcia
Birthday  : 10/11/1983   Position: Chief Executive Officer
Pay Period: June Week 1   Hourly Rate: PHP 535.71
----------------------------------------------------------
Days Worked  : 5
Hours Worked : 30.98
Gross Weekly : PHP 16,598.08
----------------------------------------------------------
              WEEKLY DEDUCTIONS (prorated)
----------------------------------------------------------
SSS               : PHP 281.25
PhilHealth        : PHP 225.00
Pag-IBIG          : PHP 25.00
Withholding Tax   : PHP 2,838.32
Total Deductions  : PHP 3,369.57
----------------------------------------------------------
NET WEEKLY SALARY : PHP 13,228.51
==========================================================
```

### Invalid employee number in payroll menu

```text
Enter Employee Number: 99999
Employee not found.
```

### No attendance records for selected month

```text
Select month (1-12): 1
No attendance records found for that employee/month.
```

### Duplicate time-in prevention

```text
Enter Employee Number: 10001
You have already timed in today.
```

---

## Constraints and Notes

- Single-file implementation (`PayrollSystem.java`) by project requirement.
- Uses arrays, maps, and lists for data handling.
- Employee and attendance CRUD updates are in-memory during runtime; attendance clock actions also write back to `data/attendance.csv`.

---

## Acknowledgments

This project was developed with the assistance of AI tools, including ChatGPT (OpenAI, GPT-5.3).  
AI was used for code suggestions, debugging, and documentation.

All AI-generated outputs were reviewed, modified, and validated by the author.

---

## Author

- April Joyce Abejo

## Mentor

- Aldrin John Tamayo
- Mapúa Malayan Digital College, a new college of Malayan Colleges Laguna, a Mapúa school