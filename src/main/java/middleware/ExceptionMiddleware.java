package middleware;

import httpServer.Request;
import httpServer.Response;

public class ExceptionMiddleware implements Middleware{
    @Override
    public void execute(Request request, Response response, MiddlewareChain middlewareChain) {
        try {
            middlewareChain.next(request, response);
        } catch (Exception e) {
            response.setStatusCode("500");
            response.addResponseHeader("content-type", "text/plain");
            response.setTextResponseBody("Internal Server Error!");

            e.printStackTrace();
        }
    }
}
