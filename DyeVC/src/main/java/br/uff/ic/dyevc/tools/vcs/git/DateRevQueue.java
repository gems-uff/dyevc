package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

/** A queue of commits sorted by commit time order. */
public class DateRevQueue {
    /** Field description */
    Entry head;

    /** Field description */
    Entry free;

    /** Create an empty date queue. */
    public DateRevQueue() {
        super();
    }

    /**
     * Method description
     *
     *
     * @param c
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
     * Method description
     *
     *
     * @return
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
     *
     * @return the next available commit; null if there are no commits left.
     */
    public CommitInfo peek() {
        return (head != null) ? head.commit : null;
    }

    /**
     * Method description
     *
     */
    public void clear() {
        head = null;
        free = null;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String toString() {
        final StringBuilder s = new StringBuilder();
        for (Entry q = head; q != null; q = q.next) {
            s.append(q.commit.toString());
        }

        return s.toString();
    }

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

    private void freeEntry(final Entry e) {
        e.next = free;
        free   = e;
    }

    /**
     * Method description
     *
     *
     * @param f
     * @return
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
     * Class description
     *
     * @author         Cristiano Cesario
     */
    static class Entry {
        /** Field description */
        Entry next;

        /** Field description */
        CommitInfo commit;
    }
}
