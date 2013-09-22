package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/** A queue of commits in FIFO order. */
public class FIFORevQueue extends BlockRevQueue {
    private BlockRevQueue.Block head;

    private BlockRevQueue.Block tail;

    /** Create an empty FIFO queue. */
    public FIFORevQueue() {
        super();
    }

    /**
     * Method description
     *
     * @param c
     */
    public void add(final CommitInfo c) {
        BlockRevQueue.Block b = tail;
        if (b == null) {
            b = free.newBlock();
            b.add(c);
            head = b;
            tail = b;

            return;
        } else if (b.isFull()) {
            b         = free.newBlock();
            tail.next = b;
            tail      = b;
        }

        b.add(c);
    }

    /**
     * Insert the commit pointer at the front of the queue.
     *
     * @param c
     *            the commit to insert into the queue.
     */
    public void unpop(final CommitInfo c) {
        BlockRevQueue.Block b = head;
        if (b == null) {
            b = free.newBlock();
            b.resetToMiddle();
            b.add(c);
            head = b;
            tail = b;

            return;
        } else if (b.canUnpop()) {
            b.unpop(c);

            return;
        }

        b = free.newBlock();
        b.resetToEnd();
        b.unpop(c);
        b.next = head;
        head   = b;
    }

    /**
     * Method description
     *
     * @return
     */
    public CommitInfo next() {
        final BlockRevQueue.Block b = head;
        if (b == null) {
            return null;
        }

        final CommitInfo c = b.pop();
        if (b.isEmpty()) {
            head = b.next;

            if (head == null) {
                tail = null;
            }

            free.freeBlock(b);
        }

        return c;
    }

    /**
     * Method description
     *
     */
    public void clear() {
        head = null;
        tail = null;
        free.clear();
    }

    /**
     * Method description
     *
     * @param f
     * @return
     */
    boolean everbodyHasFlag(final int f) {
        for (BlockRevQueue.Block b = head; b != null; b = b.next) {
            for (int i = b.headIndex; i < b.tailIndex; i++) {
                if ((b.commits[i].getFlags() & f) == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Method description
     *
     * @param f
     * @return
     */
    boolean anybodyHasFlag(final int f) {
        for (BlockRevQueue.Block b = head; b != null; b = b.next) {
            for (int i = b.headIndex; i < b.tailIndex; i++) {
                if ((b.commits[i].getFlags() & f) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Method description
     *
     * @param f
     */
    void removeFlag(final int f) {
        final int not_f = ~f;
        for (BlockRevQueue.Block b = head; b != null; b = b.next) {
            for (int i = b.headIndex; i < b.tailIndex; i++) {
                b.commits[i].setFlags(b.commits[i].getFlags() & not_f);
            }
        }
    }

    /**
     * Method description
     *
     * @return
     */
    public String toString() {
        final StringBuilder s = new StringBuilder();
        for (BlockRevQueue.Block q = head; q != null; q = q.next) {
            for (int i = q.headIndex; i < q.tailIndex; i++) {
                s.append(q.commits[i].toString());
            }
        }

        return s.toString();
    }
}
