import org.checkerframework.checker.initialization.qual.*;
import org.checkerframework.checker.nullness.qual.*;

final class FinalClassLambda {
    @Nullable String s;

    FinalClassLambda() {
        use(this::init);
    }

    void init() {}

    static void use(Runnable r) {}
}
