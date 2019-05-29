package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class WasRevisonOf {
    private Entity generatedEntity;
    private Entity usedEntity;

    public WasRevisonOf(Entity generatedEntity, Entity usedEntity) {
        this.generatedEntity = generatedEntity;
        this.usedEntity = usedEntity;
    }

    public Entity getGeneratedEntity() {
        return generatedEntity;
    }

    public Entity getUsedEntity() {
        return usedEntity;
    }

}
