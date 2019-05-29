package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class WasAssociatedWith {
    private Activity activity;
    private Agent agent;

    public WasAssociatedWith(Activity activity, Agent agent) {
        this.activity = activity;
        this.agent = agent;
    }

    public Activity getActivity() {
        return activity;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }
}
