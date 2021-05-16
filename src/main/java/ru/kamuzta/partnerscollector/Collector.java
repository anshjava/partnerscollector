package ru.kamuzta.partnerscollector;

import ru.kamuzta.partnerscollector.entities.WebSite;
import ru.kamuzta.partnerscollector.model.*;
import ru.kamuzta.partnerscollector.view.*;

public class Collector {
    public static void main(String[] args) {
        HtmlView view = new HtmlView();
        Model model = new Model(view, new Provider(new AnonymousWebStrategy(WebSite.BUSINESSMENU)),
                new Provider(new AnonymousWebStrategy(WebSite.GVARDIA)),
                new Provider(new AnonymousWebStrategy(WebSite.OFFICEBURG)),
                new Provider(new AnonymousWebStrategy(WebSite.OFFICEPLANET)));
        Controller controller = new Controller(model);
        view.setController(controller);

        view.reloadPartnerTable();
    }
}
