
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

//~--- non-JDK imports --------------------------------------------------------
import br.uff.ic.dyevc.application.branchhistory.model.BranchRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.LineRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.RevisionsBucket;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedDirectory;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedFile;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItemsBucket;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.application.branchhistory.model.constant.Constant;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;

import org.apache.commons.io.FileUtils;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jgit.api.ResetCommand;

/**
 *
 * @author wallace
 */
public class ProjectService {

    private String BRANCHES_HISTORY_PATH = System.getProperty("user.home") + "/.dyevc/BRANCHES_HISTORY/";

    /**
     * Method description
     *
     * @param path
     * @param projectName
     * @return
     * @throws Exception
     */
    public ProjectRevisions getProjectRevisions(MonitoredRepository monitoredRepository) throws Exception {
        RevisionsBucket revisionsBucket = new RevisionsBucket();

        File file = new File(BRANCHES_HISTORY_PATH + monitoredRepository.getName());
        FileUtils.deleteDirectory(file);



        createDirectory(monitoredRepository.getName());

        FileUtils.copyDirectory(new File(monitoredRepository.getCloneAddress()), new File(BRANCHES_HISTORY_PATH + monitoredRepository.getName()));


        GitConnector gitConnector = new GitConnector(BRANCHES_HISTORY_PATH + monitoredRepository.getName(), monitoredRepository.getName());
        GitCommitTools gitCommitHistory = GitCommitTools.getInstance(monitoredRepository);

        Git git = new Git(gitConnector.getRepository());

        List<Ref> branchRefList = git.branchList().call();

        List<BranchRevisions> branches = new LinkedList<BranchRevisions>();

        ProjectRevisions project = new ProjectRevisions(monitoredRepository.getName());
        //System.out.println("BRANCHES: " + branchRefList.size());

        for (Ref branchRef : branchRefList) {
            Revision rev = revisionsBucket.getRevisionById(branchRef.getObjectId().getName());
            if (rev == null) {
                rev = new Revision(branchRef.getObjectId().getName(), project);
                revisionsBucket.addRevision(rev);
            }

            BranchRevisions branch = new BranchRevisions(branchRef.getName(), rev);
            branches.add(branch);
        }

        Collection<CommitRelationship> commitRelationships = gitCommitHistory.getCommitRelationships();
        Iterator<CommitRelationship> commitRelationshipIt = commitRelationships.iterator();

        //adicionando as relações de revisão pai e filho
        while (commitRelationshipIt.hasNext()) {
            CommitRelationship commitRelationship = commitRelationshipIt.next();
            String child = commitRelationship.getChild().getHash();
            String parent = commitRelationship.getParent().getHash();
            Revision revisionChild = revisionsBucket.getRevisionById(child);
            if (revisionChild == null) {
                revisionChild = new Revision(child, project);
                revisionsBucket.addRevision(revisionChild);
            }

            Revision revisionParent = revisionsBucket.getRevisionById(parent);
            if (revisionParent == null) {
                revisionParent = new Revision(parent, project);
                revisionsBucket.addRevision(revisionParent);
            }

            revisionChild.addPrev(revisionParent);
            revisionParent.addNext(revisionChild);


            /*System.out.println(commitRelationship.getChild().getHash() + "  --> "
                    + commitRelationship.getParent().getHash());*/
        }

        for (BranchRevisions branch : branches) {

            setHistoryOfBranch(branch);
        }

        project.setBranchesRevisions(branches);
        project.setRevisionsBucket(revisionsBucket);
        Iterator<Revision> it = revisionsBucket.getRevisionCollection().iterator();
        while (it.hasNext()) {
            Revision rev = it.next();
            if (rev.getPrev().isEmpty()) {
                project.addRoot(rev);

                break;
            }
        }



        return project;
    }

    /**
     * Method description
     *
     * @param projectRevisions
     * @return
     */
    //returns the versioned project
    public VersionedProject getVersionedProject(ProjectRevisions projectRevisions) {
        VersionedProject versionedProject = new VersionedProject(projectRevisions.getName(),
                projectRevisions.getName() + "/");

        VersionedItemsBucket versionedItemsBucket = new VersionedItemsBucket();
        versionedItemsBucket.addVersionedItem(versionedProject);

        RevisionsBucket revisionsBucket = projectRevisions.getRevisionsBucket();

        Collection<Revision> collection = revisionsBucket.getRevisionCollection();
        //int numberOfRevisions = collection.size();
        //int i = 0;
        Iterator<Revision> it = collection.iterator();
        while (it.hasNext()) {
            try {
                GitConnector gitConnector = new GitConnector(BRANCHES_HISTORY_PATH + versionedProject.getRelativePath(),
                        projectRevisions.getName());
                Git git = new Git(gitConnector.getRepository());
                CheckoutCommand checkoutCommand = null;    // git.checkout();
                Revision revision = it.next();
                ResetCommand resetCommand = git.reset();
                resetCommand.setMode(ResetCommand.ResetType.HARD);
                resetCommand.call();
                checkoutCommand = git.checkout();
                checkoutCommand.setName(revision.getId());

                checkoutCommand.call();

                File file = new File(BRANCHES_HISTORY_PATH + versionedProject.getRelativePath());
                getVersionedItems(versionedItemsBucket, file, revision, versionedProject, versionedProject);
                //i++;
                //System.out.println("PORCENTAGEM DE ITEMS VERSIONADOS CALCULADOS: "+((((double) i)/numberOfRevisions)*100)+" %");
                            
            } catch (Exception e) {
                System.out.println("Erro ProjectService.getVersionedProject: " + e.getMessage());
            }

        }


        return versionedProject;
    }

