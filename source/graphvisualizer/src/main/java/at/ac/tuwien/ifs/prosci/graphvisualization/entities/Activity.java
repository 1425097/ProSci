package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

import java.io.File;
import java.util.Date;

public class Activity implements Ontology {
    private String id;
    private String command;
    private String version;
    private Date startTime;
    private Date endTime;
    private File sysInfos;

    public Activity() {
    }

    public Activity(String id, String command, String version, Date startTime, Date endTime, File sysInfos) {
        this.id = id;
        this.command = command;
        this.version = version;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sysInfos = sysInfos;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public File getSysInfos() {
        return sysInfos;
    }

    public void setSysInfos(File sysInfos) {
        this.sysInfos = sysInfos;
    }
}
