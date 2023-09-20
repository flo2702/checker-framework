public class SuppressWarningsTest {

    Object f;

    @SuppressWarnings("nullnessnoinit")
    void test() {
        String a = null;
        a.toString();
    }

    @SuppressWarnings("initialization")
    void test2() {
        String a = null;
        // :: error: (dereference.of.nullable)
        a.toString();
    }

    @SuppressWarnings("nullness")
    SuppressWarningsTest() {}

    @SuppressWarnings("nullnessnoinit")
    // :: error: (initialization.fields.uninitialized)
    SuppressWarningsTest(int dummy) {}
}
