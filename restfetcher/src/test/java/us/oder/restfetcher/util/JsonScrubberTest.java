package us.oder.restfetcher.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonScrubberTest {

    @Test
    public void scrubberHidesJsonFieldOnValidJson() {
        String testString = "{\"Username\":\"uberuser\",\"Password\":\"mysecretpassword\",\"nickname\":\"TheDude\"}";
        String[] fieldsToScrub = new String[] {"Password", "Username"};
        String expectedString = "{\"Username\":\"************\",\"Password\":\"************\",\"nickname\":\"TheDude\"}";

        JsonScrubber testObject = new JsonScrubber(fieldsToScrub);

        String actualString = testObject.scrub(testString);

        assertEquals(expectedString, actualString);
    }

    @Test
    public void scrubberHidesCaseInsensitiveJsonFieldOnValidJson() {
        String testString = "{\"Username\":\"uberuser\",\"Password\":\"mysecretpassword\"}";
        String[] fieldsToScrub = new String[] {"Password"};
        String expectedString = "{\"Username\":\"uberuser\",\"Password\":\"************\"}";

        JsonScrubber testObject = new JsonScrubber(fieldsToScrub);

        String actualString = testObject.scrub(testString);

        assertEquals(expectedString, actualString);
    }

    @Test
    public void scrubberReturnsOriginalStringOnInvalidJson() {
        String testString = "\"Username\"=\"uberuser\"&\"Password\"=\"mysecretpassword\"";
        String[] fieldsToScrub = new String[] {"Password"};

        JsonScrubber testObject = new JsonScrubber(fieldsToScrub);

        String actualString = testObject.scrub(testString);

        assertEquals(testString, actualString);
    }

    @Test
    public void scrubberHidesJsonFieldInMultiLevelJson() {
        String testString = "{\"Username\":\"uberuser\",\"Password\":\"mysecretpassword\",\"MoreStuff\":{\"Password\":\"Password\"}}";
        String[] fieldsToScrub = new String[] {"Password"};
        String expectedString = "{\"Username\":\"uberuser\",\"Password\":\"************\",\"MoreStuff\":{\"Password\":\"************\"}}";

        JsonScrubber testObject = new JsonScrubber(fieldsToScrub);

        String actualString = testObject.scrub(testString);

        assertEquals(expectedString, actualString);
    }
}