package com.mubin.quant.api;

import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookType;
import com.mubin.quant.core.TradingCoreService;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.domain.Side;
import com.mubin.quant.position.PositionSnapshot;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingHttpServer {

    private final HttpServer server;

    public TradingHttpServer(int port, TradingCoreService tradingCoreService, BookManager bookManager) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health", exchange -> writeJson(exchange, 200, "{\"status\":\"UP\"}"));
        server.createContext("/orders", new OrderQueryHandler(tradingCoreService));
        server.createContext("/positions", new PositionQueryHandler(tradingCoreService));
        server.createContext("/books", new BookQueryHandler(bookManager));
        server.setExecutor(null);
    }

    public void start() {
        server.start();
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    private static final class OrderQueryHandler implements HttpHandler {
        private final TradingCoreService tradingCoreService;

        private OrderQueryHandler(TradingCoreService tradingCoreService) {
            this.tradingCoreService = tradingCoreService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String orderId = query.get("id");
            if (orderId == null || orderId.isBlank()) {
                writeJson(exchange, 400, "{\"error\":\"missing_id\"}");
                return;
            }

            Order order = tradingCoreService.getOrder(orderId);
            if (order == null) {
                writeJson(exchange, 404, "{\"error\":\"order_not_found\"}");
                return;
            }

            String body = "{"
                    + "\"orderId\":\"" + order.getOrderId() + "\"," 
                    + "\"symbol\":\"" + order.getSymbol() + "\"," 
                    + "\"status\":\"" + order.getStatus() + "\"," 
                    + "\"filledQty\":" + order.getFilledQuantity()
                    + "}";
            writeJson(exchange, 200, body);
        }
    }

    private static final class PositionQueryHandler implements HttpHandler {
        private final TradingCoreService tradingCoreService;

        private PositionQueryHandler(TradingCoreService tradingCoreService) {
            this.tradingCoreService = tradingCoreService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String symbol = query.get("symbol");
            if (symbol == null || symbol.isBlank()) {
                writeJson(exchange, 400, "{\"error\":\"missing_symbol\"}");
                return;
            }

            PositionSnapshot snapshot = tradingCoreService.positionService().snapshot(symbol);
            String body = "{"
                    + "\"symbol\":\"" + snapshot.symbol() + "\"," 
                    + "\"netQty\":" + snapshot.netQuantity() + ","
                    + "\"avgPrice\":" + snapshot.avgPrice()
                    + "}";
            writeJson(exchange, 200, body);
        }
    }

    private static final class BookQueryHandler implements HttpHandler {
        private final BookManager bookManager;

        private BookQueryHandler(BookManager bookManager) {
            this.bookManager = bookManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String book = query.get("type");
            String symbol = query.get("symbol");
            String side = query.get("side");
            String depthRaw = query.getOrDefault("depth", "5");

            if (book == null || symbol == null || side == null) {
                writeJson(exchange, 400, "{\"error\":\"missing_required_params\"}");
                return;
            }

            int depth = Integer.parseInt(depthRaw);
            List<QuoteEvent> levels = bookManager.topLevels(
                    BookType.valueOf(book),
                    symbol,
                    Side.valueOf(side),
                    depth
            );

            StringBuilder body = new StringBuilder();
            body.append("{\"levels\":[");
            for (int i = 0; i < levels.size(); i++) {
                QuoteEvent level = levels.get(i);
                if (i > 0) {
                    body.append(',');
                }
                body.append("{")
                        .append("\"quoteId\":\"").append(level.quoteId()).append("\",")
                        .append("\"source\":\"").append(level.source()).append("\",")
                        .append("\"price\":").append(level.price()).append(",")
                        .append("\"qty\":").append(level.quantity())
                        .append("}");
            }
            body.append("]}");
            writeJson(exchange, 200, body.toString());
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String raw = uri.getRawQuery();
        if (raw == null || raw.isBlank()) {
            return result;
        }

        String[] pairs = raw.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";
            result.put(key, value);
        }
        return result;
    }

    private static void writeJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
