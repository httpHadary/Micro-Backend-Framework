package middleware;
import httpServer.Request;
import httpServer.Response;

public interface Middleware {
    void execute(Request request, Response response, MiddlewareChain chain);
}
