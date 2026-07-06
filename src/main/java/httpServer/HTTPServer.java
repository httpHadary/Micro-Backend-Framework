package httpServer;

import middleware.ExceptionMiddleware;
import middleware.LoggingMiddleware;
import middleware.Middleware;
import middleware.MiddlewareChain;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPServer {
    static String ROOT_DIRECTORY = "";
    private static Router router = new Router();
    private static final List<Middleware> middlewares = new ArrayList<>();

    public static void main(String[] args) {
        for(int i = 0; i < args.length - 1 ; i++) {
            if (args[i].equals("--directory")) ROOT_DIRECTORY = args[i + 1];
        }
        if(ROOT_DIRECTORY.isEmpty()) ROOT_DIRECTORY = "files";

        try (
            ServerSocket serverSocket = new ServerSocket(4221)
        )
        {
            registerRoutes();

            use(new ExceptionMiddleware());
            use(new LoggingMiddleware());


            while (true) {
                Socket connection = serverSocket.accept();
                System.out.println("accepted new connection: " + connection.getInetAddress().getHostName());

                Thread.startVirtualThread(() -> {
                    try {
                        handleClient(connection);
                    } catch (IOException e) {
                        System.out.println("Error accepting connection: " + e.getMessage());
                    }
                });
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

    }

    static void handleClient(Socket connection) throws IOException{
        InputStream input = connection.getInputStream();
        OutputStream output = connection.getOutputStream();

        while (true) {
            Request requestObject = new Request();
            Response responseObject = new Response();

            parseRequest(input, requestObject);

            // client disconnected
            if(requestObject.getVerb() == null) {
                break;
            }

            MiddlewareChain middlewareChain =
                    new MiddlewareChain(
                        middlewares,
                        () -> {
                                try {
                                    handleRouting(requestObject, responseObject);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } );

            middlewareChain.next(requestObject, responseObject);

            byte[] response = getResponse(responseObject, !requestObject.getVerb().equals("HEAD"));
            output.write(response);
            output.flush();

            // to handle keep-alive connections
            String connectionStatus = requestObject.getRequestHeaders().getOrDefault("Connection", "");
            if (connectionStatus.equalsIgnoreCase("close")) {
                break;
            }
        }

        connection.close();
    }

    static void parseRequest(InputStream inputStream, Request request) throws IOException {

        //1) Parse the request line
        String requestLine = readLine(inputStream);
        if (requestLine == null || requestLine.isEmpty()) {
            return;
        }
        request.setRequestLine(requestLine.split( " "));

        //2) Parse the headers
        String header;
        while((header = readLine(inputStream)) != null && !header.isEmpty()) {
            String[] headerParts = header.split(": ", 2);
            request.addRequestHeader(headerParts[0], headerParts[1]);
        }

        //3) Parse the body
        int contentLength = Integer.parseInt(request.getRequestHeaders().getOrDefault("Content-Length", "0"));
        byte[] requestBody = new byte[contentLength];
        int totalRead = 0;

        while (totalRead < contentLength) {
            int bytesRead = inputStream.read(requestBody, totalRead, contentLength - totalRead); //handles data arriving in chunks
            if (bytesRead == -1) break;
            totalRead += bytesRead;
        }

        request.setRequestBody(requestBody);
    }

    static String readLine(InputStream inputStream) throws IOException { //reads a single line of the input till \r\n
        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

        int current;

        while ((current = inputStream.read()) != -1) {
            if (current == '\r') {
                int next = inputStream.read();

                if (next == '\n') {
                    break;
                }
            }

            lineBuffer.write(current);
        }

        return lineBuffer.toString(StandardCharsets.UTF_8);
    }

    static void handleRouting(Request request, Response response) throws IOException {
        String verb = request.getVerb();
        String url = request.getURL();
        String version = request.getVersion();

        if(!version.equals("HTTP/1.1") && !version.equals("HTTP/1.0")) {
            response.setStatusCode("505");
            response.setTextResponseBody("This HTTP Version Is Not Supported.");
            response.addResponseHeader("Content-Type", "text/plain");
            return;
        }
        
        Router.RouteResult result = router.handle(request, response);

        if (result == Router.RouteResult.MATCHED)
            return;
        else if (result == Router.RouteResult.METHOD_NOT_ALLOWED) {
            response.setStatusCode("405");
            response.setTextResponseBody("Method Not Allowed");
            return;
        } else {
            response.setStatusCode("404");
            response.setTextResponseBody("httpServer.Route Is Not Found");
        }
    }

    static void registerRoutes() {
        router.get("/", Handlers::homeHandler, false);
        router.get("/hello", Handlers::helloHandler, false);
        router.get("/user-agent", Handlers::userAgentHandler, false);
        router.get("/echo/{message}", Handlers::echoHandler, false);
        router.get("/search", Handlers::searchHandler, false);
        router.get("/files/{filename}", Handlers::downloadFileHandler, false);
        router.post("/files/{filename}", Handlers::createFileHandler, false);
        router.put("/files/{filename}", Handlers::updateFileHandler, false);
        router.delete("/files/{filename}", Handlers::deleteFileHandler, false);
        router.head("/files/{filename}", Handlers::headHandler, false);
    }

    static byte[] getResponse(Response response, boolean includeBody) throws IOException {
        StringBuilder responseLineAndHeaders = new StringBuilder();

        responseLineAndHeaders.append(response.getResponseLine()).append("\r\n");

        for(Map.Entry<String, String> headers : response.getResponseHeaders().entrySet()) {
            responseLineAndHeaders.append(headers.getKey()).append(": ").append(headers.getValue()).append("\r\n");
        }

        responseLineAndHeaders.append("\r\n");

        ByteArrayOutputStream finalResponse = new ByteArrayOutputStream();

        finalResponse.write(responseLineAndHeaders.toString().getBytes(StandardCharsets.UTF_8));

        if(includeBody)
            finalResponse.write(response.getResponseBody());

        return finalResponse.toByteArray();
    }

    static String getMimeType(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");

        if(dotIndex == -1) {
            return "application/octet-stream";
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase();

        return switch(extension) {
            case "html" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    public static void use(Middleware middleware) {
        if (middleware != null)
            middlewares.add(middleware);
    }

}
