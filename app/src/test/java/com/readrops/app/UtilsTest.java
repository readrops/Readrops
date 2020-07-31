package com.readrops.app;

import android.graphics.Color;

import com.readrops.app.utils.Utils;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

public class UtilsTest {

    @Test
    public void cleanTextTest() {
        String text = "    <p>This is a text<br/>to</p> clean    ";

        assertEquals("This is a text to clean", Utils.cleanText(text));
    }

    @Test
    public void colorTooBrightTest() {
        assertTrue(Utils.isColorTooBright(-986896));
    }

    @Test
    public void colorTooDarkTest() {
        assertTrue(Utils.isColorTooDark(Color.parseColor("#1a1a1a")));
    }
}
