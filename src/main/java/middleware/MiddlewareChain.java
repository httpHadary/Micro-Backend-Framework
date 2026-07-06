package middleware;

import httpServer.Request;
import httpServer.Response;

import java.util.ArrayList;
import java.util.List;

public class MiddlewareChain {
    private final List<Middleware> middlewares;
    private int index;
    private final Runnable target;

    public MiddlewareChain(List<Middleware> middlewares, Runnable target) {
        this.middlewares = middlewares;
        this.target = target;
        this.index = 0; // always starts with 0
    }

    public void next(Request request, Response response){
        if (index < middlewares.size()) {
            Middleware currentMiddleware = middlewares.get(index);
            index++;
            currentMiddleware.execute(request, response, this);
        } else {
            target.run();
        }
    }

    public List<Middleware> getMiddlewares() {
        return middlewares;
    }

    public int getIndex() {
        return index;
    }

    public Runnable getTarget() {
        return target;
    }
}
