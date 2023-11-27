import org.checkerframework.checker.initialization.qual.*;
import org.checkerframework.checker.nullness.qual.*;

final class FinalClassLambda1 {
    @Nullable String s;

    FinalClassLambda1() {
        use(this::init);
    }

    void init() {}

    static void use(Runnable r) {}
}

final class FinalClassLambda2 extends FinalClassLambda2Base {
    @Nullable String s;

    FinalClassLambda2() {
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

class FinalClassLambda2Base {
    void use(Runnable r) {}
}
