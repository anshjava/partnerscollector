package ru.kamuzta.partnerscollector.model;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import ru.kamuzta.partnerscollector.entities.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AnonymousWebStrategy implements Strategy {
    private WebSite webSite;
    private List<Partner> partnerList;
    private List<GeographyObject> geographyList;

    public AnonymousWebStrategy(WebSite webSite) {
        this.webSite = webSite;
        this.partnerList = new CopyOnWriteArrayList<>();
        this.geographyList = new CopyOnWriteArrayList<>();
        System.out.println("AnonymousWebStrategy initialized successfully:" + webSite.name() + " " + partnerList.size() + " " + geographyList.size());
    }

    @Override
    public List<Partner> getPartners() {

        //Первый таск получает лист с географичискими данными а также производит первичное заполнение карточек партнеров
        Thread geoThread = new Thread(new GeographyTask());
        geoThread.start();
        try {
            geoThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return partnerList;
    }

    class GeographyTask implements Runnable {
        WebClient webClient;

        @Override
        public void run() {
            geographyList.clear();

            try {
                webClient = WebClientFactory.getWebClient();
                HtmlPage htmlPage = getFirstPage(webClient, webSite.getUrl(), 3000);

                //составляем связки между city, region, cityID, regionID в виде объектов и помещаем их в лист.

                HtmlDivision htmlGeoDivision = (HtmlDivision) htmlPage.getByXPath("//div[@class='CitySelectWindow__item js-levenshtein-clone']").get(0);
                List<HtmlElement> geographyHtmlList = htmlGeoDivision.getByXPath("//li[@class='CitySelectList__item js-ds-city '] | //li[@class='CitySelectList__item js-ds-city CitySelectList__item--main js-ds-city-main']");

                for (HtmlElement element : geographyHtmlList) {
                    GeographyObject geographyObject = new GeographyObject();
                    geographyObject.setCityId(element.getAttribute("data-city"));
                    geographyObject.setRegionId(element.getAttribute("data-region"));
                    geographyObject.setCityName(element.getElementsByTagName("a").get(0).getTextContent());
                    geographyObject.setRegionName(element.getElementsByTagName("span").get(0).getTextContent());
                    geographyList.add(geographyObject);
                }

                //помещаем html-блоки по партнерам в лист и производим первичное заполнение карточек
                HtmlDivision htmlPartnersDivision = (HtmlDivision) htmlPage.getByXPath("//div[@class='CitySelectWindow__suppliersWrapper']").get(0);
                List<HtmlElement> partnersHtmlList = htmlPartnersDivision.getByXPath("//li[@class='Company js-ds-dealer ']");


                for (int i = 0; i < 3; i++) { // partnersHtmlList.size() здесь можно ставить ограничение по количеству партнеров при тестах
                    HtmlElement element = partnersHtmlList.get(i);
                    Partner partner = new Partner();
                    partner.setSourceSite(webSite.getUrl());
                    String dealerId = element.getAttribute("data-dealer");
                    partner.setDealerId(dealerId);
                    partner.setName(element.getElementsByTagName("strong").get(0).getTextContent());
                    partner.setRealAddress(element.getElementsByTagName("span").get(1).getTextContent());
                    partner.setWebSite(webSite.getUrl());
                    partner.setPhoneNumbers(element.getElementsByTagName("span").get(0).getTextContent());

                    String cityId = element.getAttribute("data-city");
                    partner.setCityId(cityId);
                    for (GeographyObject geographyObject : geographyList) {
                        if (geographyObject.getCityId().equals(cityId)) {
                            partner.setCity(geographyObject.getCityName());
                            partner.setRegionId(geographyObject.getRegionId());
                            partner.setRegion(geographyObject.getRegionName());
                        }
                    }

                    partnerList.add(partner);
                    System.out.println("ADDED #" + partnerList.size() + " partner to list: " + partner.getDealerId());
                }


            } catch (Exception e) {
                WebClientFactory.removeWebClient(webClient);
            }
        }

        //получаем первую страничку для парсинга
        private HtmlPage getFirstPage(WebClient webClient, String url, int timeout) throws Exception {
            HtmlPage page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(timeout);
            HtmlSpan pseudoLink = (HtmlSpan) page.getByXPath("//span[@class='pseudoLink SuggestCity__selectOther js-city-selectOther']").get(0);
            HtmlPage postPage = pseudoLink.click();
            webClient.waitForBackgroundJavaScript(timeout);
            return postPage;
        }

    }

    class GetPartnerPropertiesTask implements Runnable {
        WebClient webClient;

        @Override
        public void run() {
            webClient = WebClientFactory.getWebClient();
            Map<String, List<String>> partnerPropertiesMap = getPartnerPropertiesMap(partnerList);
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

        //получаем страничку с контактными данными партнера для парсинга
        private HtmlPage getPage(WebClient webClient, String url, int timeout) throws Exception {
            HtmlPage page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(timeout);
            return page;
        }

        //получаем мапу, где ключ - id партнера, а значение - набор параметров со страницы контактов партнера
        private Map<String, List<String>> getPartnerPropertiesMap(List<Partner> partnerList) {
            Map<String, List<String>> partnerPropertiesMap = new HashMap<>();
            List<String> partnerProperties;
            for (Partner partner : partnerList) {
                String dealerId = partner.getDealerId();
                if (!partnerPropertiesMap.containsKey(dealerId)) {
                    try {
                        partnerProperties = new ArrayList<>();
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
                        WebClientFactory.removeWebClient(webClient);
                    }
                }
            }

            return partnerPropertiesMap;
        }
    }






}
