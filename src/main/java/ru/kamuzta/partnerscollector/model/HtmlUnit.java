package ru.kamuzta.partnerscollector.model;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.javascript.*;

import java.util.logging.*;

public class HtmlUnit {
    private static final String PROXY_HOST = "165.227.105.152";
    private static final int PROXY_PORT = 8080;

    public static WebClient getWebClient() {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX); //, PROXY_HOST, PROXY_PORT);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        return webClient;
    }
}

