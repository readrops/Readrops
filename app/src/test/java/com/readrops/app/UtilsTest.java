package com.readrops.app;

import android.graphics.Color;

import com.readrops.app.utils.Utils;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void colorTooBrightTest() {
        assertTrue(Utils.isColorTooBright(-986896));
    }

    @Test
    public void colorTooDarkTest() {
        assertTrue(Utils.isColorTooDark(Color.parseColor("#1a1a1a")));
    }
}
