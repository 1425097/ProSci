package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class WasGeneratedBy {
    private Entity entity;
    private Activity activity;

    public WasGeneratedBy(Entity entity, Activity activity) {
        this.entity = entity;
        this.activity = activity;
    }

    public WasGeneratedBy() {
    }

    public Entity getEntity() {
        return entity;
    }


    public Activity getActivity() {
        return activity;
    }

}
