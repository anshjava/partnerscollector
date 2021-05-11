package ru.kamuzta.partnerscollector.entities;

//Geography objects helps to link Ids and Names of region and city
public class GeographyObject {
    private String regionId;
    private String regionName;
    private String cityId;
    private String cityName;

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    @Override
    public String toString() {
        return "GeographyObject{" +
                "regionId='" + regionId + '\'' +
                ", regionName='" + regionName + '\'' +
                ", cityId='" + cityId + '\'' +
                ", cityName='" + cityName + '\'' +
                '}';
    }
}
