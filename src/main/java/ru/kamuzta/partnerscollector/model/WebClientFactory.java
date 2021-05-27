package ru.kamuzta.partnerscollector.model;

import ru.kamuzta.partnerscollector.entities.MyProxy;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.javascript.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.*;

public class WebClientFactory {
    private static final String PROXY_LIST_NAME = "proxylist.txt";
    private static Queue<MyProxy> proxyQueue = new ConcurrentLinkedQueue<>();
    private static Map<MyProxy,WebClient> webClientMap = new ConcurrentHashMap<>();

    static {
        readProxyFile();
    }

    public static void removeWebClient(WebClient webClient) {
        MyProxy myProxy = null;
        if (webClientMap.containsValue(webClient)) {
            for (Map.Entry<MyProxy,WebClient> entry : webClientMap.entrySet()) {
                if (entry.getValue() == webClient) {
                    myProxy = entry.getKey();
                    proxyQueue.remove(myProxy);
                    break;
                }
            }
            webClientMap.remove(myProxy);
        }




    }

    public static WebClient getWebClient() {
        //get proxy from head of queue and put it on tail of queue
        MyProxy myProxy = proxyQueue.poll();
        proxyQueue.add(myProxy);

        //Checking if webClient with specified proxy exists in map and return it if it does
        if (webClientMap.containsKey(myProxy))
            return webClientMap.get(myProxy);

        //Otherwise create new webClient and put it in Map
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX, myProxy.getIp(),myProxy.getPort());

        //configuring new WebClient
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        //put new webClient in cache
        webClientMap.put(myProxy,webClient);

        return webClient;
    }

    private static void readProxyFile() {
        try {
            URL res = WebClientFactory.class.getClassLoader().getResource(PROXY_LIST_NAME);
            Path path = Paths.get(res.toURI()).toAbsolutePath();
            for (String proxyString : Files.readAllLines(path)) {
                proxyQueue.add(new MyProxy(proxyString.split(":")[0], proxyString.split(":")[1]));
            }
        } catch (Exception e) {
            System.out.println("Error while loading proxy list from file");
            e.printStackTrace();
        }

    }

}

