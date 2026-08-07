package com.ditix.backend.ProfilUtilisateur.DTO;

import com.ditix.backend.Structure.Model.Structure;

public class StructureMinDTO {
    private Long id;
    private String code;
    private String name;

    public StructureMinDTO(Structure structure) {
        if (structure != null) {
            this.id = structure.getId();
            this.code = structure.getCode();
            this.name = structure.getName();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
