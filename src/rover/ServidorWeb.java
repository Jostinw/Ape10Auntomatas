package rover;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java_cup.runtime.Symbol;
import java.util.List;
import java.util.ArrayList;

public class ServidorWeb {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Ruta raíz '/': Servir index.html con charset UTF-8
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    try {
                        byte[] htmlBytes = Files.readAllBytes(Paths.get("src/rover/index.html"));
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, htmlBytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(htmlBytes);
                        }
                    } catch (IOException e) {
                        String errMsg = "Error: No se pudo cargar el archivo index.html.";
                        byte[] errBytes = errMsg.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                        exchange.sendResponseHeaders(404, errBytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errBytes);
                        }
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            }
        });

        // Ruta '/api/analizar': Endpoint POST para analizar comandos
        server.createContext("/api/analizar", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Configurar encabezados CORS para habilitar peticiones locales cruzadas
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // Leer cuerpo del POST
                    InputStream is = exchange.getRequestBody();
                    byte[] requestBytes = is.readAllBytes();
                    String requestBody = new String(requestBytes, StandardCharsets.UTF_8);

                    // Extraer los comandos del cuerpo JSON
                    String commands = extractJsonString(requestBody, "commands");
                    if (commands == null) {
                        commands = requestBody; // Fallback por si envían texto plano
                    }

                    String jsonResponse;
                    List<String> tokenJsonList = new ArrayList<>();
                    String tokensJson = "[]";
                    try {
                        // Paso 1: Analizar léxicamente para extraer todos los tokens
                        try {
                            StringReader tokenReader = new StringReader(commands);
                            Lexer tokenLexer = new Lexer(tokenReader);
                            Symbol s;
                            while ((s = tokenLexer.next_token()).sym != sym.EOF) {
                                String tokenName = (s.sym < sym.terminalNames.length) ? sym.terminalNames[s.sym] : "Desconocido";
                                String lexeme = tokenLexer.yytext();
                                int line = s.left;
                                int col = s.right;
                                tokenJsonList.add("{"
                                        + "\"token\":\"" + escapeJson(tokenName) + "\","
                                        + "\"lexema\":\"" + escapeJson(lexeme) + "\","
                                        + "\"linea\":" + line + ","
                                        + "\"columna\":" + col
                                        + "}");
                            }
                        } catch (Exception lexEx) {
                            // Ignorar errores léxicos aquí, ya que el parser o el lexer principal los reportarán
                        }
                        tokensJson = "[" + String.join(",", tokenJsonList) + "]";

                        // Paso 2: Análisis sintáctico
                        StringReader reader = new StringReader(commands);
                        Lexer lexer = new Lexer(reader);
                        parser p = new parser(lexer);
                        Symbol rootSymbol = p.parse();

                        if (rootSymbol != null && rootSymbol.value instanceof Result) {
                            Result r = (Result) rootSymbol.value;
                            String simEscaped = escapeJson(r.simulation);
                            String treeJson = r.node.toJson();

                            jsonResponse = "{"
                                    + "\"status\":\"success\","
                                    + "\"simulacion\":\"" + simEscaped + "\","
                                    + "\"arbol\":" + treeJson + ","
                                    + "\"tokens\":" + tokensJson
                                    + "}";
                        } else {
                            jsonResponse = "{"
                                    + "\"status\":\"error\","
                                    + "\"error\":\"Error al procesar la derivación sintáctica.\","
                                    + "\"tokens\":" + tokensJson
                                    + "}";
                        }
                    } catch (RuntimeException ex) {
                        String errorMsg = escapeJson(ex.getMessage());
                        jsonResponse = "{"
                                + "\"status\":\"error\","
                                + "\"error\":\"" + errorMsg + "\","
                                + "\"tokens\":" + tokensJson
                                + "}";
                    } catch (Exception ex) {
                        String errorMsg = escapeJson(ex.toString());
                        jsonResponse = "{"
                                + "\"status\":\"error\","
                                + "\"error\":\"" + errorMsg + "\","
                                + "\"tokens\":" + tokensJson
                                + "}";
                    }

                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            }
        });

        System.out.println("Servidor corriendo en http://localhost:" + port);
        server.start();
    }

    // Extractor nativo ligero de cadenas de JSON sin dependencias externas
    private static String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int index = json.indexOf(searchKey);
        if (index == -1) return null;
        int colonIndex = json.indexOf(":", index + searchKey.length());
        if (colonIndex == -1) return null;
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote == -1) return null;

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else if (c == 'r') sb.append('\r');
                else sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '\"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Escapador nativo de caracteres especiales de JSON
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
