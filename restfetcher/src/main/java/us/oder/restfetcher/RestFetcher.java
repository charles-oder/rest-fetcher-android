package us.oder.restfetcher;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.oder.restfetcher.util.JsonScrubber;

public class RestFetcher {

    private static String[] fieldsToScrub = new String[]{"password", "username"};
    private static final String TAG = RestFetcher.class.getSimpleName();

    private final IConnectionFactory connectionFactory;

    private String url;
    private Map<String, String> headers;
    private RestMethod method;
    private String body;
    public OnFetchErrorListener onFetchErrorListener;
    public OnFetchSuccessListener onFetchSuccessListener;

    public String getUrl() {
        return url;
    }

    public RestMethod getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public interface OnFetchErrorListener {
        void onFetchError( RestError error );
    }

    public interface OnFetchSuccessListener {
        void onFetchSuccess( RestResponse response );
    }

    public RestFetcher( String url, RestMethod method, Map<String, String> headers, String body ) {
        this( url, method, headers, body, new ConnectionFactory() );
    }

    public RestFetcher( String url, RestMethod method, Map<String, String> headers, String body, IConnectionFactory factory ) {
        this.url = url;
        this.headers = headers;
        this.method = method;
        this.body = body;
        this.connectionFactory = factory;
    }

    public static void setFieldsToScrub( String[] fieldsToScrub ) {
        RestFetcher.fieldsToScrub = fieldsToScrub;
    }

    private void processRestResponse(RestResponse restResponse) {
        if ( restResponse != null ) {
            sendResponse( restResponse );
        } else {
            sendError( new RestError( 404, "NOT FOUND" ) );
        }
    }

    private void sendResponse(RestResponse restResponse) {
        if (restResponse.code > 199 && restResponse.code < 300) {
            sendSuccess(restResponse);
        } else {
            sendError( new RestError( restResponse.code, restResponse.body ) );
        }
    }

    private RestResponse performRequest() throws IOException {
        logRequest();
        RestResponse output = null;
        HttpURLConnection conn = null;
        try {

            conn = establishConnection();
            
            String body = getBodyString(conn);

            Map<String, String> responseHeaders = extractResponseHeaders(conn);

            output = new RestResponse(conn.getResponseCode(), responseHeaders, body);
        } catch (IOException e) {
            throw e;
        } finally {
            if ( conn != null ) {
                conn.disconnect();
            }
        }
        logResponse( output );
        return output;
    }

    @NonNull
    private String getBodyString(HttpURLConnection conn) {
        String body = "";
        try {
            InputStream is = conn.getInputStream();
            if (is != null) {
                body = convertInputStreamToString(is);
            }
        } catch (IOException e) {
            // Just don't populate the body
        }
        return body;
    }

    @NonNull
    private Map<String, String> extractResponseHeaders( HttpURLConnection conn ) {
        Map<String, String> responseHeaders = new HashMap<>();
        Map<String, List<String>> incomingHeaders = conn.getHeaderFields();
        for (String h : incomingHeaders.keySet()) {
            String header = "";
            for (int i = 0; i < incomingHeaders.get( h ).size(); i++) {
                header += incomingHeaders.get( h ).get( i );
                header += i == incomingHeaders.get( h ).size() - 1 ? "" : ";";
            }
            responseHeaders.put(h, header);
        }
        return responseHeaders;
    }

    private void sendSuccess( final RestResponse restResponse) {
        if (onFetchSuccessListener != null) {
            onFetchSuccessListener.onFetchSuccess( restResponse );
        }
    }

    private void sendError( final RestError error) {
        if (onFetchErrorListener != null) {
            onFetchErrorListener.onFetchError( error );
        }
    }


    private HttpURLConnection establishConnection() throws IOException {
        HttpURLConnection output;
        if (method == RestMethod.POST || method == RestMethod.PUT) {
            output = createBodyConnection( method );
        } else {
            output = createConnection( method );
        }
        return output;
    }

    private HttpURLConnection createConnection( RestMethod method ) throws IOException {
        HttpURLConnection conn = (HttpURLConnection)connectionFactory.createHttpURLConnection(url);
        conn.setRequestMethod( method.toString() );
        injectHeaders( conn );
        return conn;
    }

    private HttpURLConnection createBodyConnection( RestMethod method ) throws IOException {
        HttpURLConnection conn = (HttpURLConnection)connectionFactory.createHttpURLConnection( url );
        conn.setRequestMethod( method.toString() );
        injectHeaders( conn );
        writeBody( conn );
        return conn;
    }

    private void writeBody( HttpURLConnection conn ) throws IOException {
        conn.setDoOutput( true );
        OutputStreamWriter writer = new OutputStreamWriter( conn.getOutputStream() );
        writer.write( body );
        writer.close();
    }

    private void injectHeaders( HttpURLConnection conn ) {
        for (String key : headers.keySet()) {
            conn.setRequestProperty( key, headers.get( key ) );
        }
    }

    private void logRequest() {
        Log.d( TAG, "Request URL: " + url );
        Log.d( TAG, "Request Method: " + headers );
        String headerLog = "Request Headers: ";
        for(String key : headers.keySet()) {
            headerLog += " | " + key + " : " + headers.get( key );
        }
        Log.d( TAG, headerLog );
        JsonScrubber scrubber = new JsonScrubber( fieldsToScrub );
        Log.d( TAG, "Request Body: " + scrubber.scrub( body ) );
    }

    private void logResponse(RestResponse response) {
        if (response != null) {
            Log.d( TAG, "Response Code: " + response.code );
            String headerLog = "Response Headers: ";
            Map<String, String> headers = response.headers;
            for (String key : headers.keySet()) {
                headerLog += " | " + key + " : " + headers.get(key);
            }
            Log.d( TAG, headerLog );
            Log.d( TAG, "Response Body: " + response.body );
        } else {
            Log.d( TAG, "No Response Received!" );
        }
    }

    public void fetch() {
        RestResponse restResponse;
        try {
            restResponse = performRequest();
        } catch ( IOException e ) {
            restResponse = getServerConnectionErrorResponse();
        }
        processRestResponse( restResponse );

    }

    public void fetchAsync() {
        new AsyncTask<Void, Void, RestResponse>() {

            @Override
            protected RestResponse doInBackground(Void... params) {
                try {
                    return performRequest();
                } catch ( IOException e ) {
                    return getServerConnectionErrorResponse();
                }
            }

            @Override
            protected void onPostExecute(RestResponse restResponse) {
                processRestResponse(restResponse);
            }
        }.execute();
    }

    @NonNull
    private RestResponse getServerConnectionErrorResponse() {
        return new RestResponse( 404, new HashMap<String, String>(), "Could not reach server" );
    }

    public static String convertInputStreamToString( InputStream inputStream ) throws IOException {
        String line;
        String result = "";
        if ( inputStream != null ) {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
            try {
                while ( ( line = bufferedReader.readLine() ) != null ) {
                    result += line;
                }
            } catch ( IOException e ) {
                throw e;
            } finally {
                inputStream.close();
            }
        } else {
            throw new IOException( "InputStream: null" );
        }
        return result.trim();
    }

    public interface IConnectionFactory {
        URLConnection createHttpURLConnection(String url) throws IOException;
    }

    public static class ConnectionFactory implements IConnectionFactory {
        @Override
        public URLConnection createHttpURLConnection( String url ) throws IOException {
            URL u = new URL( url );
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setReadTimeout( 10000 );
            conn.setConnectTimeout( 15000 );
            return conn;
        }
    }


}
