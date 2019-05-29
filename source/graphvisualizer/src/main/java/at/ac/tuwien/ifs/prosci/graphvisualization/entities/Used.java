package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class Used {
    private Entity entity;
    private Activity activity;

    public Used(Entity entity, Activity activity) {
        this.entity = entity;
        this.activity = activity;
    }


    public Entity getEntity() {
        return entity;
    }

    public Activity getActivity() {
        return activity;
    }

}
