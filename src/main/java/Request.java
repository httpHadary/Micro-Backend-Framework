import java.util.HashMap;
import java.util.Map;

public class Request {
    private String requestVerb;
    private String requestURL;
    private String httpVersion;
    private Map<String, String> requestHeaders;
    private Map<String, String> pathParameters;
    private byte[] requestBody;

    Request() {
        requestHeaders = new HashMap<>();
        pathParameters = new HashMap<>();
    }
    Request(String[] requestLine, Map<String, String> requestHeaders, Map<String, String> pathParameters) {
        setRequestLine(requestLine);
        setRequestHeaders(requestHeaders);
        setPathParameters(pathParameters);
    }
    Request(String[] requestLine, Map<String, String> requestHeaders, Map<String, String> pathParameters, byte[] requestBody) {
        setRequestLine(requestLine);
        setRequestHeaders(requestHeaders);
        setPathParameters(pathParameters);
        setRequestBody(requestBody);
    }

    public void setRequestLine(String[] requestLine) {
        if(requestLine != null) {
            this.requestVerb = requestLine[0];
            this.requestURL = requestLine[1];
            this.httpVersion = requestLine[2];
        }
    }
    public String getRequestLine() {
        return getVerb() + " " + getURL() + " " + getVersion();
    }
    public String getVerb() {
        return this.requestVerb;
    }
    public String getURL() {
        return this.requestURL;
    }
    public String getVersion() {
        return this.httpVersion;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        if(requestHeaders != null)
            this.requestHeaders = requestHeaders;
        else
            this.requestHeaders = new HashMap<>();
    }
    public Map<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }
    public void addRequestHeader(String key, String value) {
        if(!key.isEmpty() && !value.isEmpty()) {
            this.requestHeaders.put(key, value);
        }
    }

    public void setRequestBody(byte[] requestBody) {
        if(requestBody == null)
            requestBody = new byte[0];
        this.requestBody = requestBody;
    }
    public byte[] getRequestBody() {
        return this.requestBody;
    }

    public Map<String, String> getQueryParameters() {
        if(!requestURL.contains("?")) {
            return new HashMap<>();
        }

        String query = this.requestURL.split("\\?", 2)[1];
        String[] params = query.split("&");
        Map<String, String> parameters = new HashMap<>();

        String[] keyValue;
        for(String param : params) {
            keyValue = param.split("=", 2);
            if(keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1]);
            }
        }

        return parameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        if(pathParameters != null)
            this.pathParameters = pathParameters;
        else
            this.pathParameters = new HashMap<>();
    }
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }
    public String getPathParameter(String key) {
        return pathParameters.get(key);
    }
    public void addPathParameter(String key, String value) {
        pathParameters.put(key, value);
    }
    public void clearPathParameters() {
        pathParameters.clear();
    }
}