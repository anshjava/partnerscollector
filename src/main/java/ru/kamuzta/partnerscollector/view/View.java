package ru.kamuzta.partnerscollector.view;

import ru.kamuzta.partnerscollector.Controller;
import ru.kamuzta.partnerscollector.entities.Partner;

import java.util.List;

public interface View {
    void update(List<Partner> partners);
    void setController(Controller controller);
}
