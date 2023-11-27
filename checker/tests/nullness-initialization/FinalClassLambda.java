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

final class I2 extends Z {
    @Nullable String s;

    I2() {
        use(() -> init());
        use(
                new Runnable() {
                    @Override
                    public void run() {
                        init();
                    }
                });
    }

    void init() {}
}

class Z {
    void use(Runnable r) {}
}
