package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class WasDerivedFrom {
    private Entity generatedEntity;
    private Entity usedEntity;

    public WasDerivedFrom(Entity generatedEntity, Entity usedEntity) {
        this.generatedEntity = generatedEntity;
        this.usedEntity = usedEntity;
    }

    public WasDerivedFrom() {

    }

    public Entity getGeneratedEntity() {
        return generatedEntity;
    }

    public void setGeneratedEntity(Entity generatedEntity) {
        this.generatedEntity = generatedEntity;
    }

    public Entity getUsedEntity() {
        return usedEntity;
    }

    public void setUsedEntity(Entity usedEntity) {
        this.usedEntity = usedEntity;
    }
}
