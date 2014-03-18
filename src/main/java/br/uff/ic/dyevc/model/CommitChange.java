package br.uff.ic.dyevc.model;

/**
 *
 * @author Cristiano
 */
public class CommitChange implements Comparable<CommitChange>{
    private String changeType;
    private String oldPath;
    private String newPath;

    /**
     * @return the changeType
     */
    public String getChangeType() {
        return changeType;
    }

    /**
     * @param changeType the changeType to set
     */
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    /**
     * @return the oldPath
     */
    public String getOldPath() {
        return oldPath;
    }

    /**
     * @param oldPath the oldPath to set
     */
    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    /**
     * @return the newPath
     */
    public String getNewPath() {
        return newPath;
    }

    /**
     * @param newPath the newPath to set
     */
    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getChangeType())
                .append(":&nbsp;");
        if (getChangeType().equalsIgnoreCase("RENAME") || getChangeType().equalsIgnoreCase("COPY")) {
            result.append(getOldPath()).append("&nbsp;->&nbsp;").append(getNewPath());
        } else if (getChangeType().equalsIgnoreCase("DELETE")) {
            result.append(getOldPath());
        } else {
            result.append(getNewPath());
        }
        return result.toString();
    }

    @Override
    public int compareTo(CommitChange o) {
        String thisPath = (this.newPath == null) ? this.oldPath : newPath;
        String thatPath = (o.getNewPath() == null) ? o.getOldPath() : o.getNewPath();
        return String.CASE_INSENSITIVE_ORDER.compare(thisPath, thatPath);
    }
    
    
}
