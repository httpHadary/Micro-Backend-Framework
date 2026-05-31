import java.io.IOException;

@FunctionalInterface
public interface RouteHandler {
    void handle(Request request, Response response) throws IOException;
}
