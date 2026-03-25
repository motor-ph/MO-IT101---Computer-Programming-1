<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/JUnit_5-25A162?logo=junit5&logoColor=white" alt="JUnit 5" />
  <img src="https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white" alt="Git" />
  <img src="https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white" alt="GitHub" />
  <img src="https://img.shields.io/badge/Tests-34_passing-brightgreen?logo=checkmarx&logoColor=white" alt="34 tests passing" />
  <img src="https://img.shields.io/badge/QA_Cases-15/15-blue?logo=testcafe&logoColor=white" alt="QA 15/15" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?logo=opensourceinitiative&logoColor=white" alt="MIT License" />
</p>

# MotorPH Payroll System

> A command-line Java payroll application for **MO-IT101 — Computer Programming 1**.  
> Computes employee salaries, statutory deductions (SSS, PhilHealth, Pag-IBIG), and withholding tax from CSV data.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | **Java 17** |
| Build | **Apache Maven** |
| Testing | **JUnit 5** (Jupiter + Parameterized) |
| Version Control | **Git** / **GitHub** |
| Data | **CSV** (employees & attendance) |

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
- **Unit tests**: all **15 QA cases** (QA-01–QA-15) automated with JUnit 5; run `mvn test` (see [Unit Tests](#unit-tests)).

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

## Project Structure

```text
MO-IT101---Computer-Programming-1/
├── pom.xml
├── README.md
├── LICENSE
├── docs/
│   └── qa-test-cases.md
├── src/
│   ├── main/java/
│   │   └── PayrollSystem.java
│   └── test/java/
│       ├── PayrollSystemQaTest.java
│       ├── PayrollDeductionsTest.java
│       └── QaTest15ColumnIndexConstantsTest.java
└── data/
    ├── employees.csv
    └── attendance.csv
```

> Employee and attendance data is loaded dynamically from CSV at startup.
> Run the program from the project root so relative paths resolve correctly.

---

## Getting Started

### Prerequisites

- **JDK 17+** installed (`java --version`)
- **Apache Maven** installed (`mvn --version`)

### Compile & Run

```bash
# Option A: Maven (recommended)
mvn compile
mvn exec:java -Dexec.mainClass=PayrollSystem

# Option B: javac / java
mkdir -p target/classes
javac -d target/classes src/main/java/PayrollSystem.java
java -cp target/classes PayrollSystem
```

### Login Credentials

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `motorph123` |

---

## Unit Tests

All **15 QA test cases** (QA-01 through QA-15) are automated with **JUnit 5**. Running `mvn test` executes every case and reports Pass or Fail.

Full step-by-step descriptions are in **[docs/qa-test-cases.md](docs/qa-test-cases.md)**.

### Test classes

| Class | QA coverage | What it tests |
|-------|-------------|---------------|
| **`PayrollSystemQaTest`** | QA-01 – QA-15 (one `@Test` per QA number) | Login, CSV loading, CRUD, payslip, time-in/out, deductions, source scan |
| **`PayrollDeductionsTest`** | QA-14 (extended) | PHP 25,000 reference + parameterized SSS/PhilHealth/tax bracket samples |
| **`QaTest15ColumnIndexConstantsTest`** | QA-15 (extended) | Source scan for magic `emp[digits]` / `row[digits]` |

### QA-14 reference (PHP 25,000 monthly gross)

| Item | Expected |
|------|----------|
| SSS Deduction | 1,125 |
| PhilHealth Deduction | 375 |
| Pag-IBIG Deduction | 100 |
| Total Statutory Deductions | 1,600 |
| Taxable Income (25,000 − 1,600) | 23,400 |
| Withholding Tax (23,400 − 20,833) × 20% | 513.40 |

### Run all tests

```bash
mvn test
```

A successful run prints no failures; Maven reports tests executed under the `surefire` plugin.

---

## Sample Console Outputs

<details>
<summary><strong>Successful login + menu</strong></summary>

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

</details>

<details>
<summary><strong>Monthly payslip (single employee)</strong></summary>

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

</details>

<details>
<summary><strong>Weekly payslip (single employee)</strong></summary>

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

</details>

<details>
<summary><strong>Edge cases</strong></summary>

```text
# Non-existent employee
Enter Employee Number: 99999
Employee not found.

# No attendance for selected month
Select month (1-12): 1
No attendance records found for that employee/month.

# Duplicate time-in
Enter Employee Number: 10001
You have already timed in today.
```

</details>

---

## Constraints and Notes

- Application logic is consolidated in one class, [`src/main/java/PayrollSystem.java`](src/main/java/PayrollSystem.java). `pom.xml` and `src/test/java/` add Maven/JUnit testing; you can still compile and run with `javac` / `java` as shown in [Getting Started](#getting-started).
- Uses arrays, maps, and lists for data handling.
- Employee and attendance CRUD updates are in-memory during runtime; attendance clock actions also write back to `data/attendance.csv`.

---

## Acknowledgments

This project was developed with the assistance of AI tools, including ChatGPT (OpenAI, GPT-5.3).  
AI was used for code suggestions, debugging, and documentation.

All AI-generated outputs were reviewed, modified, and validated by the author.

---

## Author

**April Joyce Abejo**

## Mentor

**Aldrin John Tamayo**  
Mapua Malayan Digital College, a new college of Malayan Colleges Laguna, a Mapua school