    private void setHistoryOfBranch(BranchRevisions branch) {
        //pega todos os pais do head
        List<Revision> parents = branch.getHead().getPrev();
        //cria uma linha de revisões
        LineRevisions line = new LineRevisions(branch.getHead());
        while (!parents.isEmpty()) {

            //adiciona o primeiro pai do head
            line.addRevision(parents.get(0));

            //para cada um dos outros pais do cabeça cria outras linhas
            for (int i = 1; i < parents.size(); i++) {

                Revision revision = parents.get(i);
                //verifica se há alguma outra linha com essa head
                if (!branch.haveLineRevisionByHeadId(revision.getId())) {
                    //se não houver cria-se uma linha com esta head
                    LineRevisions newLine = new LineRevisions(revision);
                    getParents(branch, newLine);
                }

            }

            parents = parents.get(0).getPrev();

        }

        branch.addLineRevisions(line);

        //System.out.println("TERMINOU LINHAS: " + branch.getLinesRevisions().size());


    }

    private void getParents(BranchRevisions branch, LineRevisions line) {
        List<Revision> parents = line.getHead().getPrev();
        while (!parents.isEmpty()) {

            line.addRevision(parents.get(0));

            for (int i = 1; i < parents.size(); i++) {
                Revision revision = parents.get(i);
                if (!branch.haveLineRevisionByHeadId(revision.getId())) {
                    LineRevisions newLine = new LineRevisions(revision);
                    getParents(branch, newLine);
                }

            }

            parents = parents.get(0).getPrev();
        }

        branch.addLineRevisions(line);
    }

    private void createDirectory(String name) {
        File file = new File(BRANCHES_HISTORY_PATH + name);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void getVersionedItems(VersionedItemsBucket versionedItemsBucket, File file, Revision revision, VersionedProject versionedProject, VersionedItem antVersionedItem) {
        if (!file.getName().startsWith(".")) {

            if (file.isFile()) {
                VersionedItem versionedItem = versionedItemsBucket.getVersionedItemByRelativePath(file.getAbsolutePath().substring(BRANCHES_HISTORY_PATH.length()));
                if (versionedItem == null) {
                    versionedItem = new VersionedFile(file.getName(), file.getAbsolutePath().substring(BRANCHES_HISTORY_PATH.length()), versionedProject);
                    versionedItemsBucket.addVersionedItem(versionedItem);
                    if (antVersionedItem.getType() == Constant.PROJECT) {
                        ((VersionedProject) antVersionedItem).addVersionedItem(versionedItem);
                    } else if (antVersionedItem.getType() == Constant.DIRECTORY) {
                        ((VersionedDirectory) antVersionedItem).addVersionedItem(versionedItem);
                    }
                }

                versionedItem.addRevison(revision);
            } else {
                File files[] = file.listFiles();
                VersionedItem versionedItem = versionedItemsBucket.getVersionedItemByRelativePath(file.getAbsolutePath().substring(BRANCHES_HISTORY_PATH.length()));
                if (versionedItem == null) {
                    versionedItem = new VersionedDirectory(file.getName(), file.getAbsolutePath().substring(BRANCHES_HISTORY_PATH.length()), versionedProject);
                    versionedItemsBucket.addVersionedItem(versionedItem);
                    if (antVersionedItem.getType() == Constant.PROJECT) {
                        ((VersionedProject) antVersionedItem).addVersionedItem(versionedItem);
                    } else if (antVersionedItem.getType() == Constant.DIRECTORY) {
                        ((VersionedDirectory) antVersionedItem).addVersionedItem(versionedItem);
                    }
                }

                versionedItem.addRevison(revision);

                for (int i = 0; i < files.length; i++) {
                    File file1 = files[i];
                    getVersionedItems(versionedItemsBucket, file1, revision, versionedProject, versionedItem);
                }
            }
        }
    }


}
