package ru.kamuzta.partnerscollector.model;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import ru.kamuzta.partnerscollector.entities.*;
import java.util.*;

public class OfficeBurgStrategy implements Strategy {
    private static final String URL = "https://office-burg.ru";
    private static final String URL_AUTH = "https://office-burg.ru/auth/";
    private static final String URL_CONTACTS = "https://office-burg.ru/info/moskva/%s/contacts/";
    private static final String USER_LOGIN = "";
    private static final String USER_PASSWORD = "";

    private WebClient webClient;

    @Override
    public List<Partner> getPartners() {
        try {
            webClient = HtmlUnit.getWebClient();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Partner> partnerList = new ArrayList<>();
        HtmlPage htmlPage = getPage(URL, 3000);
        try {
            HtmlDivision htmlDivision = (HtmlDivision) htmlPage.getByXPath("//div[@class='CitySelectWindow__suppliersWrapper']").get(0);
            List<HtmlElement> partnersHtmlList = htmlDivision.getByXPath("//li[@class='Company js-ds-dealer ']");
            List<GeographyObject> geographyList = getGeographyList(htmlPage);

            for (int i = 0; i < partnersHtmlList.size(); i++) { // здесь можно ставить ограничение по количеству партнеров при тестах
                HtmlElement element = partnersHtmlList.get(i);
                Partner partner = new Partner();
                partner.setSourceSite(URL);
                String dealerId = element.getAttribute("data-dealer");
                partner.setDealerId(dealerId);
                partner.setName(element.getElementsByTagName("strong").get(0).getTextContent());
                partner.setRealAddress(element.getElementsByTagName("span").get(1).getTextContent());
                partner.setWebSite(URL);
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
            e.printStackTrace();
        }

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
        return partnerList;
    }

    //получам залогиненную страничку, если вдруг понадобится
    private HtmlPage getLoginPage(String url, int timeout) {
        HtmlPage page = null;
        HtmlPage postPage = null;
        try {
            webClient = HtmlUnit.getWebClient();
            page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(timeout);
            //находим log-in форму, её поля и кнопку
            HtmlForm form = page.getFormByName("form_auth");
            HtmlTextInput loginField = form.getInputByName("USER_LOGIN");
            HtmlPasswordInput passwordField = form.getInputByName("USER_PASSWORD");
            HtmlButton button = form.getButtonByName("Login");
            //заполняем поля и нажимаем кнопку
            loginField.setValueAttribute(USER_LOGIN);
            webClient.waitForBackgroundJavaScript(timeout);
            passwordField.setValueAttribute(USER_PASSWORD);
            webClient.waitForBackgroundJavaScript(timeout);
            postPage = button.click();
            webClient.waitForBackgroundJavaScript(timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postPage;
    }

    //получаем страничку выбора городов для парсинга базовой информации по городам, областям и партнерам
    private HtmlPage getPage(String url, int timeout) {
        HtmlPage page = null;
        HtmlPage postPage = null;
        try {
            webClient = HtmlUnit.getWebClient();
            page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(timeout);
            HtmlSpan pseudoLink = (HtmlSpan) page.getByXPath("//span[@class='pseudoLink SuggestCity__selectOther js-city-selectOther']").get(0);
            postPage = pseudoLink.click();
            webClient.waitForBackgroundJavaScript(timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postPage;
    }


    //составляем связки между city, region, cityID, regionID в виде объектов и помещаем их в лист.
    private List<GeographyObject> getGeographyList(HtmlPage htmlPage) {
        List<GeographyObject> geographyList = new ArrayList<>();
        try {
            HtmlDivision htmlDivision = (HtmlDivision) htmlPage.getByXPath("//div[@class='CitySelectWindow__item js-levenshtein-clone']").get(0);
            List<HtmlElement> geographyHtmlList = htmlDivision.getByXPath("//li[@class='CitySelectList__item js-ds-city '] | //li[@class='CitySelectList__item js-ds-city CitySelectList__item--main js-ds-city-main']");

            for (HtmlElement element : geographyHtmlList) {
                GeographyObject geographyObject = new GeographyObject();
                geographyObject.setCityId(element.getAttribute("data-city"));
                geographyObject.setRegionId(element.getAttribute("data-region"));
                geographyObject.setCityName(element.getElementsByTagName("a").get(0).getTextContent());
                geographyObject.setRegionName(element.getElementsByTagName("span").get(0).getTextContent());
                geographyList.add(geographyObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geographyList;
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
                    HtmlPage contactsPage = getPage(String.format(URL_CONTACTS, dealerId), 100);

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
                    System.out.println("ADDED #" + partnerPropertiesMap.size() + " [" + dealerId +"] " + " partnerProperty to list: " + partnerProperties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            webClient.close();
        }

        return partnerPropertiesMap;
    }
}
