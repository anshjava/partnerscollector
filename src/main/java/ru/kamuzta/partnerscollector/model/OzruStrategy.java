package ru.kamuzta.partnerscollector.model;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import ru.kamuzta.partnerscollector.entities.*;
import java.util.*;

public class OzruStrategy implements Strategy {
    private final String FILE_PATH = getHtmlPath("ozru_partners.html");
    private static final String URL_CONTACTS = "https://ozru.ru/info/moskva/%s/contacts/";

    private WebClient webClient;

    @Override
    public List<Partner> getPartners() {
        try {
            webClient = HtmlUnit.getWebClient();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Partner> partnerList = new ArrayList<>();
        HtmlPage htmlPage = getPage(FILE_PATH);
        try {
            HtmlElement htmlElement = (HtmlElement) htmlPage.getByXPath("//ul[@class='CompaniesList']").get(0);
            List<HtmlElement> partnersHtmlList = htmlElement.getByXPath("//li[@class='Company js-ds-dealer']");

            for (int i = 0; i < 5; i++) { // здесь можно ставить ограничение по количеству партнеров при тестах
                HtmlElement element = partnersHtmlList.get(i);
                Partner partner = new Partner();

                partner.setSourceSite("https://ozru.ru");
                String dealerId = element.getAttribute("data-dealer");
                partner.setDealerId(dealerId);
                partner.setName(element.getElementsByTagName("strong").get(0).getTextContent());
                partner.setRealAddress(element.getElementsByTagName("span").get(1).getTextContent());
                partner.setWebSite("https://ozru.ru");
                partner.setPhoneNumbers(element.getElementsByTagName("span").get(0).getTextContent());

                String cityId = element.getAttribute("data-city");
                partner.setCityId(cityId);

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

    //получаем ИЗ ФАЙЛА страничку выбора городов для парсинга базовой информации по городам, областям и партнерам
    private HtmlPage getPage(String filePath) {
        HtmlPage page = null;
        try {
            webClient = HtmlUnit.getWebClient();
            page = webClient.getPage("file:\\\\" + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return page;
    }

    //получаем страничку выбора городов для парсинга базовой информации по городам, областям и партнерам
    private HtmlPage getPage(String url, int timeout) {
        HtmlPage page = null;
        try {
            webClient = HtmlUnit.getWebClient();
            page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return page;
    }

    //получаем полный путь до локального файла, который будем парсить
    public String getHtmlPath(String fileName) {
        String resultPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + this.getClass().getPackage().getName().replaceAll("[.]", "/");

        //если в конце переданного пути отсутствует слэш - добавляем его
        if (!resultPath.endsWith("\\") && !resultPath.endsWith("/"))
            resultPath = resultPath + "/";

        //если ос windows и путь начинается со слэша - убираем его
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && (resultPath.startsWith("\\") || resultPath.startsWith("/")))
            resultPath = resultPath.substring(1);

        return resultPath + fileName;
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
                    HtmlPage contactsPage = getPage(String.format(URL_CONTACTS, dealerId), 2000);

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
