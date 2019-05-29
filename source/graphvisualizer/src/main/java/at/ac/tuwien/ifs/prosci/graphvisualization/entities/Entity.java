package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

import at.ac.tuwien.ifs.prosci.graphvisualization.helper.ProsciProperties;

public class Entity implements Ontology {
    private ProsciProperties prosciProperties;
    private String id;
    private String version;
    private String value;
    private String name;
    private String id_onto;

    public Entity(String id_onto, String id, String name, String version, String value) {
        this.id_onto = id_onto;
        this.id = id;
        this.name = name;
        this.version = version;
        this.value = value;

    }

    public String getId_onto() {
        return id_onto;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getValue() {
        return value;
    }


    public String getName() {
        return name;
    }


}
