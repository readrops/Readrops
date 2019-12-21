package com.readrops.app;

import com.readrops.app.utils.Utils;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class UtilsTest {

    @Test
    public void cleanTextTest() {
        String text = "    <p>This is a text<br/>to</p> clean    ";

        assertEquals("This is a text to clean", Utils.cleanText(text));
    }
}
