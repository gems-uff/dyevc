package br.uff.ic.dyevc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Cristiano
 */
public class RepositoryStatusMessages {

    private static final long serialVersionUID = 1735605826498282433L;
    private HashMap<String, List<String>> statuses;
    
    public RepositoryStatusMessages() {
        statuses = new HashMap<String, List<String>>();
    }

    public void addMessages(MonitoredRepository repository, List<RepositoryStatus> status) {
        List<String> messages = new ArrayList<String>();
        for (Iterator<RepositoryStatus> it = status.iterator(); it.hasNext();) {
            String message = processMessage(repository, it.next());
            if (message != null) {
                messages.add(message);
            }
        }
        statuses.remove(repository.getId());
        statuses.put(repository.getId(), messages);
    }
    
    public List<String> getRepositoryMessages(String repositoryId) {
        return (statuses.get(repositoryId) != null) ? statuses.get(repositoryId) : Collections.EMPTY_LIST;
    }
    
    public List<String> getAllMessages() {
        List<String> result = new ArrayList<String>();
        
        for (Map.Entry<String, List<String>> entry : statuses.entrySet()) {
            String string = entry.getKey();
            List<String> list = entry.getValue();
            result.addAll(list);
        }
        
        return result;
    }

    public void clearMessages() {
        statuses.clear();
    }
    private String processMessage(MonitoredRepository repository, RepositoryStatus status) {
        StringBuilder message = new StringBuilder("Branch ").append(status.getRepositoryBranch())
                .append(" on repository ").append(repository.getName());
        if (status.getAhead() == 0) {
            if (status.getBehind() == 0) {
                return null;
                // message.append(" is synchronized with ");
            } else {
                message.append(" is ").append(status.getBehind()).append(" commit(s) behind of");
            }
        } else {
            if (status.getBehind() == 0) {
                message.append(" is ").append(status.getAhead()).append(" commit(s) ahead of");
            } else {
                message.append(" is ").append(status.getAhead()).append(" commit(s) ahead and ").append(status.getBehind()).append(" commit(s) behind of");
            }
        }
        message.append(" branch ").append(status.getReferencedRepositoryBranch()).append(".");
        return message.toString();
    }
}
