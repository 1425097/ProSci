package at.ac.tuwien.ifs.prosci.provstarter.command;

import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import org.springframework.beans.factory.annotation.Autowired;

public class Show {
    @Autowired
    private ProsciProperties prosciProperties;

    public void showWorkspaces()  {

        boolean hasWorkspace=false;

        for (Object key: prosciProperties.getProps().keySet()){
            if(key.toString().equals("workspace.current")){
                hasWorkspace=true;
                System.out.println("Current workspace: "+ prosciProperties.readProperties(key.toString()));
            }
        }
        for (Object key: prosciProperties.getProps().keySet()){
            if(! key.toString().equals("workspace.current")){
                hasWorkspace=true;
                System.out.println("Workspace: "+ key+", Path: "+prosciProperties.readProperties(key.toString()));
            }
        }
        if(!hasWorkspace){
            System.out.println("no workspaces.");
        }
    }
}
