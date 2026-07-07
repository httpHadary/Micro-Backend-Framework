package middleware;

import httpServer.Request;
import httpServer.Response;

public class CorsMiddleware implements Middleware{
    @Override
    public void execute(Request request, Response response, MiddlewareChain middlewareChain) {

        if ("OPTIONS".equals(request.getVerb())) {
            response.setStatusCode("204");
            response.addResponseHeader("Access-Control-Allow-Origin", "*");
            response.addResponseHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD, OPTIONS");

            String requestedHeaders = request.getRequestHeaders().getOrDefault("Access-Control-Request-Headers", "Authorization, Content-Type");

            response.addResponseHeader("Access-Control-Allow-Headers", requestedHeaders);

            response.setResponseBody(new byte[0]);
            return;
        }

        middlewareChain.next(request, response);

        response.addResponseHeader("Access-Control-Allow-Origin", "*");
    }
}
