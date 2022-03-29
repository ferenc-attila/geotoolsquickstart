package org.geotools.tutorial.quickstart;

public class Location {

    private Long id;
    private double latitude;
    private double longitude;
    private String name;
    private int population;

    public Location(double latitude, double longitude, String name, int population) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.population = population;
    }

    public Location(Long id, double latitude, double longitude, String name, int population) {
        this(latitude, longitude, name, population);
        this.id = id;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", population=" + population +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
}
