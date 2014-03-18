package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

/** A queue of commits sorted by commit time order. */
public class DateCommitQueue {
    /** The queue's head */
    Entry head;

    /** The queue's tail. */
    Entry free;

    /** Create an empty date queue. */
    public DateCommitQueue() {
        super();
    }

    /**
     * Adds the specified CommitInfo in the correct position in the queue, according to its commit date.
     * @param c The CommitInfo to be added.
     */
    public void add(final CommitInfo c) {
        Entry       q    = head;
        final long  when = c.getCommitDate().getTime();
        final Entry n    = newEntry(c);
        if ((q == null) || (when > q.commit.getCommitDate().getTime())) {
            n.next = q;
            head   = n;
        } else {
            Entry p = q.next;
            while ((p != null) && (p.commit.getCommitDate().getTime() > when)) {
                q = p;
                p = q.next;
            }

            n.next = q.next;
            q.next = n;
        }
    }

    /**
     * Pops the head element from the queue.
     * @return The next element in the queue.
     */
    public CommitInfo next() {
        final Entry q = head;
        if (q == null) {
            return null;
        }

        head = q.next;
        freeEntry(q);

        return q.commit;
    }

    /**
     * Peek at the next commit, without removing it.
     * @return the next available commit; null if there are no commits left.
     */
    public CommitInfo peek() {
        return (head != null) ? head.commit : null;
    }

    /**
     * Clears the queue by releasing its references.
     */
    public void clear() {
        head = null;
        free = null;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        for (Entry q = head; q != null; q = q.next) {
            s.append(q.commit.toString());
        }

        return s.toString();
    }

    /**
     * Creates a new entry to store a CommitInfo.
     * @param c The CommitInfo to be stored in the created entry.
     * @return The created entry.
     */
    private Entry newEntry(final CommitInfo c) {
        Entry r = free;
        if (r == null) {
            r = new Entry();
        } else {
            free = r.next;
        }

        r.commit = c;

        return r;
    }

    /**
     * Releases the specified entry.
     * @param e The entry to be released.
     */
    private void freeEntry(final Entry e) {
        e.next = free;
        free   = e;
    }

    /**
     * Verifies if all elements in the queue have the specified flag.
     * @param f The flag to be verified.
     * @return True, if all elements have the specified flag.
     */
    boolean everbodyHasFlag(final int f) {
        for (Entry q = head; q != null; q = q.next) {
            if ((q.commit.getFlags() & f) == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * An entry in the queue.
     * @author Cristiano Cesario
     */
    static class Entry {
        /** The next element. */
        Entry next;

        /** The CommitInfo stored in this entry. */
        CommitInfo commit;
    }
}
