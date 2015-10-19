package us.oder.restfetcher;

import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Config(sdk = 18)
@RunWith(RobolectricTestRunner.class)
public class RestFetcherTest {

    private String url;
    private RestMethod method;
    private Map<String, String> headers;
    private String body;
    private String mockResponseBody = "";

    @Mock
    RestFetcher.OnFetchErrorListener mockOnFetchErrorListener;

    @Mock
    RestFetcher.OnFetchSuccessListener mockOnFetchSuccessListener;
    private boolean fetched;

    @Mock
    HttpURLConnection mockHttpURLConnection;

    @Mock
    OutputStream mockOutputStream;


    private Handler handler = new Handler();

    private int lastResponseCode;
    private String lastResponseBody;
    private Map<String, String> lastResponseHeaders;
    private Map<java.lang.String, List<String>> mockResponseHeaders;

    class MockConnectionFactory implements RestFetcher.IConnectionFactory{
        public String url = "";
        @Override
        public URLConnection createHttpURLConnection( String url ) throws IOException {
            this.url = url;
            return mockHttpURLConnection;
        }
    }
    MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks( this );
        mockResponseHeaders = new HashMap<>();
        List<String> headerValue1 = new ArrayList<>();
        headerValue1.add( "value" );
        mockResponseHeaders.put( "key1", headerValue1 );
        List<String> headerValue2 = new ArrayList<>();
        headerValue2.add( "value" );
        headerValue2.add( "another value" );
        mockResponseHeaders.put( "key2", headerValue2 );
        when( mockHttpURLConnection.getHeaderFields() ).thenReturn( mockResponseHeaders );
        when(mockHttpURLConnection.getResponseCode()).thenReturn( 200 );
        when(mockHttpURLConnection.getInputStream()).thenReturn( getMockInputStream( mockResponseBody ) );
        when(mockHttpURLConnection.getOutputStream()).thenReturn( mockOutputStream );
        lastResponseCode = 0;
        lastResponseBody = "";
        lastResponseHeaders = new HashMap<>();
        url = "http://google.com";
        headers = new HashMap<>();
        headers.put("sample", "header");
        body = "{}";
        InputStream stream = new ByteArrayInputStream(body.getBytes( "UTF-8" ));

    }

    private InputStream getMockInputStream(String str) {
        try {
            return new ByteArrayInputStream(str.getBytes( "UTF-8" ));
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void makeGetRequest() throws IOException {
        RestFetcher fetcher = new RestFetcher( "http://google.com", RestMethod.GET, headers, mockResponseBody, mockConnectionFactory, handler );
        mockResponseBody = "{\"cracker\":\"monkey\"}";
        when(mockHttpURLConnection.getInputStream()).thenReturn( getMockInputStream( mockResponseBody ) );
        fetcher.onFetchSuccessListener = new RestFetcher.OnFetchSuccessListener() {
            @Override
            public void onFetchSuccess( RestResponse response ) {
                System.out.println();
                lastResponseCode = response.code;
                lastResponseBody = response.body;
                lastResponseHeaders = response.headers;
            }
        };
        fetcher.fetch();
        verify(mockHttpURLConnection).setRequestMethod( "GET" );
        verify(mockHttpURLConnection).setRequestProperty( "sample", "header" );
        assertEquals( "http://google.com", mockConnectionFactory.url );
        assertEquals( 200, lastResponseCode );
        assertEquals( "{\"cracker\":\"monkey\"}", lastResponseBody );
        assertEquals( 2, lastResponseHeaders.size() );
        assertEquals( "value", lastResponseHeaders.get( "key1" ) );
        assertEquals( "value;another value", lastResponseHeaders.get( "key2" ) );
    }

    @Test
    public void makePostRequest() throws Exception {
        body = "{\"thing\":\"one\"}";
        RestFetcher fetcher = new RestFetcher(url, RestMethod.POST, headers, body, mockConnectionFactory, handler );
        fetcher.onFetchSuccessListener = new RestFetcher.OnFetchSuccessListener() {
            @Override
            public void onFetchSuccess( RestResponse response ) {
                System.out.println();
                lastResponseCode = response.code;
                lastResponseBody = response.body;
                lastResponseHeaders = response.headers;
            }
        };
        fetcher.fetch();
        verify(mockHttpURLConnection).setRequestMethod( "POST" );
        verify(mockHttpURLConnection).setRequestProperty( "sample", "header" );
        assertEquals( "http://google.com", mockConnectionFactory.url );
        assertEquals( 200, lastResponseCode );
        assertEquals( "", lastResponseBody );
        assertEquals( 2, lastResponseHeaders.size() );
        assertEquals( "value", lastResponseHeaders.get( "key1" ) );
        assertEquals( "value;another value", lastResponseHeaders.get( "key2" ) );

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass( byte[].class );
        verify(mockOutputStream).write(captor.capture(), anyInt(), anyInt());
        assertEquals("{\"thing\":\"one\"}", RestFetcher.convertInputStreamToString(new ByteArrayInputStream(captor.getValue())));
    }

    @Test
    public void makePutRequest() throws Exception {
        body = "{\"thing\":\"one\"}";
        RestFetcher fetcher = new RestFetcher(url, RestMethod.PUT, headers, body, mockConnectionFactory, handler );
        fetcher.onFetchSuccessListener = new RestFetcher.OnFetchSuccessListener() {
            @Override
            public void onFetchSuccess( RestResponse response ) {
                System.out.println();
                lastResponseCode = response.code;
                lastResponseBody = response.body;
                lastResponseHeaders = response.headers;
            }
        };
        fetcher.fetch();
        verify(mockHttpURLConnection).setRequestMethod( "PUT" );
        verify(mockHttpURLConnection).setRequestProperty( "sample", "header" );
        assertEquals( "http://google.com", mockConnectionFactory.url );
        assertEquals( 200, lastResponseCode );
        assertEquals( "", lastResponseBody );
        assertEquals( 2, lastResponseHeaders.size() );
        assertEquals( "value", lastResponseHeaders.get( "key1" ) );
        assertEquals( "value;another value", lastResponseHeaders.get( "key2" ) );

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass( byte[].class );
        verify(mockOutputStream).write(captor.capture(), anyInt(), anyInt());
        assertEquals("{\"thing\":\"one\"}", RestFetcher.convertInputStreamToString(new ByteArrayInputStream(captor.getValue())));
    }

    @Test
    public void makeDeleteRequest() throws Exception {
        RestFetcher fetcher = new RestFetcher(url, RestMethod.DELETE, headers, body, mockConnectionFactory, handler );
        fetcher.onFetchSuccessListener = new RestFetcher.OnFetchSuccessListener() {
            @Override
            public void onFetchSuccess( RestResponse response ) {
                System.out.println();
                lastResponseCode = response.code;
                lastResponseBody = response.body;
                lastResponseHeaders = response.headers;
            }
        };
        fetcher.fetch();
        verify(mockHttpURLConnection).setRequestMethod( "DELETE" );
        verify(mockHttpURLConnection).setRequestProperty( "sample", "header" );
        assertEquals( "http://google.com", mockConnectionFactory.url );
        assertEquals( 200, lastResponseCode );
        assertEquals( "", lastResponseBody );
        assertEquals( 2, lastResponseHeaders.size() );
        assertEquals( "value", lastResponseHeaders.get( "key1" ) );
        assertEquals( "value;another value", lastResponseHeaders.get( "key2" ) );

    }

    @Test
    public void fetchErrorInvokesCallback() throws IOException {
        RestFetcher restFetcher = new RestFetcher(url, RestMethod.GET, headers, body, mockConnectionFactory, handler );
        restFetcher.onFetchErrorListener = mockOnFetchErrorListener;
        when(mockHttpURLConnection.getInputStream()).thenThrow(new IOException());

        ArgumentMatcher<RestError> errorMatcher = new ArgumentMatcher<RestError>() {
            @Override
            public boolean matches(Object argument) {
                RestError e = (RestError)argument;
                return (e.reason.equals("NOT FOUND") && e.code == 404);
            }
        };

        restFetcher.fetch();


        verify(mockOnFetchErrorListener).onFetchError(argThat(errorMatcher));
    }

    @Test
    public void fetchSuccessInvokesCallback() throws IOException {
        mockResponseBody = "{\"cracker\":\"monkey\"}";
        when(mockHttpURLConnection.getInputStream()).thenReturn( getMockInputStream( mockResponseBody ) );
        final Map<String, String> expectedResponseHeaders = new HashMap<>();
        expectedResponseHeaders.put("crackers", "monkey");

        RestFetcher restFetcher = new RestFetcher(url, RestMethod.GET, headers, body, mockConnectionFactory, handler );
        restFetcher.onFetchSuccessListener = mockOnFetchSuccessListener;
        RestFetcher fetcher = new RestFetcher( "http://google.com", RestMethod.GET, headers, mockResponseBody, mockConnectionFactory, handler );
        fetcher.onFetchSuccessListener = new RestFetcher.OnFetchSuccessListener() {
            @Override
            public void onFetchSuccess( RestResponse response ) {
                System.out.println();
                lastResponseCode = response.code;
                lastResponseBody = response.body;
                lastResponseHeaders = response.headers;
            }
        };
        fetcher.fetch();
        verify(mockHttpURLConnection).setRequestMethod( "GET" );
        verify(mockHttpURLConnection).setRequestProperty( "sample", "header" );
        assertEquals( "http://google.com", mockConnectionFactory.url );
        assertEquals( 200, lastResponseCode );
        assertEquals( "{\"cracker\":\"monkey\"}", lastResponseBody );
        assertEquals( 2, lastResponseHeaders.size() );
        assertEquals( "value", lastResponseHeaders.get( "key1" ) );
        assertEquals( "value;another value", lastResponseHeaders.get( "key2" ) );

    }

    @Test
    public void fetchErrorDoesNotBombWithNoErrorListener() throws IOException {
        RestFetcher restFetcher = new RestFetcher(url, RestMethod.GET, headers, body, mockConnectionFactory, new Handler());
        when(mockHttpURLConnection.getInputStream()).thenThrow(new IOException());

        restFetcher.fetch();  // No exception expected
    }

    @Test
    public void errorResponsesCallErrorCallback() throws IOException {
        mockResponseBody = "{\"cracker\":\"monkey\"}";
        when(mockHttpURLConnection.getInputStream()).thenReturn( getMockInputStream( mockResponseBody ) );
        when(mockHttpURLConnection.getResponseCode()).thenReturn( 400 );



        RestFetcher fetcher = new RestFetcher( "http://google.com", RestMethod.GET, headers, mockResponseBody, mockConnectionFactory, handler );
        fetcher.onFetchErrorListener = new RestFetcher.OnFetchErrorListener() {
            @Override
            public void onFetchError( RestError error ) {
                lastResponseCode = error.code;
                lastResponseBody = error.reason;
            }
        };
        fetcher.fetch();
        verify(mockHttpURLConnection ).setRequestMethod( "GET" );
        verify(mockHttpURLConnection ).setRequestProperty( "sample", "header" );
        assertEquals( "http://google.com", mockConnectionFactory.url );
        assertEquals( 400, lastResponseCode );
        assertEquals( mockResponseBody, lastResponseBody );
//        HttpResponse expectedResponse = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 400, "BAD_REQUEST");
//        expectedResponse.setHeader("crackers", "monkey");
//        BasicHttpEntity entity = new BasicHttpEntity();
//        InputStream stream = new ByteArrayInputStream(expectedResponseBody.getBytes("UTF-8"));
//        entity.setContent(stream);
//        expectedResponse.setEntity(entity);
//
//        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(expectedResponse);
//
//        RestFetcher.client = mockHttpClient;

    }

    @Test
    public void fetchAsynchPerformsHttpRequest() throws IOException, InterruptedException {
        RestFetcher restFetcher = new RestFetcher(url, RestMethod.GET, headers, body);
        final Semaphore semaphore = new Semaphore(0);
//        RestFetcher.client = mockHttpClient;
        fetched = false;
        restFetcher.onFetchSuccessListener = new RestFetcher.OnFetchSuccessListener() {
            @Override
            public void onFetchSuccess(RestResponse response) {
                fetched = true;
                semaphore.release();
            }
        };
//        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, ""));
        restFetcher.fetchAsync();
        semaphore.acquire();
        assertTrue(fetched);
    }

    @Test
    public void covertInputStreamToStringWithNullInputStream() {
        boolean exceptionThrown = false;
        try {
            RestFetcher.convertInputStreamToString(null);
        } catch (IOException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void covertInputStreamToStringWithBadInputStream() throws IOException {
        InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.available()).thenThrow(new IOException());
        when(mockInputStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
        boolean exceptionThrown = false;
        try {
            RestFetcher.convertInputStreamToString(mockInputStream);
        } catch (IOException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}
