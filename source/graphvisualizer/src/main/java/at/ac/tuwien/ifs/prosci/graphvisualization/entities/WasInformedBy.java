package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

public class WasInformedBy {
    private Activity informed;
    private Activity informant;

    public WasInformedBy(Activity informed, Activity informant) {
        this.informed = informed;
        this.informant = informant;
    }

    public Activity getInformed() {
        return informed;
    }

    public Activity getInformant() {
        return informant;
    }
}
