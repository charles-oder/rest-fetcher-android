package us.oder.restfetcher;

import java.util.HashMap;
import java.util.Map;

public class RestApiBase {

    private static final String TAG = RestApiBase.class.getSimpleName();

    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String ACCEPT_KEY = "Accept";

    public static final String DEFAULT_CONTENT_TYPE = "application/json";
    public static final String DEFAULT_ACCEPT = "application/json; version=1";

    private static RestMethod defaultBaseRestMethod = RestMethod.GET;
    private static String defaultRequestBody = "";

    public interface OnApiErrorListener {
        void onApiError( RestError error );
    }

    public interface OnApiSuccessListener<T> {
        void onApiSuccess( T response );
    }

    public static abstract class Request<T extends Response> implements RestFetcher.OnFetchErrorListener, RestFetcher.OnFetchSuccessListener {

        public OnApiSuccessListener<T> getOnApiSuccessListener() {
            return onApiSuccessListener;
        }

        public void setOnApiSuccessListener( OnApiSuccessListener<T> onApiSuccessListener ) {
            this.onApiSuccessListener = onApiSuccessListener;
        }

        public OnApiErrorListener getOnApiErrorListener() {
            return onApiErrorListener;
        }

        public void setOnApiErrorListener( OnApiErrorListener onApiErrorListener ) {
            this.onApiErrorListener = onApiErrorListener;
        }

        public RestFetcher fetcher;
        private OnApiSuccessListener<T> onApiSuccessListener;
        private OnApiErrorListener onApiErrorListener;

        public String getRequestBody() {
            return defaultRequestBody;
        }

        protected String getApiResource() {
            return getApiBaseAddress() + getApiRoute();
        }

        protected abstract String getApiRoute();

        protected abstract String getApiBaseAddress();

        public RestMethod getRestMethod() {
            return defaultBaseRestMethod;
        }

        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
            headers.put(ACCEPT_KEY, DEFAULT_ACCEPT);
            return headers;
        }

        public void prepare() {
            fetcher = new RestFetcher( getApiResource(), getRestMethod(), getHeaders(), getRequestBody() );
            fetcher.onFetchErrorListener = this;
            fetcher.onFetchSuccessListener = this;
        }

        public void fetch() {
            getFetcher().fetch();
        }

        public void fetchAsync() {
            getFetcher().fetchAsync();
        }

        public RestFetcher getFetcher() {
            if ( fetcher == null ) {
                prepare();
            }
            return fetcher;
        }

        public T createApiResponse( RestResponse response ) {
            return (T) new Response(response ); // must cast our default impelementation
        }

        @Override
        public void onFetchError( RestError error ) {
            if ( getOnApiErrorListener() != null ) {
                getOnApiErrorListener().onApiError( error );
            }
        }

        @Override
        public void onFetchSuccess( RestResponse response ) {
            if ( getOnApiSuccessListener() != null ) {
                T apiResponse = createApiResponse( response );
                onApiSuccessListener.onApiSuccess( apiResponse );
            }
        }
    }

    public static class Response {

        public RestResponse restResponse;

        public Response(RestResponse restResponse) {
            this.restResponse = restResponse;
            processResponse(this.restResponse);
        }

        protected void processResponse(RestResponse response){
        }
    }

}
