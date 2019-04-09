package at.ac.tuwien.ifs.prosci.graphvisualization.entities;

import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Entity implements Ontology {
    private ProsciProperties prosciProperties;
    private String id;
    private String version;
    private String value;
    private String name;

    public Entity(String id, String name, String version, String value) {

        this.id = id;
        this.name = name;
        this.version = version;
        this.value = value;

    }

    public ProsciProperties getProsciProperties() {
        return prosciProperties;
    }

    public void setProsciProperties(ProsciProperties prosciProperties) {
        this.prosciProperties = prosciProperties;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void loadFile() throws IOException {
        prosciProperties = new ProsciProperties();
        Git git = Git.open(new File(prosciProperties.readProperties("path.input")));
        ObjectId versionId = git.getRepository().resolve(version);

        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            RevCommit commit = revWalk.parseCommit(versionId);
            // and using commit's tree find the value
            RevTree tree = commit.getTree();
            System.out.println("Having tree: " + tree);

            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(name));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file 'README.md'");
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = git.getRepository().open(objectId);

                // and then one can the loader to read the file
                loader.copyTo(new FileOutputStream(prosciProperties.readProperties("path.prosci.version") + name + "_" + version));
            }

            revWalk.dispose();
        }

    }
}
