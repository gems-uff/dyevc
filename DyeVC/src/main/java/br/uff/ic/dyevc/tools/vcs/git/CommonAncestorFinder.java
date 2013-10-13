package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.model.CommitInfo;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes the common ancestor of the starting commits.
 * <p>
 * To compute the common ancestor a temporary flag is assigned to each of the starting commits. The maximum number of starting
 * commits is bounded by the number of free flags available when the finder is initialized. These
 * flags will be automatically released on the next reset of the finder, but not until then, as they are assigned to
 * commits throughout the history.
 * <p>
 * Several internal flags are reused here for different purposes, but this should not have any impact as this finder
 * should be run alone, and without any other finders wrapped around it.
 */
public class CommonAncestorFinder {
    /**
     * Set on {@link br.uff.ic.dyevc.model.CommitInfo} instances when they are first found in the commit history.
     * <p>
     * For a CommitInfo this indicates we have pulled apart the tree and parent references from the raw bytes available
     * in the repository and translated those to our own local RevTree and RevCommit instances. The raw buffer is also
     * available for message and other header filtering.
     * <p>
     * For a RevTag this indicates we have pulled part the tag references to find out who the tag refers to, and what
     * that object's type is.
     */
    static final int PARSED = 1 << 0;

    /**
     * Set on {@link br.uff.ic.dyevc.model.CommitInfo} instances added to our {@link #pending} queue.
     * <p>
     * We use this flag to avoid adding the same commit instance twice to our queue, especially if we reached it by more
     * than one path.
     */
    static final int SEEN = 1 << 1;

    /**
     * Set on {@link br.uff.ic.dyevc.model.CommitInfo} instances added to our {@link #pending} queue.
     * <p>
     * We use this flag to avoid adding the same commit instance twice to our queue, especially if we reached it by more
     * than one path.
     */
    private static final int IN_PENDING = 1 << 1;

    /**
     * Temporary mark for use within generators or filters.
     * <p>
     * This mark is only for local use within a single scope. If someone sets the mark they must unset it before any
     * other code can see the mark.
     */
    private static final int POPPED = 1 << 4;

    /**
     * Set on a {@link br.uff.ic.dyevc.model.CommitInfo} instances that can be a common ancestor.
     */
    private static final int MERGE_BASE = 1 << 3;

    /**
     * Set on {@link br.uff.ic.dyevc.model.CommitInfo} instances the caller does not want output.
     */
    static final int UNINTERESTING = 1 << 2;

    /**
     * Number of reserver flags that the application cannot use.
     */
    static final int RESERVED_FLAGS = 6;

    /**
     * Flags that the application can use.
     */
    private static final int APP_FLAGS = -1 & ~((1 << RESERVED_FLAGS) - 1);

    /**
     * Field description
     */
    private int carryFlags = UNINTERESTING;
    private int freeFlags  = APP_FLAGS;

    /**
     * List of commits to start traversing from.
     */
    private final ArrayList<CommitInfo> roots;

    /**
     * Initial queue of commits to start traversing from.
     */
    private DateCommitQueue initQueue;

    /**
     * Queue of commits to be evaluated.
     */
    private DateCommitQueue pending;

    /**
     * Map of {@link br.uff.ic.dyevc.model.CommitInfo} instances, keyed by their hash.
     */
    private final Map<String, CommitInfo> commitMap;

    /**
     * Stores nodes that had flags changed, in order to reset them later;
     */
    private HashSet<CommitInfo> changed;
    private int                 branchMask;
    private int                 recarryTest;
    private int                 recarryMask;
    private boolean             initialized = false;

    /**
     * Constructs an instance of this finder.
     * @param cis The map of {@link br.uff.ic.dyevc.model.CommitInfo} where the common ancestor
     * will be searched.
     */
    public CommonAncestorFinder(final Map<String, CommitInfo> cis) {
        commitMap = cis;
        pending   = new DateCommitQueue();
        initQueue = new DateCommitQueue();
        roots     = new ArrayList<CommitInfo>();
        changed   = new HashSet<CommitInfo>();
    }

    /**
     * Initializes flags and pending queue based on a initial queue of commits.
     * @param p The queue of commits to initialize this finder.
     */
    private void init(DateCommitQueue p) {
        try {
            for (;;) {
                final CommitInfo c = p.next();
                if (c == null) {
                    break;
                }

                add(c);
            }
        } finally {
            initialized = true;

            // Always free the flags immediately. This ensures the flags
            // will be available for reuse when the walk resets.
            //
            freeFlag(branchMask);

            // Setup the condition used by carryOntoOne to detect a late
            // merge base and produce it on the next round.
            //
            recarryTest = branchMask | POPPED;
            recarryMask = branchMask | POPPED | MERGE_BASE;
        }
    }

    /**
     * Adds a commit in the pending queue and sets its flags
     * @param c
     */
    private void add(final CommitInfo c) {
        final int flag = allocFlag();
        branchMask |= flag;

        if ((c.getFlags() & branchMask) != 0) {

            // This should never happen. RevWalk ensures we get a
            // commit admitted to the initial queue only once. If
            // we see this marks aren't correctly erased.
            //
            throw new IllegalStateException(MessageFormat.format("Stale RevFlags on {0}", c.getHash()));
        }

        c.setFlags(c.getFlags() | flag);
        changed.add(c);
        pending.add(c);
    }

