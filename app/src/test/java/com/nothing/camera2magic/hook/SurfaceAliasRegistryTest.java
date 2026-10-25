package com.nothing.camera2magic.hook;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.Assert.*;

public class SurfaceAliasRegistryTest {
    @Test public void unknownAndOriginalOutputsPassThrough() {
        SurfaceAliasRegistry<Object> aliases = new SurfaceAliasRegistry<>();
        Object original = new Object();
        Object unrelated = new Object();
        aliases.register(original, new Object());
        assertSame(original, aliases.resolve(original));
        assertSame(unrelated, aliases.resolve(unrelated));
        assertNull(aliases.resolve(null));
    }

    @Test public void replacementReturnsExactOriginalIdentity() {
        SurfaceAliasRegistry<Object> aliases = new SurfaceAliasRegistry<>();
        Object original = new Object();
        Object replacement = new Object();
        aliases.register(original, replacement);
        assertSame(original, aliases.resolve(replacement));
        assertSame(original, aliases.resolve(replacement));
    }

    @Test public void lateCallbackFromPreviousSessionStillResolves() {
        SurfaceAliasRegistry<Object> aliases = new SurfaceAliasRegistry<>();
        Object original = new Object();
        Object oldReplacement = new Object();
        Object newReplacement = new Object();
        aliases.register(original, oldReplacement);
        aliases.register(original, newReplacement);
        assertSame(original, aliases.resolve(oldReplacement));
        assertSame(original, aliases.resolve(newReplacement));
    }

    @Test public void concurrentSessionsKeepTheirOwnOutputs() throws Exception {
        SurfaceAliasRegistry<Object> aliases = new SurfaceAliasRegistry<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<?>> tasks = new ArrayList<>();
        try {
            for (int i = 0; i < 100; i++) tasks.add(pool.submit(() -> {
                Object original = new Object();
                Object replacement = new Object();
                aliases.register(original, replacement);
                assertSame(original, aliases.resolve(replacement));
            }));
            for (Future<?> task : tasks) task.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }
}
