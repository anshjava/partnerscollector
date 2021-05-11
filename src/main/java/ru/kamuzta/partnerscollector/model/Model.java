package ru.kamuzta.partnerscollector.model;

import ru.kamuzta.partnerscollector.entities.*;
import ru.kamuzta.partnerscollector.view.*;
import java.util.*;

public class Model {
    private View view;
    private Provider[] providers;

    public Model(View view, Provider... providers) {
        if (view == null || providers == null || providers.length == 0) {
            throw new IllegalArgumentException();
        }
        this.view = view;
        this.providers = providers;
    }

    public void reloadPartners() {
        List<Partner> partnerList = new ArrayList<>();
        for (Provider provider : providers) {
            partnerList.addAll(provider.getPartners());
        }
        view.update(partnerList);
    }
}
