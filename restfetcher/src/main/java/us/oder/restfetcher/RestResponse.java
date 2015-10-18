package us.oder.restfetcher;

import java.util.Map;

public class RestResponse {
    public final int code;
    public final Map<String, String> headers;
    public final String body;

    public RestResponse(int code, Map<String, String> headers, String body) {
        this.code = code;
        this.headers = headers;
        this.body = body;
    }
}
