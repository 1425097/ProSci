package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class Agent implements Ontology {
    private String id;
    private String detail;


    public Agent(String id, String detail) {
        this.id = id;
        this.detail = detail;

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getVersion() {
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }


}
