package us.oder.restfetcher;

import java.util.HashMap;
import java.util.Map;

public class RestApiBase {
    
    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String ACCEPT_KEY = "Accept";

    public static final String DEFAULT_CONTENT_TYPE = "application/json";
    public static final String DEFAULT_ACCEPT = "application/json; version=1";

    public interface OnApiErrorListener {
        void onApiError( RestError error );
    }

    public interface OnApiSuccessListener<T> {
        void onApiSuccess( T response );
    }

    public interface IRestFetcherFactory {
        RestFetcher createRestFetcher(String url, RestMethod method, Map<String, String> headers, String body);
    }

    public static abstract class Request<T extends Response> implements RestFetcher.OnFetchErrorListener, RestFetcher.OnFetchSuccessListener {

        private RestFetcher fetcher;
        private IRestFetcherFactory restFetcherFactory;

        private OnApiSuccessListener<T> onApiSuccessListener;
        private OnApiErrorListener onApiErrorListener;

        public Request() {
            this(new RestFetcherFactory());
        }

        public Request(IRestFetcherFactory restFetcherFactory) {
            this.restFetcherFactory = restFetcherFactory;
        }

        protected abstract String getApiRoute();

        protected abstract String getApiBaseAddress();

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

        public String getRequestBody() {
            return "";
        }

        protected String getApiResource() {
            return getApiBaseAddress() + getApiRoute() + getQueryString();
        }

        private String getQueryString() {
            String output = "";
            boolean firstArg = true;
            for (String key : getQueryArguments().keySet()) {
                output += firstArg ? "?" : "&";
                output += key;
                output += "=";
                output += getQueryArguments().get( key );
                firstArg = false;
            }
            return output;
        }

        protected RestMethod getRestMethod() {
            return RestMethod.GET;
        }

        protected Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
            headers.put(ACCEPT_KEY, DEFAULT_ACCEPT);
            return headers;
        }

        protected RestFetcher getFetcher() {
            if ( fetcher == null ) {
                prepare();
            }
            return fetcher;
        }

        protected T createApiResponse( RestResponse response ) {
            return (T) new Response(response ); // must cast our default impelementation
        }

        public void prepare() {
            fetcher = restFetcherFactory.createRestFetcher( getApiResource(), getRestMethod(), getHeaders(), getRequestBody() );
            fetcher.onFetchErrorListener = this;
            fetcher.onFetchSuccessListener = this;
        }

        public void fetch() {
            getFetcher().fetch();
        }

        public void fetchAsync() {
            getFetcher().fetchAsync();
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

        protected Map<String, String> getQueryArguments() {
            return new HashMap<>();
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

    private static class RestFetcherFactory implements IRestFetcherFactory {

        @Override
        public RestFetcher createRestFetcher(String url, RestMethod method, Map<String, String> headers, String body) {
            return new RestFetcher( url, method, headers, body );
        }
    }

}
