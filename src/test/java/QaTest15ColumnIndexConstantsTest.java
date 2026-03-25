import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * QA-15: Light guard that {@code PayrollSystem.java} does not use literal {@code emp[0]} / {@code row[0]}
 * style indexing for employee or attendance rows (loop indices {@code emp[i]} are allowed).
 */
@DisplayName("QA_TEST_15_ColumnIndexConstantsUsage")
class QaTest15ColumnIndexConstantsTest {

    private static final Path PAYROLL_SOURCE = Path.of("src/main/java/PayrollSystem.java");
    private static final Pattern EMP_NUMERIC_INDEX = Pattern.compile("\\bemp\\[\\d+\\]");
    private static final Pattern ROW_NUMERIC_INDEX = Pattern.compile("\\brow\\[\\d+\\]");

    @Test
    @DisplayName("QA_TEST_15_ColumnIndexConstantsUsage — no magic emp[]/row[] numeric column indices")
    void payrollSourceUsesNamedConstantsForEmpAndAttendanceRows() throws IOException {
        Assumptions.assumeTrue(
            Files.isRegularFile(PAYROLL_SOURCE),
            "src/main/java/PayrollSystem.java must exist (run mvn test from project root)"
        );
        String code = Files.readString(PAYROLL_SOURCE);
        assertFalse(
            EMP_NUMERIC_INDEX.matcher(code).find(),
            "Use EMP_* constants instead of emp[digits] for employee column access"
        );
        assertFalse(
            ROW_NUMERIC_INDEX.matcher(code).find(),
            "Use ATT_* constants instead of row[digits] for attendance row access"
        );
    }
}
