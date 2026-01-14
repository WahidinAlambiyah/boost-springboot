package com.example.graphqlusers.web;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public class CorsHandler implements HttpHandler {
    private final HttpHandler next;
    private static final HttpString ACCESS_CONTROL_ALLOW_ORIGIN = new HttpString("Access-Control-Allow-Origin");
    private static final HttpString ACCESS_CONTROL_ALLOW_HEADERS = new HttpString("Access-Control-Allow-Headers");
    private static final HttpString ACCESS_CONTROL_ALLOW_METHODS = new HttpString("Access-Control-Allow-Methods");

    public CorsHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_HEADERS, "authorization,content-type");
        exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,OPTIONS");
        if (exchange.getRequestMethod().equalToString("OPTIONS")) {
            exchange.setStatusCode(204);
            return;
        }
        next.handleRequest(exchange);
    }
}
