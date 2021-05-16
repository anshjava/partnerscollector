package ru.kamuzta.partnerscollector.entities;

public enum WebSite {
    BUSINESSMENU("https://бизнес-меню.рф",
            "https://бизнес-меню.рф/auth/",
            "https://бизнес-меню.рф/инфо/moskva/%s/контакты/",
            null,
            null),
    GVARDIA("https://gvardia.ru",
            "https://gvardia.ru/auth/",
            "https://gvardia.ru/info/moskva/%s/contacts/",
            null,
            null),
    OFFICEBURG("https://office-burg.ru",
            "https://office-burg.ru/auth/",
            "https://office-burg.ru/info/moskva/%s/contacts/",
            null,
            null),
    OFFICEPLANET("https://www.office-planet.ru",
            "https://www.office-planet.ru/auth/",
            "https://www.office-planet.ru/info/zeya/%s/contacts/",
            null,
            null),
    OZRU("https://ozru.ru",
            "https://ozru.ru/auth/",
            "https://ozru.ru/info/moskva/%s/contacts/",
            null,
            null);

    private String url;
    private String urlAuth;
    private String urlContacts;
    private String userLogin;
    private String userPassword;

    WebSite(String url, String urlAuth, String urlContacts, String userLogin, String userPassword) {
        this.url = url;
        this.urlAuth = urlAuth;
        this.urlContacts = urlContacts;
        this.userLogin = userLogin;
        this.userPassword = userPassword;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlAuth() {
        return urlAuth;
    }

    public String getUrlContacts() {
        return urlContacts;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
