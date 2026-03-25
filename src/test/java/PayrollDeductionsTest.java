import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QA-14: Deduction formula validation (JUnit). Display names use QA_TEST_14_DeductionFormulasValidation.
 * Reference scenario: monthly salary PHP 25,000 with expected SSS, PhilHealth, Pag-IBIG,
 * taxable income, and bracket tax (20% in excess of 20,833).
 */
@DisplayName("QA_TEST_14_DeductionFormulasValidation")
class PayrollDeductionsTest {

    private static final double MONTHLY_SALARY = 25_000.0;
    private static final double EXPECTED_SSS = 1_125.0;
    private static final double EXPECTED_PHILHEALTH = 375.0;
    private static final double EXPECTED_PAGIBIG = 100.0;
    /** SSS + PhilHealth + Pag-IBIG (statutory; before withholding tax). */
    private static final double EXPECTED_TOTAL_STATUTORY_DEDUCTIONS = 1_600.0;
    private static final double EXPECTED_TAXABLE_INCOME = 23_400.0;
    private static final double EXPECTED_WITHHOLDING_TAX = 513.4;
    private static final double TAX_BRACKET_FLOOR = 20_833.0;
    private static final double SECOND_BRACKET_RATE = 0.20;

    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation — SSS at PHP 25k monthly gross")
    void monthlySalary25000_sssMatchesExpected() {
        assertEquals(EXPECTED_SSS, PayrollSystem.computeSss(MONTHLY_SALARY), 0.001);
    }

    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation — PhilHealth at PHP 25k monthly gross")
    void monthlySalary25000_philhealthMatchesExpected() {
        assertEquals(EXPECTED_PHILHEALTH, PayrollSystem.computePhilHealth(MONTHLY_SALARY), 0.001);
    }

    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation — Pag-IBIG at PHP 25k monthly gross")
    void monthlySalary25000_pagibigMatchesExpected() {
        assertEquals(EXPECTED_PAGIBIG, PayrollSystem.computePagIbig(MONTHLY_SALARY), 0.001);
    }

    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation — total statutory deductions at PHP 25k")
    void monthlySalary25000_totalStatutoryDeductionsMatchExpected() {
        double total = PayrollSystem.computeSss(MONTHLY_SALARY)
            + PayrollSystem.computePhilHealth(MONTHLY_SALARY)
            + PayrollSystem.computePagIbig(MONTHLY_SALARY);
        assertEquals(EXPECTED_TOTAL_STATUTORY_DEDUCTIONS, total, 0.001);
    }

    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation — taxable income = gross minus statutory")
    void monthlySalary25000_taxableIncomeIsSalaryMinusStatutoryDeductions() {
        double statutory = PayrollSystem.computeSss(MONTHLY_SALARY)
            + PayrollSystem.computePhilHealth(MONTHLY_SALARY)
            + PayrollSystem.computePagIbig(MONTHLY_SALARY);
        double taxable = MONTHLY_SALARY - statutory;
        assertEquals(EXPECTED_TAXABLE_INCOME, taxable, 0.001);
    }

    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation — withholding 20% over 20833 on taxable 23400")
    void taxableIncome23400_withholdingTaxIs20PercentInExcessOf20833() {
        double taxable = EXPECTED_TAXABLE_INCOME;
        double expected = (taxable - TAX_BRACKET_FLOOR) * SECOND_BRACKET_RATE;
        assertEquals(EXPECTED_WITHHOLDING_TAX, expected, 0.001);
        assertEquals(EXPECTED_WITHHOLDING_TAX, PayrollSystem.computeWithholdingTax(taxable), 0.001);
    }

    @Test
    @DisplayName("QA_TEST_14_DeductionFormulasValidation — full PHP 25k reference table")
    void monthlySalary25000_fullScenarioMatchesReferenceTable() {
        double sss = PayrollSystem.computeSss(MONTHLY_SALARY);
        double phil = PayrollSystem.computePhilHealth(MONTHLY_SALARY);
        double pag = PayrollSystem.computePagIbig(MONTHLY_SALARY);
        double totalStatutory = sss + phil + pag;
        double taxable = MONTHLY_SALARY - totalStatutory;
        double withholding = PayrollSystem.computeWithholdingTax(taxable);

        assertEquals(1_125.0, sss, 0.001);
        assertEquals(375.0, phil, 0.001);
        assertEquals(100.0, pag, 0.001);
        assertEquals(1_600.0, totalStatutory, 0.001);
        assertEquals(23_400.0, taxable, 0.001);
        assertEquals(513.4, withholding, 0.001);
        assertEquals((23_400.0 - 20_833.0) * 0.20, withholding, 0.001);
    }

    @ParameterizedTest(name = "QA_TEST_14_DeductionFormulasValidation — SSS gross={0} expect={1}")
    @CsvSource({
        "3000, 135",
        "6500, 292.5",
        "24750, 1125",
        "30000, 1125"
    })
    void sssBracketSamples(double monthlyGross, double expectedSss) {
        assertEquals(expectedSss, PayrollSystem.computeSss(monthlyGross), 0.001);
    }

    @ParameterizedTest(name = "QA_TEST_14_DeductionFormulasValidation — PhilHealth gross={0} employee share={1}")
    @CsvSource({
        "8000, 150",
        "20000, 300",
        "60000, 900"
    })
    void philhealthSamples(double monthlyGross, double expectedEmployeeShare) {
        assertEquals(expectedEmployeeShare, PayrollSystem.computePhilHealth(monthlyGross), 0.001);
    }

    @ParameterizedTest(name = "QA_TEST_14_DeductionFormulasValidation — withholding taxable={0} tax={1}")
    @CsvSource({
        "20832, 0",
        "20833, 0",
        "25000, 833.4",
        "33333, 2500"
    })
    void withholdingTaxBracketSamples(double taxableIncome, double expectedTax) {
        assertEquals(expectedTax, PayrollSystem.computeWithholdingTax(taxableIncome), 0.001);
    }
}
