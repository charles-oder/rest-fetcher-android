package us.oder.restfetcher;

public class RestError {
    public final int code;
    public final String reason;

    public RestError(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }
}
