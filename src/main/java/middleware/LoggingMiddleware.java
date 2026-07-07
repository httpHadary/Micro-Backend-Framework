package middleware;

import httpServer.Request;
import httpServer.Response;

public class LoggingMiddleware implements Middleware {
    @Override
    public void execute(Request request, Response response, MiddlewareChain chain) {
        long startTime = System.nanoTime();

        chain.next(request, response);

        long endTime = System.nanoTime();

        double durationMilliSec = (double) (endTime - startTime) / 1_000_000;

        System.out.printf("[INFO] %s %s %s -> %s (%.3f ms)%n",
                request.getClientIP(),
                request.getVerb(),
                request.getURL(),
                response.getStatusCode(),
                durationMilliSec);
    }
}
