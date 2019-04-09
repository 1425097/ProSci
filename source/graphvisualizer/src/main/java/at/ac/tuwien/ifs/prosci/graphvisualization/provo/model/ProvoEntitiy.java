package at.ac.tuwien.ifs.prosci.graphvisualization.provo.model;


import org.openprovenance.prov.model.Entity;

public class ProvoEntitiy {
    private Entity entity;
    private int version;
    private String path;
    private String commitID;

    public ProvoEntitiy(Entity entity, int version, String path, String commitID) {
        this.entity = entity;
        this.version = version;
        this.path = path;
        this.commitID = commitID;
    }


    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCommitID() {
        return commitID;
    }

    public void setCommitID(String commitID) {
        this.commitID = commitID;
    }
}
