package com.ditix.backend.Structure.DTO;

import com.ditix.backend.Structure.Model.Structure;
import java.time.ZonedDateTime;

public class StructureDTO {
    
    // V1 Fields
    private Long id;
    private String name;
    private String latitude;
    private String longitude;
    private String ministere;
    private String region;
    private String zone;

    // V2 Fields
    private String code;
    private Long ministereId;
    private String ministereNom;
    private Double latitudeV2;
    private Double longitudeV2;
    private String departement;
    private String commune;
    private String adresse;
    private String categorie;
    private Boolean actif;
    private ZonedDateTime creeLe;
    private ZonedDateTime modifieLe;

    public StructureDTO(Structure structure) {
        // V1 Mapping
        this.id = structure.getId();
        this.name = structure.getName();
        this.latitude = structure.getLatitude();
        this.longitude = structure.getLongitude();
        this.ministere = structure.getMinistere();
        this.region = structure.getRegion();
        this.zone = structure.getZone();

        // V2 Mapping
        this.code = structure.getCode();
        if (structure.getMinistereV2() != null) {
            this.ministereId = structure.getMinistereV2().getId();
            this.ministereNom = structure.getMinistereV2().getNom();
        }
        this.latitudeV2 = structure.getLatitudeV2();
        this.longitudeV2 = structure.getLongitudeV2();
        this.departement = structure.getDepartement();
        this.commune = structure.getCommune();
        this.adresse = structure.getAdresse();
        this.categorie = structure.getCategorie();
        this.actif = structure.getActif();
        this.creeLe = structure.getCreeLe();
        this.modifieLe = structure.getModifieLe();
    }

    // Getters and Setters
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

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getMinistereId() { return ministereId; }
    public void setMinistereId(Long ministereId) { this.ministereId = ministereId; }

    public String getMinistereNom() { return ministereNom; }
    public void setMinistereNom(String ministereNom) { this.ministereNom = ministereNom; }

    public Double getLatitudeV2() { return latitudeV2; }
    public void setLatitudeV2(Double latitudeV2) { this.latitudeV2 = latitudeV2; }

    public Double getLongitudeV2() { return longitudeV2; }
    public void setLongitudeV2(Double longitudeV2) { this.longitudeV2 = longitudeV2; }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public ZonedDateTime getCreeLe() { return creeLe; }
    public void setCreeLe(ZonedDateTime creeLe) { this.creeLe = creeLe; }

    public ZonedDateTime getModifieLe() { return modifieLe; }
    public void setModifieLe(ZonedDateTime modifieLe) { this.modifieLe = modifieLe; }
}
