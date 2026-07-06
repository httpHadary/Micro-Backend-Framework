package middleware;

import httpServer.Request;
import httpServer.Response;

public class AuthenticationMiddleware implements Middleware{
    @Override
    public void execute(Request request, Response response, MiddlewareChain middlewareChain) {

    }
}
