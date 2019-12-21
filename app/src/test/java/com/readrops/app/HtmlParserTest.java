package com.readrops.app;

import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.ParsingResult;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class HtmlParserTest {

    @Test
    public void getFeedLinkTest() throws Exception {
        String url = "https://github.com/readrops/Readrops";

        ParsingResult parsingResult = new ParsingResult("https://github.com/readrops/Readrops/commits/develop.atom", "Recent Commits to Readrops:develop");
        List<ParsingResult> parsingResultList = new ArrayList<>();
        parsingResultList.add(parsingResult);

        List<ParsingResult> parsingResultList1 = HtmlParser.getFeedLink(url);

        Assert.assertEquals(parsingResultList, parsingResultList1);
    }

    @Test
    public void getFaviconLinkTest() throws IOException {
        String url = "https://github.com/readrops/Readrops";

        assertEquals("https://github.com/fluidicon.png", HtmlParser.getFaviconLink(url));
    }
}
