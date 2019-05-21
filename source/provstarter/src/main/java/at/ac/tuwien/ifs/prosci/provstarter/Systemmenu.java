package at.ac.tuwien.ifs.prosci.provstarter;

import at.ac.tuwien.ifs.prosci.provstarter.command.*;
import at.ac.tuwien.ifs.prosci.provstarter.helper.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
public class Systemmenu {

    private final Logger LOGGER = LogManager.getLogger(this.getClass());

    @Autowired
    private Workspace workspace;
    @Autowired
    private Start start;
    @Autowired
    private GraphicStarter graphicStarter;
    @Autowired
    private Save save;
    @Autowired
    private Show show;
    @Autowired
    private FileMonitor fileMonitor;

    public StatusCode option(String argument) {
        LOGGER.debug("receive Command: "+argument);
        try {
            String[] arguments = argument.split(" ");
            StatusCode statusCode=null;
            switch (arguments[0]) {
                case "workspace":
                    if (arguments.length == 3) {
                        statusCode=workspace.initWorkspace(arguments[1], arguments[2]);
                       // fileMonitor.start();
                    } else if (arguments.length == 2) {
                        statusCode=workspace.initWorkspace(arguments[1], null);
                        //fileMonitor.start();
                    } else {
                        System.out.println("incorrect arguments: workspace [workspace name] [workspace path]");
                        return StatusCode.FAIL_SYSTEM;
                    }
                    break;
                case "start":
                    statusCode=start.startXterm();
                    break;
                case "graph":
                    statusCode=graphicStarter.start();
                    break;
                case "save":
                    statusCode=save.save();
                    break;
                case "show":
                    show.showWorkspaces();
                    break;
                case "help":
                    System.out.println("Usage：");
                    System.out.println("Initialize or change workspace");
                    System.out.println("workspace [workspacename] [path(option)]\n");
                    System.out.println("start tracing workspace");
                    System.out.println("start\n");
                    System.out.println("start graphic visualization for current workspace");
                    System.out.println("graph\n");
                    System.out.println("save files in current workspace");
                    System.out.println("save\n");
                    System.out.println("show existing workspaces");
                    System.out.println("show workspace\n");
                    break;
                default:
                    System.out.println("Can't find the command!");
                    System.out.println("get help with following command: ");
                    System.out.println("help");
                    break;
            }
            return statusCode;
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.error("Can't run the command.");
            return StatusCode.FAIL_SYSTEM;
        }
    }

}