    /**
     * Gets the common ancestor of the commits marked as start commits. Caller should previously
     * invoke {@link #markStart(br.uff.ic.dyevc.model.CommitInfo) } method.
     * @param revisions The revisions to find the common ancestor.
     * @return The common ancestor for the commits marked as start commits.
     */
    public CommitInfo getCommonAncestor(String... revisions) throws DyeVCException {
        if (revisions == null) {
            throw new DyeVCException("Revisions cannot be null.");
        }

        if (revisions.length == 0) {
            throw new DyeVCException("Revisions cannot be empty.");
        }

        for (String revision : revisions) {
            markStart(commitMap.get(revision));
        }

        // Initializes pending queue based on initQueue (nodes marked to start from)
        if (!initialized) {
            init(initQueue);
        }

        for (;;) {
            final CommitInfo c = pending.next();

            if (c == null) {
                reset();

                return null;
            }

            for (final String id : c.getParents()) {
                final CommitInfo p = commitMap.get(id);

                if ((p.getFlags() & IN_PENDING) != 0) {
                    continue;
                }

                if ((c.getFlags() & PARSED) == 0) {
                    c.markInWalk();
                    c.setFlags(c.getFlags() | PARSED);
                    changed.add(c);
                }

                p.setFlags(p.getFlags() | IN_PENDING);
                changed.add(p);
                pending.add(p);
            }

            int     carry = c.getFlags() & branchMask;
            boolean mb    = carry == branchMask;
            if (mb) {

                // If we are a merge base make sure our ancestors are
                // also flagged as being popped, so that they do not
                // generate to the caller.
                //
                carry |= MERGE_BASE;
            }

            carryOntoHistory(c, carry);

            if ((c.getFlags() & MERGE_BASE) != 0) {

                // This commit is an ancestor of a merge base we already
                // popped back to the caller. If everyone in pending is
                // that way we are done traversing; if not we just need
                // to move to the next available commit and try again.
                //
                if (pending.everbodyHasFlag(MERGE_BASE)) {
                    reset();

                    return null;
                }

                continue;
            }

            c.setFlags(c.getFlags() | POPPED);
            changed.add(c);

            if (mb) {

//              c.setFlags(c.getFlags() | MERGE_BASE);
                reset();

                return c;
            }
        }
    }

    private void carryOntoHistory(CommitInfo c, final int carry) {
        for (;;) {
            final Set<String> parents = c.getParents();
            if (parents == null) {
                return;
            }

            final String[] pList = parents.toArray(new String[0]);
            if (pList == null) {
                return;
            }

            final int n = pList.length;
            if (n == 0) {
                return;
            }

            for (int i = 1; i < n; i++) {
                final CommitInfo p = commitMap.get(pList[i]);
                if (!carryOntoOne(p, carry)) {
                    carryOntoHistory(p, carry);
                }
            }

            c = commitMap.get(pList[0]);

            if (carryOntoOne(c, carry)) {
                break;
            }
        }
    }

    private boolean carryOntoOne(final CommitInfo p, final int carry) {
        final boolean haveAll = (p.getFlags() & carry) == carry;
        p.setFlags(p.getFlags() | carry);
        changed.add(p);

        if ((p.getFlags() & recarryMask) == recarryTest) {

            // We were popped without being a merge base, but we just got
            // voted to be one. Inject ourselves back at the front of the
            // pending queue and tell all of our ancestors they are within
            // the merge base now.
            //
            p.setFlags(p.getFlags() & ~POPPED);
            changed.add(p);
            pending.add(p);
            carryOntoHistory(p, branchMask | MERGE_BASE);

            return true;
        }

        // If we already had all carried flags, our parents do too.
        // Return true to stop the caller from running down this leg
        // of the revision graph any further.
        //
        return haveAll;
    }

    /**
     * Method description
     *
     *
     * @param mask
     */
    void freeFlag(final int mask) {
        freeFlags  |= mask;
        carryFlags &= ~mask;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    int allocFlag() {
        if (freeFlags == 0) {
            throw new IllegalArgumentException(MessageFormat.format("{0} flags already created",
                    Integer.valueOf(32 - RESERVED_FLAGS)));
        }

        final int m = Integer.lowestOneBit(freeFlags);
        freeFlags &= ~m;

        return m;
    }

    /**
     * Mark a commit to start graph traversal from.
     *
     * @param c the commit to start traversing from. The commit passed must exist in the {@link #commitMap} informed
     * on the constructor.
     */
    public final void markStart(final CommitInfo c) {
        if ((c.getFlags() & SEEN) != 0) {
            return;
        }

        if ((c.getFlags() & PARSED) == 0) {
            c.markInWalk();
            c.setFlags(c.getFlags() | PARSED);
            changed.add(c);
        }

        c.setFlags(c.getFlags() | SEEN);
        changed.add(c);
        roots.add(c);
        initQueue.add(c);
    }

    /**
     * Resets internal state of all changed commits and allows this instance to be used again.
     */
    protected void reset() {
        for (CommitInfo c : changed) {
            c.resetWalk();
        }

        roots.clear();
        changed.clear();
        initQueue   = new DateCommitQueue();
        pending     = new DateCommitQueue();
        freeFlags   = APP_FLAGS;
        carryFlags  = UNINTERESTING;
        initialized = false;
    }
}
