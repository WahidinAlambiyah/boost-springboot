package com.example.boost.context;

public final class RequestContext {
    private static final ThreadLocal<RequestContextData> CONTEXT = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void set(RequestContextData data) {
        CONTEXT.set(data);
    }

    public static RequestContextData get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
