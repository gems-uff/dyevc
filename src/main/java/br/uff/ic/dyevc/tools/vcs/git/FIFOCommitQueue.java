package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

/** A queue of commits in FIFO order. */
public class FIFOCommitQueue extends BlockCommitQueue {
    private BlockCommitQueue.Block head;

    private BlockCommitQueue.Block tail;

    /** Create an empty FIFO queue. */
    public FIFOCommitQueue() {
        super();
    }

    /**
     * Adds a CommitInfo to the first available block.
     * @param c The CommitInfo to be added.
     */
    public void add(final CommitInfo c) {
        BlockCommitQueue.Block b = tail;
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
     * @param c the commit to insert into the queue.
     */
    public void unpop(final CommitInfo c) {
        BlockCommitQueue.Block b = head;
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
     * Pops the next element from the head block queue.
     * @return The next CommitInfo in the block queue.
     */
    public CommitInfo next() {
        final BlockCommitQueue.Block b = head;
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
     * Clears the queue.
     */
    public void clear() {
        head = null;
        tail = null;
        free.clear();
    }

    /**
     * Verifies if all elements in the queue have the specified flag.
     * @param f The flag to be verified.
     * @return True, if all elements have the flag.
     */
    boolean everbodyHasFlag(final int f) {
        for (BlockCommitQueue.Block b = head; b != null; b = b.next) {
            for (int i = b.headIndex; i < b.tailIndex; i++) {
                if ((b.commits[i].getFlags() & f) == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Verifies if at least one element in the call has the specified flag.
     * @param f The flag to be verified.
     * @return True, if at least one element has the specified flag.
     */
    boolean anybodyHasFlag(final int f) {
        for (BlockCommitQueue.Block b = head; b != null; b = b.next) {
            for (int i = b.headIndex; i < b.tailIndex; i++) {
                if ((b.commits[i].getFlags() & f) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Removes the specified flag from all elements in the queue.
     * @param f The flag to be removed.
     */
    void removeFlag(final int f) {
        final int not_f = ~f;
        for (BlockCommitQueue.Block b = head; b != null; b = b.next) {
            for (int i = b.headIndex; i < b.tailIndex; i++) {
                b.commits[i].setFlags(b.commits[i].getFlags() & not_f);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        for (BlockCommitQueue.Block q = head; q != null; q = q.next) {
            for (int i = q.headIndex; i < q.tailIndex; i++) {
                s.append(q.commits[i].toString());
            }
        }

        return s.toString();
    }
}
