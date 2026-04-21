package com.ditix.backend.Structure.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "structures")
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String latitude;
    private String longitude;
    private String ministere;
    private String region;
    private String zone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public String getMinistere() { return ministere; }
    public void setMinistere(String ministere) { this.ministere = ministere; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
}