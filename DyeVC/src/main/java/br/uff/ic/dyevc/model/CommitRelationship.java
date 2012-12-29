package br.uff.ic.dyevc.model;

/**
 * Relates two <a href="CommitInfo">CommitInfo</a> objects, child and parent, with 
 * each other.
 * 
 * @author Cristiano
 */
public class CommitRelationship {

    private CommitInfo child;
    private CommitInfo parent;

    public CommitRelationship(CommitInfo child, CommitInfo parent) {
        this.child = child;
        this.parent = parent;
        child.incrementParents();
        parent.incrementChildren();
    }

    public CommitInfo getChild() {
        return child;
    }

    public CommitInfo getParent() {
        return parent;
    }

    @Override
    public String toString() { // Always good for debugging
        return child + " --> " + parent;
    }
}
