package webServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StaticFileServer {
    public static void serve(Request request, Response response, String rootDirectory) {
        String path = request.getURL();

        if (path.equals("/")) {
            path = "/index.html";
        }

        String fileName = rootDirectory + path;

        File file = new File(fileName);

        if (!file.exists() || !file.isFile()) {
            response.setStatusCode("404");
            response.text("Route Is Not Found");
            return;
        }

        byte[] body;

        try {
            body = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            response.setStatusCode("500");
            response.text("Internal Server Error");
            return;
        }

        response.setStatusCode("200");
        response.addResponseHeader("Content-Type", WebServer.getMimeType(fileName));
        response.setResponseBody(body);
    }
}
