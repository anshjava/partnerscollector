package ru.kamuzta.partnerscollector.model;

import ru.kamuzta.partnerscollector.entities.Partner;

import java.util.List;

public interface Strategy {
    List<Partner> getPartners();
}
