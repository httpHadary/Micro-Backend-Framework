package httpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Router {
    private final List<Route> routes;
    public enum RouteResult {
        MATCHED, NOT_FOUND, METHOD_NOT_ALLOWED
    }

    Router() {
        routes = new ArrayList<>();
    }

    public void get(String path, RouteHandler handler, boolean requiresAuth) {
        routes.add(new Route("GET", path, handler, requiresAuth));
    }
    public void post(String path, RouteHandler handler, boolean requiresAuth) {
        routes.add(new Route("POST", path, handler, requiresAuth));
    }
    public void put(String path, RouteHandler handler, boolean requiresAuth) {
        routes.add(new Route("PUT", path, handler, requiresAuth));
    }
    public void delete(String path, RouteHandler handler, boolean requiresAuth) {
        routes.add(new Route("DELETE", path, handler, requiresAuth));
    }
    public void head(String path, RouteHandler handler, boolean requiresAuth) {
        routes.add(new Route("HEAD", path, handler, requiresAuth));
    }

    public boolean matches(Route route, Request request) {
        String[] patternParts = splitPath(route.getPattern());
        String[] urlParts = splitPath(request.getURL().split("\\?", 2)[0]); //removes query parameters before splitting

        if(patternParts.length != urlParts.length) return false;

        for(int i = 0 ; i < patternParts.length ; i++) {
            String patternPart = patternParts[i];
            String urlPart = urlParts[i];

            if(patternPart.startsWith("{") && patternPart.endsWith("}")) {
                String key = patternPart.substring(1, patternPart.length() - 1);
                String value = java.net.URLDecoder.decode(urlPart, StandardCharsets.UTF_8);
                request.addPathParameter(key, value);
                continue;
            }

            if (!patternPart.equals(urlPart)) return false;
        }

        return true;
    }

    private String[] splitPath(String path) {
        path = path.replaceFirst("^/", "");

        if (path.isEmpty()) return new String[0];

        return path.split("/");
    }

    public RouteResult handle(Request request, Response response) throws IOException {
        boolean doesPathExists = false;

        for(Route route : routes) {
            request.clearPathParameters();

            if(matches(route, request)) {
                doesPathExists = true;

                if(route.getMethod().equals(request.getVerb())) {

                    route.getHandler().handle(request, response);

                    return RouteResult.MATCHED; //everything is fine
                }
            }
        }

        if(doesPathExists) return RouteResult.METHOD_NOT_ALLOWED; //path matched put with wrong method

        return RouteResult.NOT_FOUND; //both method and path not matched
    }

}
