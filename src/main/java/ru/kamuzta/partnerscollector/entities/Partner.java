package ru.kamuzta.partnerscollector.entities;

public class Partner {
    private String sourceSite;
    private String dealerId;
    private String regionId;
    private String region;
    private String cityId;
    private String city;
    private String legalName;
    private String name;
    private String psrn;
    private String legalAddress;
    private String realAddress;
    private String webSite;
    private String mails;
    private String phoneNumbers;
    private String workingHours;
    private String shopsCount;

    @Override
    public String toString() {
        return "Сайт-источник: " + sourceSite + " | " +
                "Дилер ID: " + dealerId + " | " +
                "Регион ID: " + regionId + " | " +
                "Регион: " + region + " | " +
                "Город ID: " + cityId + " | " +
                "Город: " + city + " | " +
                "ЮЛ: " + legalName + " | " +
                "Название: " + name + " | " +
                "ОГРН: " + psrn + " | " +
                "Юр.адрес: " + legalAddress + " | " +
                "Факт.адрес: " + realAddress + " | " +
                "Веб-сайт: " + webSite + " | " +
                "E-mail: " + mails + " | " +
                "Телефоны: " + phoneNumbers + " | " +
                "Рабочие часы: " + workingHours + " | " +
                "Кол-во магазинов: " + shopsCount;
    }

    public String getSourceSite() {
        return sourceSite;
    }

    public void setSourceSite(String sourceSite) {
        this.sourceSite = sourceSite;
    }

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPsrn() {
        return psrn;
    }

    public void setPsrn(String psrn) {
        this.psrn = psrn;
    }

    public String getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(String legalAddress) {
        this.legalAddress = legalAddress;
    }

    public String getRealAddress() {
        return realAddress;
    }

    public void setRealAddress(String realAddress) {
        this.realAddress = realAddress;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public String getMails() {
        return mails;
    }

    public void setMails(String mails) {
        this.mails = mails;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getShopsCount() {
        return shopsCount;
    }

    public void setShopsCount(String shopsCount) {
        this.shopsCount = shopsCount;
    }
}
