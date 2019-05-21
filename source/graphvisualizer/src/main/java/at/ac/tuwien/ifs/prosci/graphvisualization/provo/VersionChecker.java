package at.ac.tuwien.ifs.prosci.graphvisualization.provo;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.ProsciProperties;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class VersionChecker {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ResourceBundle path_mapping;
    @Autowired
    private ProsciProperties prosciProperties;

    public Git getGit(String location) throws IOException {
        Git git = Git.open(new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString(location)));
        return git;
    }

    public List<DiffEntry> getRevision(String currentCommitId, String lastCommitId) throws  IOException, GitAPIException {
        Git git = getGit("input");
        ObjectId oldHead = git.getRepository().resolve(lastCommitId);
        ObjectId head = git.getRepository().resolve(currentCommitId);
        RevWalk revWalk = new RevWalk(git.getRepository());
        oldHead = ObjectId.fromString(revWalk.parseCommit(oldHead).getTree().getName());
        head = ObjectId.fromString(revWalk.parseCommit(head).getTree().getName());
        revWalk.close();

        List<DiffEntry> diffs;
        // prepare the two iterators to compute the diff between
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);

            // finally get the list of changed files

            diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();



        }


        return diffs;

    }


    public List<String> getFiles(String commitID) throws IOException {
        List<String> pathList = new ArrayList<>();
        RevCommit commit = getCommit(commitID);

        TreeWalk treeWalk = new TreeWalk(getGit("input").getRepository());
        treeWalk.reset(commit.getTree().getId());
        while (treeWalk.next()) {
            String path = treeWalk.getPathString();
            pathList.add(path);
        }
        treeWalk.close();
        return pathList;
    }

    public void getFile(String commitID,String id, String fileName) throws IOException {
        RevCommit commit = getCommit(commitID);

        TreeWalk treeWalk = new TreeWalk(getGit("input").getRepository());
        treeWalk.addTree(commit.getTree().getId());
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(id));

        if(!treeWalk.next()){
            LOGGER.error("Can not find file with given name: "+id);
        }
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = getGit("input").getRepository().open(objectId);


        String dest=prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.version")+fileName;
        OutputStream out = new FileOutputStream(dest);
        loader.copyTo(out);
        out.close();
    }

    public Date getCommitTime(String commitID) throws IOException {
        return getCommit(commitID).getAuthorIdent().getWhen();

    }

    private RevCommit getCommit(String commitID) throws IOException {
        RevWalk revWalk = new RevWalk(getGit("input").getRepository());
        RevCommit commit = revWalk.parseCommit(ObjectId.fromString(commitID));
        revWalk.close();
        return commit;
    }

    public ArrayList<RevCommit> reverseIterator() throws IOException, GitAPIException {

        Iterator<RevCommit> revCommitIterator = getGit("input").log().call().iterator();

        ArrayList<RevCommit> revCommits = new ArrayList<>();
        ArrayList<RevCommit> reversedRevCommits = new ArrayList<>();

        while (revCommitIterator.hasNext()) {
            //last commit in first place
            revCommits.add(revCommitIterator.next());
        }

        for (int i = revCommits.size() - 1; i >= 0; i--) {
            //first commit in first place
            reversedRevCommits.add(revCommits.get(i));
        }
        return reversedRevCommits;
    }

    public int getIndexOfVersionID(String versionID) {
        ArrayList<RevCommit> reversedRevCommits=new ArrayList<>();
        try {
            reversedRevCommits =reverseIterator();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (GitAPIException e) {
            return 0;
        }


        if(versionID==null){
            return reversedRevCommits.size()-1;
        }
        for (int i = 0; i < reversedRevCommits.size(); i++) {
            if (reversedRevCommits.get(i).getName().equals(versionID))
                return i;
        }
        return -1;
    }

}