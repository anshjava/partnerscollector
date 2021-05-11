package ru.kamuzta.partnerscollector.model;

import ru.kamuzta.partnerscollector.entities.Partner;

import java.util.List;

public class Provider {
    private Strategy strategy;

    public Provider(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public List<Partner> getPartners() {
        return strategy.getPartners();
    }
}
