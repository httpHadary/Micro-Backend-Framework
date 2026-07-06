package httpServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class Handlers {

    public static void homeHandler(Request request, Response response) {
        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", "text/plain");
        response.setTextResponseBody("Hello From Elhaddour's Server.");
    }

    public static void helloHandler(Request request, Response response) {
        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", "text/plain");
        response.setTextResponseBody("Hello World");
    }

    public static void userAgentHandler(Request request, Response response) {
        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", "text/plain");
        response.setTextResponseBody(request.getRequestHeaders().getOrDefault("User-Agent" , "null"));
    }

    public static void echoHandler(Request request, Response response) {
        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", "text/plain");
        response.setTextResponseBody(request.getPathParameter("message"));
    }

    public static void downloadFileHandler(Request request, Response response) throws IOException {
        String fileName = request.getPathParameter("filename");
        File file = new File(HTTPServer.ROOT_DIRECTORY, fileName);

        if (file.exists()) {
            byte[] body = Files.readAllBytes(file.toPath());
            response.setStatusCode("200");
            response.addResponseHeader("Content-Type", HTTPServer.getMimeType(fileName));
            response.setResponseBody(body);
        } else {
            response.setStatusCode("404");
            response.addResponseHeader("Content-Type", "text/plain");
            response.setTextResponseBody("File Was Not Found");
        }
    }

    public static void createFileHandler(Request request, Response response) throws IOException {
        String fileName = request.getPathParameter("filename");
        File file = new File(HTTPServer.ROOT_DIRECTORY, fileName);

        Files.write(file.toPath(), request.getRequestBody());
        response.setStatusCode("201");
        response.setResponseBody(new byte[0]);
    }

    public static void updateFileHandler(Request request, Response response) throws IOException {
        String fileName = request.getPathParameter("filename");
        File file = new File(HTTPServer.ROOT_DIRECTORY, fileName);

        Files.write(file.toPath(), request.getRequestBody());

        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", "text/plain");
        response.setTextResponseBody("File Updated Successfully");
    }

    public static void deleteFileHandler(Request request, Response response) throws IOException {
        String fileName = request.getPathParameter("filename");
        File file = new File(HTTPServer.ROOT_DIRECTORY, fileName);

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

    public static void headHandler(Request request, Response response) throws IOException {
        downloadFileHandler(request, response);
    }

    public static void searchHandler(Request request, Response response) {
        Map<String, String> parameters = request.getQueryParameters();
        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", "text/plain");
        response.setTextResponseBody("Searching For: " + parameters.getOrDefault("q", "java") + "\nPage: " + parameters.getOrDefault("page", "1"));
    }
}
