package ru.kamuzta.partnerscollector;

import ru.kamuzta.partnerscollector.model.*;

public class Controller {
    private Model model;

    public Controller(Model model) {
        if (model == null) {
            throw new IllegalArgumentException();
        }
        this.model = model;
    }

    public void reloadPartners() {
        model.reloadPartners();
    }
}
