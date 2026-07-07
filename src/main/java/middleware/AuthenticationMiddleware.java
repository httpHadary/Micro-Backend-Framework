package middleware;

import httpServer.Request;
import httpServer.Response;
import httpServer.Route;

import java.util.Map;

public class AuthenticationMiddleware implements Middleware{
    private static final String VALID_TOKEN = "Bearer secret-token";

    @Override
    public void execute(Request request, Response response, MiddlewareChain middlewareChain) {
        Route route = request.getMatchedRoute();

        if (!route.getRequiresAuthentication()) {
            middlewareChain.next(request, response);
            return;
        }

        String authorizationValue = request.getRequestHeaders().get("authorization");

        if (!VALID_TOKEN.equals(authorizationValue)) {
            response.setStatusCode("401");
            response.text("Unauthorized");
            response.addResponseHeader("Content-Type", "text/plain");
            return;
        }

        middlewareChain.next(request, response);
    }
}
