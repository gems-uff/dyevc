package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.VCSException;

import org.apache.commons.io.FileUtils;

import org.eclipse.jgit.lib.StoredConfig;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides several utilities to handle git repositories.
 *
 * @author Cristiano
 */
public final class GitTools {
    /**
     * Copies configuration from a source repository to a working clone and includes a
     * remote configuration pointing to the source repository
     * @param source the repository to copy configuration from
     * @param target the repository to copy configuration to
     * @throws br.uff.ic.dyevc.exception.VCSException
     */
    public static void adjustTargetConfiguration(GitConnector source, GitConnector target) throws VCSException {
        try {
            LoggerFactory.getLogger(GitTools.class).trace("adjustTargetConfiguration -> Entry");
            LoggerFactory.getLogger(GitTools.class).debug("Copying configuration from {} to {}",
                                    source.getRepositoryPath(), target.getRepositoryPath());

            File   targetConfigFile = new File(target.getRepositoryPath());
            String sourceConfigPath = source.getPath() + "/" + GitConnector.CONFIG_FILE;
            FileUtils.copyFileToDirectory(new File(sourceConfigPath), targetConfigFile, false);

            StoredConfig targetConfig = target.getRepository().getConfig();

            targetConfig.setString(GitConnector.DEFAULT_REMOTE, target.getId(), GitConnector.KEY_FETCH,
                                   GitConnector.FETCH_REFS_HEADS + ":" + GitConnector.REFS_REMOTES + target.getId()
                                   + "/*");
            targetConfig.setString(GitConnector.DEFAULT_REMOTE, target.getId(), GitConnector.KEY_URL, source.getPath());
            targetConfig.save();

            LoggerFactory.getLogger(GitTools.class).debug("Finished copying configuration from {} to {}",
                                    source.getRepositoryPath(), target.getRepositoryPath());
            LoggerFactory.getLogger(GitTools.class).trace("adjustTargetConfiguration -> Exit");
        } catch (IOException ex) {
            Logger.getLogger(GitTools.class.getName()).log(Level.SEVERE,
                             "An error ocurred while copying configuration from " + source.getRepositoryPath() + " to "
                             + target.getRepositoryPath() + ".", ex);
        }
    }
}
