package ru.kamuzta.partnerscollector.model;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import ru.kamuzta.partnerscollector.entities.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class AnonymousWebStrategy implements Strategy {
    private WebSite webSite;
    private List<Partner> partnerList;
    private List<GeographyObject> geographyList;
    private Map<String, List<String>> partnerPropertiesMap;
    private Queue<String> idsQueue;

    public AnonymousWebStrategy(WebSite webSite) {
        this.webSite = webSite;
        this.partnerPropertiesMap = new ConcurrentHashMap<>();
        System.out.println("AnonymousWebStrategy initialized successfully: " + webSite.name());
    }

    //получаем первую страничку для парсинга
    private HtmlPage getFirstPage(WebClient webClient, String url, int timeout) {
        HtmlPage postPage = null;
        try {
            HtmlPage page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(timeout);
            HtmlSpan pseudoLink = (HtmlSpan) page.getByXPath("//span[@class='pseudoLink SuggestCity__selectOther js-city-selectOther']").get(0);
            postPage = pseudoLink.click();
            webClient.waitForBackgroundJavaScript(timeout);
            webClient.close();
        } catch (Exception ioException) {
            WebClientFactory.removeWebClient(webClient);
        }

        return postPage;
    }

    @Override
    public List<Partner> getPartners() {
        while (true) {
            WebClient webClient = WebClientFactory.getWebClient();
            HtmlPage htmlPage = getFirstPage(webClient, webSite.getUrl(), 3000);
            //если успешно получили страницу
            if (htmlPage != null) {
                //Получает лист с географичискими данными
                geographyList = obtainGeographyList(htmlPage);
                //а также производит первичное заполнение карточек партнеров
                partnerList = obtainStartedPartnerList(htmlPage);
                break;
            } else {
                System.out.println("Failed to get first page of " + webSite.name() + " retrying");
                WebClientFactory.removeWebClient(webClient);
            }
        }

        Set<String> setOfDealerIds = new HashSet<>();
        for (Partner partner : partnerList) {
            setOfDealerIds.add(partner.getDealerId());
        }
        idsQueue = new ConcurrentLinkedQueue<>(setOfDealerIds);



        while (setOfDealerIds.size() != partnerPropertiesMap.size()) {
            System.out.println("Waiting for threads of " + webSite.name() + " work completion: " + partnerPropertiesMap.size() + "/" + setOfDealerIds.size());
            if (!idsQueue.isEmpty()) {
                new Thread(new GetPartnerPropertiesTask(idsQueue.poll())).start();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //дозаполняем карты партнеров данными из partnerPropertiesMap
        fillPartnerCardsWithProperties();
        return partnerList;
    }

    private List<GeographyObject> obtainGeographyList(HtmlPage htmlPage) {
        List<GeographyObject> geoList = new CopyOnWriteArrayList<>();

        //составляем связки между city, region, cityID, regionID в виде объектов и помещаем их в лист.
        HtmlDivision htmlGeoDivision = (HtmlDivision) htmlPage.getByXPath("//div[@class='CitySelectWindow__item js-levenshtein-clone']").get(0);
        List<HtmlElement> geographyHtmlList = htmlGeoDivision.getByXPath("//li[@class='CitySelectList__item js-ds-city '] | //li[@class='CitySelectList__item js-ds-city CitySelectList__item--main js-ds-city-main']");

        for (HtmlElement element : geographyHtmlList) {
            GeographyObject geographyObject = new GeographyObject();
            geographyObject.setCityId(element.getAttribute("data-city"));
            geographyObject.setRegionId(element.getAttribute("data-region"));
            geographyObject.setCityName(element.getElementsByTagName("a").get(0).getTextContent());
            geographyObject.setRegionName(element.getElementsByTagName("span").get(0).getTextContent());
            geoList.add(geographyObject);
        }
        return geoList;
    }

    private List<Partner> obtainStartedPartnerList(HtmlPage htmlPage) {
        List<Partner> startedPartnerList = new CopyOnWriteArrayList<>();

        //помещаем html-блоки по партнерам в лист и производим первичное заполнение карточек
        HtmlDivision htmlPartnersDivision = (HtmlDivision) htmlPage.getByXPath("//div[@class='CitySelectWindow__suppliersWrapper']").get(0);
        List<HtmlElement> partnersHtmlList = htmlPartnersDivision.getByXPath("//li[@class='Company js-ds-dealer ']");

        for (int i = 0; i < partnersHtmlList.size(); i++) { //  здесь можно ставить ограничение по количеству партнеров при тестах
            HtmlElement element = partnersHtmlList.get(i);
            Partner partner = new Partner();
            partner.setSourceSite(webSite.getUrl());
            partner.setDealerId(element.getAttribute("data-dealer"));
            partner.setName(element.getElementsByTagName("strong").get(0).getTextContent());
            partner.setRealAddress(element.getElementsByTagName("span").get(1).getTextContent());
            partner.setWebSite(webSite.getUrl());
            partner.setPhoneNumbers(element.getElementsByTagName("span").get(0).getTextContent());

            //заполняем географические данные партнера по его cityId
            String cityId = element.getAttribute("data-city");
            partner.setCityId(cityId);
            for (GeographyObject geographyObject : geographyList) {
                if (geographyObject.getCityId().equals(cityId)) {
                    partner.setCity(geographyObject.getCityName());
                    partner.setRegionId(geographyObject.getRegionId());
                    partner.setRegion(geographyObject.getRegionName());
                }
            }

            startedPartnerList.add(partner);
            System.out.println("NEW Partner" + partner.getDealerId() + "created from " + webSite.name());
        }

        return startedPartnerList;
    }

    private void fillPartnerCardsWithProperties() {
        //дозаполняем карточки партнеров результатами индивидуального парсинга по dealerId
        for (Partner partner : partnerList) {
            List<String> partnerPropertiesList = partnerPropertiesMap.get(partner.getDealerId());
            if (partnerPropertiesList != null && partnerPropertiesList.size() > 1) {
                partner.setPhoneNumbers(partnerPropertiesList.get(0));
                partner.setMails(partnerPropertiesList.get(1));
                partner.setWorkingHours(partnerPropertiesList.get(2));
                partner.setLegalName(partnerPropertiesList.get(3));
                partner.setLegalAddress(partnerPropertiesList.get(4));
                partner.setPsrn(partnerPropertiesList.get(5));
                partner.setShopsCount(partnerPropertiesList.get(6));
            }
        }
    }

    class GetPartnerPropertiesTask implements Runnable {
        String dealerId;
        List<String> partnerProperties;
        WebClient webClient;

        GetPartnerPropertiesTask(String dealerId) {
            this.dealerId = dealerId;
            this.partnerProperties = new CopyOnWriteArrayList<>();
            this.webClient = WebClientFactory.getWebClient();
            System.out.println("Task for dealerId " + dealerId + " created");
        }

        @Override
        public void run() {
            try {
                HtmlPage contactsPage = getPage(webClient, String.format(webSite.getUrlContacts(), dealerId), 500); //timeout
                HtmlDivision contactsDivision = (HtmlDivision) contactsPage.getByXPath("//div[@class='Contact__group']").get(0);
                List<HtmlDivision> contactsDivisionList = contactsDivision.getElementsByAttribute("div", "class", "ContactFeature__value");
                if (contactsDivisionList.size() != 0) {
                    partnerProperties.add(contactsDivisionList.get(0).getTextContent());
                    partnerProperties.add(contactsDivisionList.get(2).getTextContent());
                    partnerProperties.add(contactsDivisionList.get(3).getTextContent());
                    partnerProperties.add(contactsDivisionList.get(4).getTextContent());
                    partnerProperties.add(contactsDivisionList.get(5).getTextContent());
                    partnerProperties.add(contactsDivisionList.get(6).getTextContent());
                }

                List<HtmlDivision> shopsDivision = contactsPage.getByXPath("//div[@class='TabsContent']");
                if (shopsDivision.size() != 0) {
                    List<HtmlDivision> shopsDivisionList = shopsDivision.get(0).getElementsByAttribute("div", "class", "StoresAddresses__box StoreAddress StoreAddress--default Collapsible Collapsible--default");
                    partnerProperties.add(String.valueOf(shopsDivisionList.size()));
                } else {
                    partnerProperties.add("0");
                }

                partnerPropertiesMap.put(dealerId, partnerProperties);
                System.out.println("ADDED #" + partnerPropertiesMap.size() + " [" + dealerId + "] " + " partnerProperty to list: " + partnerProperties);
            } catch (Exception e) {
                System.out.println("Got exception while doing task for dealerId " + dealerId);
                idsQueue.add(dealerId);
                WebClientFactory.removeWebClient(webClient);
            }
        }

        //получаем страничку с контактными данными партнера для парсинга
        private HtmlPage getPage(WebClient webClient, String url, int timeout) throws Exception {
            HtmlPage page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(timeout);
            webClient.close();
            return page;
        }
    }


}
