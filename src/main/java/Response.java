import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final String httpVersion = "HTTP/1.1";
    private String statusCode;
    private String codeInterpretation;
    private Map<String, String> responseHeaders;
    private byte[] responseBody;
    private static final Map<String, String> STATUS_CODES = new HashMap<>();

    static {
        STATUS_CODES.put("200", "OK");
        STATUS_CODES.put("201", "Created");
        STATUS_CODES.put("400", "Bad Request");
        STATUS_CODES.put("404", "Not Found");
        STATUS_CODES.put("405", "Method Not Allowed");
        STATUS_CODES.put("500", "Internal Server Error");
        STATUS_CODES.put("505", "HTTP Version Not Supported");
    }

    Response() {
        responseHeaders = new HashMap<>();
        addResponseHeader("Connection", "keep-alive");
    }
    Response(String statusCode, Map<String, String> responseHeaders) {
        this.setStatusCode(statusCode);
        this.setResponseHeaders(responseHeaders);
        addResponseHeader("Connection", "keep-alive");
    }
    Response(String statusCode, Map<String, String> responseHeaders, byte[] responseBody) {
        this.setStatusCode(statusCode);
        this.setResponseHeaders(responseHeaders);
        this.setResponseBody(responseBody);
        addResponseHeader("Connection", "keep-alive");
    }

    public String getHttpVersion() {
        return this.httpVersion;
    }

    public void setStatusCode(String statusCode) {
        if(statusCode != null && !statusCode.isEmpty()) {
            this.statusCode = statusCode;
            this.setCodeInterpretation(STATUS_CODES.get(statusCode));
        }
    }
    public String getStatusCode() {
        return this.statusCode;
    }

    public void setCodeInterpretation(String codeInterpretation) {
        if(codeInterpretation != null && !codeInterpretation.isEmpty())
            this.codeInterpretation = codeInterpretation;
    }
    public String getCodeInterpretation() {
        return this.codeInterpretation;
    }

    public String getResponseLine() {
        return getHttpVersion() + " " + getStatusCode() + " " + getCodeInterpretation();
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        if(responseHeaders != null)
            this.responseHeaders = responseHeaders;
        else
            this.responseHeaders = new HashMap<>();
    }
    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }
    public void addResponseHeader(String key, String value) {
        if(!key.isEmpty() && !value.isEmpty()) {
            this.responseHeaders.put(key, value);
        }
    }

    public void setResponseBody(byte[] responseBody) {
        if(responseBody == null)
            responseBody = new byte[0];

        this.responseBody = responseBody;
        addResponseHeader("Content-Length", this.responseBody.length + "");
    }
    public void setTextResponseBody(String responseBody) {
        if(responseBody == null)
            responseBody = "";

        setResponseBody(responseBody.getBytes(StandardCharsets.UTF_8));
    }
    public byte[] getResponseBody() {
        return this.responseBody;
    }
}