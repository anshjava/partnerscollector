package ru.kamuzta.partnerscollector.view;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.kamuzta.partnerscollector.Controller;
import ru.kamuzta.partnerscollector.entities.Partner;
import ru.kamuzta.partnerscollector.model.HtmlUnit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HtmlView implements View {
    private Controller controller;
    private WebClient webClient;
    private final String partnersPath = getResultHtmlPath("partners.html");

    @Override
    public void update(List<Partner> partners) {
        for (Partner partner : partners) {
            System.out.println(partner);
        }

        try {
            updateFile(getUpdatedFileContent(partners));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    public String getResultHtmlPath(String fileName) {
        String resultPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + this.getClass().getPackage().getName().replaceAll("[.]", "/");

        //если в конце переданного пути отсутствует слэш - добавляем его
        if (!resultPath.endsWith("\\") && !resultPath.endsWith("/"))
            resultPath = resultPath + "/";

        //если ос windows и путь начинается со слэша - убираем его
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && (resultPath.startsWith("\\") || resultPath.startsWith("/")))
            resultPath = resultPath.substring(1);

        return resultPath + fileName;
    }

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


    private String getUpdatedFileContent(List<Partner> partnerList) {
        HtmlPage page = getPage(partnersPath);

        //клонируем блок с шаблоном и получаем из него шаблон нужной формы
        HtmlElement hiddenTemplateTr = page.getDocumentElement().getElementsByAttribute("tr", "bgcolor", "white").get(0);
        HtmlElement templateTr = (HtmlElement) hiddenTemplateTr.cloneNode(true);
        templateTr.removeAttribute("bgcolor");


        //убираем все действующие записи из page, но оставляем в нем невидимый шаблон
        List<HtmlElement> elements = page.getDocumentElement().getElementsByAttribute("tr", "bgcolor", "yellow");
        for (HtmlElement element : elements) {
            element.remove();
        }

        templateTr.setAttribute("bgcolor", "yellow");

        //готовим к вставке вакансии по шаблону
        for (Partner partner : partnerList) {
            HtmlElement clonedTemplateTr = (HtmlElement) templateTr.cloneNode(true);

            HtmlElement sourceSite = clonedTemplateTr.getElementsByAttribute("td", "class", "sourceSite").get(0);
            sourceSite.setTextContent(partner.getSourceSite());
            HtmlElement dealerId = clonedTemplateTr.getElementsByAttribute("td", "class", "dealerId").get(0);
            dealerId.setTextContent(partner.getDealerId());
            HtmlElement regionId = clonedTemplateTr.getElementsByAttribute("td", "class", "regionId").get(0);
            regionId.setTextContent(partner.getRegionId());
            HtmlElement region = clonedTemplateTr.getElementsByAttribute("td", "class", "region").get(0);
            region.setTextContent(partner.getRegion());
            HtmlElement cityId = clonedTemplateTr.getElementsByAttribute("td", "class", "cityId").get(0);
            cityId.setTextContent(partner.getCityId());
            HtmlElement city = clonedTemplateTr.getElementsByAttribute("td", "class", "city").get(0);
            city.setTextContent(partner.getCity());
            HtmlElement legalName = clonedTemplateTr.getElementsByAttribute("td", "class", "legalName").get(0);
            legalName.setTextContent(partner.getLegalName());
            HtmlElement name = clonedTemplateTr.getElementsByAttribute("td", "class", "name").get(0);
            name.setTextContent(partner.getName());
            HtmlElement psrn = clonedTemplateTr.getElementsByAttribute("td", "class", "psrn").get(0);
            psrn.setTextContent(partner.getPsrn());
            HtmlElement legalAddress = clonedTemplateTr.getElementsByAttribute("td", "class", "legalAddress").get(0);
            legalAddress.setTextContent(partner.getLegalAddress());
            HtmlElement realAddress = clonedTemplateTr.getElementsByAttribute("td", "class", "realAddress").get(0);
            realAddress.setTextContent(partner.getRealAddress());
            HtmlElement webSite = clonedTemplateTr.getElementsByAttribute("td", "class", "webSite").get(0);
            webSite.setTextContent(partner.getWebSite());
            HtmlElement mails = clonedTemplateTr.getElementsByAttribute("td", "class", "mails").get(0);
            mails.setTextContent(partner.getMails());
            HtmlElement phoneNumbers = clonedTemplateTr.getElementsByAttribute("td", "class", "phoneNumbers").get(0);
            phoneNumbers.setTextContent(partner.getPhoneNumbers());
            HtmlElement workingHours = clonedTemplateTr.getElementsByAttribute("td", "class", "workingHours").get(0);
            workingHours.setTextContent(partner.getWorkingHours());
            HtmlElement shopCount = clonedTemplateTr.getElementsByAttribute("td", "class", "shopCount").get(0);
            shopCount.setTextContent(partner.getShopsCount());
            hiddenTemplateTr.insertBefore(clonedTemplateTr);
        }

        return page.asXml();
    }

    private void updateFile(String text) {
        try (FileWriter fileWriter = new FileWriter(new File(partnersPath))) {
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadPartnerTable() {
        controller.reloadPartners();
    }
}
