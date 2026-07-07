package middleware;

import httpServer.Request;
import httpServer.Response;

import java.io.File;

public class StaticFilesMiddleware implements Middleware{
    private final String rootDirectory;

    public StaticFilesMiddleware(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public void execute(Request request, Response response, MiddlewareChain middlewareChain) {
        String path = request.getURL().split("\\?", 2)[0];

        if(path.equals("/")) {
            path = "index.html";
        }

        File file = new File(rootDirectory, path.substring(1));

        if (file.exists() && file.isFile()) {
            System.out.println(file.getAbsolutePath());
            return;
        }

        middlewareChain.next(request, response);
    }
}
