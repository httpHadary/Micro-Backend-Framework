public class Route {
    private final String method;
    private final String pattern;
    private final RouteHandler handler;

    public Route(String method, String pattern, RouteHandler handler) {
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
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
