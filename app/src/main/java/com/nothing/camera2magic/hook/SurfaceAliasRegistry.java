package com.nothing.camera2magic.hook;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/** Maps camera-owned output aliases back to the objects known by the caller. */
final class SurfaceAliasRegistry<T> {
    private final Map<T, WeakReference<T>> originals = new WeakHashMap<>();

    synchronized void register(T original, T replacement) {
        originals.put(replacement, new WeakReference<>(original));
    }

    synchronized T resolve(T output) {
        WeakReference<T> reference = originals.get(output);
        T original = reference == null ? null : reference.get();
        return original == null ? output : original;
    }
}
