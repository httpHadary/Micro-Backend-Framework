package httpServer;

public class Route {
    private final String method;
    private final String pattern;
    private final RouteHandler handler;
    private final boolean requiresAuthentication;

    public Route(String method, String pattern, RouteHandler handler, boolean requiresAuthentication) {
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
        this.requiresAuthentication = requiresAuthentication;
    }

    public String getMethod() {
        return method;
    }

    public String getPattern() {
        return pattern;
    }

    public RouteHandler getHandler() {
        return handler;
    }
}
