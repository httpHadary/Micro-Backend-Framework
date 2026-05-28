import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.net.URLDecoder;

public class HTTPServer {
    static String ROOT_DIRECTORY = "";

    public static void main(String[] args) {
        for(int i = 0; i < args.length - 1 ; i++) {
            if (args[i].equals("--directory")) ROOT_DIRECTORY = args[i + 1];
        }
        if(ROOT_DIRECTORY.isEmpty()) ROOT_DIRECTORY = "files";

        try (
            ServerSocket serverSocket = new ServerSocket(4221)
        )
        {

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

            handleRouting(requestObject, responseObject);

            byte[] response = getResponse(responseObject, !requestObject.getVerb().equals("HEAD"));
            output.write(response);
            output.flush();

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

        switch (verb) {
            case "GET" -> handleGETs(request, response, url);
            case "POST" -> handlePOSTs(request, response, url);
            case "PUT" -> handlePUTs(request, response, url);
            case "DELETE" -> handleDELETEs(response, url);
            case "HEAD" -> handleHEADs(request, response, url);
            default -> {
                response.setStatusCode("405");
                response.setTextResponseBody("This HTTP Method Is Not Supported.");
                response.addResponseHeader("Content-Type", "text/plain");
            }
        }
    }

    static void handleGETs(Request request, Response response, String url) throws IOException {
        if(url.equals("/")) {
            response.setStatusCode("200");
            response.addResponseHeader("Content-Type", "text/plain");
            response.setTextResponseBody("Hello From Elhaddour's Server.");
        } else if(url.equals("/hello")) {
            response.setStatusCode("200");
            response.addResponseHeader("Content-Type", "text/plain");
            response.setTextResponseBody("Hello World");
        } else if(url.startsWith("/echo/")) {
            response.setStatusCode("200");
            response.addResponseHeader("Content-Type", "text/plain");
            response.setTextResponseBody(url.substring(6));
        } else if(url.equals("/user-agent")) {
            response.setStatusCode("200");
            response.addResponseHeader("Content-Type", "text/plain");
            response.setTextResponseBody(request.getRequestHeaders().getOrDefault("User-Agent" , "null"));
        } else if(url.startsWith("/search?")) {
            Map<String, String> parameters = request.getQueryParameters();
            response.setStatusCode("200");
            response.addResponseHeader("Content-Type", "text/plain");
            response.setTextResponseBody("Searching For: " + parameters.getOrDefault("q", "java") + "\nPage: " + parameters.getOrDefault("page", "1"));
        } else if (url.startsWith("/files/")) {
            String fileName = URLDecoder.decode(url.substring(7), StandardCharsets.UTF_8);
            File file = new File(ROOT_DIRECTORY, fileName);

            if(file.exists()) {
                byte[] body = Files.readAllBytes(file.toPath());
                response.setStatusCode("200");
                response.addResponseHeader("Content-Type", getMimeType(fileName));
                response.setResponseBody(body);
            } else {
                response.setStatusCode("404");
                response.addResponseHeader("Content-Type", "text/plain");
                response.setTextResponseBody("File Was Not Found");
            }
        } else {
            response.setStatusCode("404");
            response.setTextResponseBody("Route Is Not Found");
        }
    }

    static void handlePOSTs(Request request, Response response, String url) throws IOException {
        if (!url.startsWith("/files/")) {
            response.setStatusCode("404");
            response.setTextResponseBody("Route Is Not Found");
            return;
        }

        int startOfFileName = url.lastIndexOf("/") + 1;
        String fileName = java.net.URLDecoder.decode(url.substring(startOfFileName), StandardCharsets.UTF_8);

        File file = new File(ROOT_DIRECTORY, fileName);

        Files.write(file.toPath(), request.getRequestBody());
        response.setStatusCode("201");
        response.setResponseBody(new byte[0]);
    }

    static void handlePUTs(Request request, Response response, String url) throws IOException {
        if (!url.startsWith("/files/")) {
            response.setStatusCode("404");
            response.setTextResponseBody("Route Is Not Found");
            return;
        }

        int startOfFileName = url.lastIndexOf("/") + 1;
        String fileName = java.net.URLDecoder.decode(url.substring(startOfFileName), StandardCharsets.UTF_8);

        File file = new File(ROOT_DIRECTORY, fileName);
        Files.write(file.toPath(), request.getRequestBody());

        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", "text/plain");
        response.setTextResponseBody("File Updated Successfully");
    }

    static void handleDELETEs(Response response, String url) throws IOException {
        if (!url.startsWith("/files/")) {
            response.setStatusCode("404");
            response.setTextResponseBody("Route Is Not Found");
            return;
        }

        int startOfFileName = url.lastIndexOf("/") + 1;
        String fileName = java.net.URLDecoder.decode(url.substring(startOfFileName), StandardCharsets.UTF_8);

        File file = new File(ROOT_DIRECTORY, fileName);

        boolean isDeleted = Files.deleteIfExists(file.toPath());

        if (isDeleted) {
            response.setStatusCode("200");
            response.setTextResponseBody("File Deleted Successfully");
        } else {
            response.setStatusCode("404");
            response.setTextResponseBody("File Not Found");
        }

        response.addResponseHeader("Content-Type", "text/plain");
    }

    static void handleHEADs(Request request, Response response, String url) throws IOException {
        handleGETs(request, response, url);
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

}
