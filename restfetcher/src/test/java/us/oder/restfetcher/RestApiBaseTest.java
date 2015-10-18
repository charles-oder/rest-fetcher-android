package us.oder.restfetcher;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

public class RestApiBaseTest {

    RestApiBase.Request<RestApiBase.Response> testObject;

    @Mock
    RestFetcher mockRestFetcher;

    @Mock
    RestApiBase.OnApiErrorListener mockOnApiErrorListener;

    @Mock
    RestApiBase.OnApiSuccessListener<RestApiBase.Response> mockOnApiSuccessListener;

    class ConcreteApiRequest extends RestApiBase.Request<RestApiBase.Response> {

        @Override
        protected String getApiBaseAddress() {
            return "http://google.com";
        }

        @Override
        protected String getApiRoute() {
            return "/api";
        }
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks( this );
        testObject = new ConcreteApiRequest();
    }

    @Test
    public void getApiResourceReturnsBaseValues () {
        assertEquals( "http://google.com/api", testObject.getApiResource() );
        assertEquals( RestMethod.GET, testObject.getRestMethod() );
        assertEquals( "", testObject.getRequestBody() );
        assertEquals( "application/json", testObject.getHeaders().get( "Content-Type" ) );
        assertEquals( "application/json; version=1", testObject.getHeaders().get( "Accept" ) );
    }


    @Test
    public void prepareSetsUpRestFetcherCorrectly () {

        String expectedUrl = "http://google.com/api";
        RestMethod expectedMethod = RestMethod.GET;
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Content-Type", "application/json");
        expectedHeaders.put("Accept","application/json; version=1");
        String expectedBody = "";

        testObject.prepare();

        assertEquals( expectedUrl, testObject.fetcher.getUrl() );
        assertEquals( expectedMethod, testObject.fetcher.getMethod() );
        for (String key : expectedHeaders.keySet()) {
            assertEquals(expectedHeaders.get(key), testObject.fetcher.getHeaders().get(key));
        }
        assertEquals( expectedBody, testObject.fetcher.getBody() );
    }

    @Test
    public void getFetcherCreatesNewFetcher() {
        assertNull(testObject.fetcher);
        assertNotNull( testObject.getFetcher() );
        assertNotNull( testObject.fetcher );
    }

    @Test
    public void fetchExecutesRestFetcher() {
        testObject.fetcher = mockRestFetcher;
        testObject.fetch();

        verify(mockRestFetcher).fetch();
    }

    @Test
    public void fetchAsyncExecutesRestFetcher() {
        testObject.fetcher = mockRestFetcher;
        testObject.fetchAsync();

        verify(mockRestFetcher).fetchAsync();
    }

    @Test
    public void restErrorCallsErrorCallback() {

        final RestError expectedError = new RestError(400, "BAD REQUEST");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                testObject.onFetchError(expectedError);
                return null;
            }
        }).when(mockRestFetcher).fetch();
        testObject.fetcher = mockRestFetcher;
        testObject.setOnApiErrorListener(mockOnApiErrorListener);
        testObject.fetch();

        ArgumentMatcher<RestError> errorMatcher = new ArgumentMatcher<RestError>() {
            @Override
            public boolean matches(Object argument) {
                RestError error = (RestError)argument;
                return error.code == 400;
            }
        };

        verify(mockRestFetcher).fetch();
        verify(mockOnApiErrorListener).onApiError( argThat( errorMatcher ) );
    }

    @Test
    public void doesNotBombIfNoErrorListenerPresent() {
        testObject.onFetchError( null ); //should not throw nullpointer
    }

    @Test
    public void restSuccessCallsSuccessCallback() {
        final RestResponse expectedRestResponse = new RestResponse(200, new HashMap<String,String>(), "");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                testObject.onFetchSuccess(expectedRestResponse);
                return null;
            }
        }).when(mockRestFetcher).fetch();
        testObject.fetcher = mockRestFetcher;
        testObject.setOnApiSuccessListener(mockOnApiSuccessListener);
        testObject.fetch();

        ArgumentMatcher<RestApiBase.Response> responseMatcher = new ArgumentMatcher<RestApiBase.Response>() {
            @Override
            public boolean matches(Object argument) {
                RestApiBase.Response response = (RestApiBase.Response)argument;
                return response.restResponse.code == 200;
            }
        };

        verify(mockOnApiSuccessListener).onApiSuccess( argThat( responseMatcher ) );
    }

    @Test
    public void doesNotBombIfNoSuccessListenerPresent() {
        testObject.onFetchSuccess( null ); //should not throw nullpointer
    }

    boolean processCalled = false;

    @Test
    public void creatingResponseCallsProcessResponse() {
        RestResponse restResponse = new RestResponse(200, new HashMap<String, String>(), "{}");
        RestApiBase.Response concreteResponse = new RestApiBase.Response(restResponse) {
            @Override
            protected void processResponse(RestResponse response) {
                processCalled = true;
            }
        };
        assertEquals(restResponse, concreteResponse.restResponse);
        assertTrue( processCalled );
    }

}