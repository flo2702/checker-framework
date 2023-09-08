import org.checkerframework.checker.nullness.qual.*;

public class SuppressWarningsTest {

    Object f;

    @SuppressWarnings("all")
    void test() {
        String a = null;
        a.toString();
    }

    @SuppressWarnings("initialization")
    void test2() {
        String a = null;
        // :: error: dereference.of.nullable
        a.toString();
    }

    @SuppressWarnings("nullness")
    SuppressWarningsTest() {}

    @SuppressWarnings("nullness-no-init")
    // :: error: initialization.fields.uninitialized
    SuppressWarningsTest(int dummy) {}
}
