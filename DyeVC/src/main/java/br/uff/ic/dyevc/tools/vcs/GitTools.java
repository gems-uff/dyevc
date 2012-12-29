package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.exception.VCSException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.LoggerFactory;

/**
 * This class provides several utilities to handle git repositories.
 *
 * @author Cristiano
 */
public final class GitTools {
    
    /**
     * Copies configuration from a repository to a working clone and includes a
     * remote configuration pointing to the source repository
     * @param source the repository to copy configuration from
     * @param target the repository to copy configuration to
     * @throws IOException
     * @throws ConfigInvalidException
     */
    public static void adjustTargetConfiguration(GitConnector source, GitConnector target) throws VCSException {
        try {
            LoggerFactory.getLogger(GitTools.class).trace("adjustTargetConfiguration -> Entry");
            LoggerFactory.getLogger(GitTools.class).debug("Copying configuration from {} to {}"
                    , source.getRepositoryPath(), target.getRepositoryPath());
            StoredConfig targetConfig = target.getRepository().getConfig();
            Set<String> names = targetConfig.getNames(GitConnector.DEFAULT_REMOTE
                    , GitConnector.DEFAULT_ORIGIN);
            
            HashMap<String, String> keyvalues = new HashMap<String, String>();
            for (Iterator<String> it = names.iterator(); it.hasNext();) {
                String name = it.next();
                keyvalues.put(name, targetConfig.getString(GitConnector.DEFAULT_REMOTE
                        , GitConnector.DEFAULT_ORIGIN, name));
            }

            File targetConfigFile = new File(target.getRepositoryPath());
            String sourceConfigPath = source.getPath()
                    + "/" + GitConnector.CONFIG_FILE;
            FileUtils.copyFileToDirectory(new File(sourceConfigPath), targetConfigFile, true);
            
            targetConfig.load();
            
            for (Map.Entry<String, String> entry : keyvalues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equalsIgnoreCase(GitConnector.COMMAND_FETCH)) {
                    value = value.replace(GitConnector.REFS_REMOTES + GitConnector.DEFAULT_ORIGIN, 
                                             GitConnector.REFS_REMOTES + target.getId());
                }
                targetConfig.setString(GitConnector.DEFAULT_REMOTE, target.getId(), key, value);
            }
            targetConfig.save();
            LoggerFactory.getLogger(GitTools.class).debug("Finished copying configuration from {} to {}"
                    , source.getRepositoryPath(), target.getRepositoryPath());
            LoggerFactory.getLogger(GitTools.class).trace("adjustTargetConfiguration -> Exit");
        } catch (ConfigInvalidException ex) {
            Logger.getLogger(GitTools.class.getName()).log(Level.SEVERE, "An error ocurred while copying configuration from " +
                    source.getRepositoryPath() + " to " + target.getRepositoryPath() + ".", ex);
        } catch (IOException ex) {
            Logger.getLogger(GitTools.class.getName()).log(Level.SEVERE, "An error ocurred while copying configuration from " +
                    source.getRepositoryPath() + " to " + target.getRepositoryPath() + ".", ex);
        }
    }
    
}
