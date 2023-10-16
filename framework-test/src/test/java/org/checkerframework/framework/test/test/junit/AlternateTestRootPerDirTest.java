package org.checkerframework.framework.test.test.junit;

import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.checkerframework.framework.test.TestRootDirectory;
import org.checkerframework.framework.test.TypecheckResult;
import org.checkerframework.framework.test.diagnostics.TestDiagnosticUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.List;

/** Tests the explicit tests root configuration. */
@TestRootDirectory("tests-alt")
public class AlternateTestRootPerDirTest extends CheckerFrameworkPerDirectoryTest {

    /**
     * @param testFiles the files containing test code, which will be type-checked
     */
    public AlternateTestRootPerDirTest(List<File> testFiles) {
        super(testFiles, ValueChecker.class, "");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"alt-dir-a", "alt-dir-b"};
    }

    @Override
    public void checkResult(TypecheckResult typecheckResult) {
        super.checkResult(typecheckResult);
        MatcherAssert.assertThat(
                "test check result has exactly one expected diagnostic",
                typecheckResult.getExpectedDiagnostics().size(),
                CoreMatchers.describedAs(
                        "singleton collection %0",
                        CoreMatchers.is(1), typecheckResult.getExpectedDiagnostics()));
        MatcherAssert.assertThat(
                "test check result has the expected diagnostic of one of the test files",
                typecheckResult.getExpectedDiagnostics(),
                CoreMatchers.either(
                                CoreMatchers.hasItem(
                                        TestDiagnosticUtils.fromJavaFileComment(
                                                "Issue6125A.java", 5, "error: (assignment)")))
                        .or(
                                CoreMatchers.hasItem(
                                        TestDiagnosticUtils.fromJavaFileComment(
                                                "Issue6125B.java", 5, "error: (assignment)"))));
        MatcherAssert.assertThat(
                "test check result has exactly zero unexpected diagnostics",
                typecheckResult.getUnexpectedDiagnostics().size(),
                CoreMatchers.describedAs(
                        "zero length collection %0",
                        CoreMatchers.is(0), typecheckResult.getUnexpectedDiagnostics()));
        MatcherAssert.assertThat(
                "test check result has exactly zero missing diagnostics",
                typecheckResult.getMissingDiagnostics().size(),
                CoreMatchers.describedAs(
                        "zero length collection %0",
                        CoreMatchers.is(0), typecheckResult.getMissingDiagnostics()));
    }
}
