package ru.kamuzta.partnerscollector.entities;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() || hashCode() != o.hashCode()) return false;
        GeographyObject that = (GeographyObject) o;
        return Objects.equals(regionId, that.regionId) &&
                Objects.equals(regionName, that.regionName) &&
                Objects.equals(cityId, that.cityId) &&
                Objects.equals(cityName, that.cityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regionId, regionName, cityId, cityName);
    }
}
