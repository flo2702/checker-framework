import org.checkerframework.checker.initialization.qual.UnknownInitialization;

public class UnboxUninitalizedFieldTest {
    @UnknownInitialization Integer n;

    UnboxUninitalizedFieldTest() {
        // :: error: unboxing.of.nullable
        int y = n;
    }
}
