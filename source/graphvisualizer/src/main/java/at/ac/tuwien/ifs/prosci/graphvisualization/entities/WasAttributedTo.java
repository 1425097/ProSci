package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class WasAttributedTo {
    private Entity entity;
    private Agent agent;

    public WasAttributedTo(Entity entity, Agent agent) {
        this.entity = entity;
        this.agent = agent;
    }

    public Entity getEntity() {
        return entity;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }
}
