package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

import org.eclipse.jgit.lib.AnyObjectId;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Computes the merge base(s) of the starting commits.
 * <p>
 * This generator is selected if the RevFilter is only {@link org.eclipse.jgit.revwalk.filter.RevFilter#MERGE_BASE}.
 * <p>
 * To compute the merge base we assign a temporary flag to each of the starting commits. The maximum number of starting
 * commits is bounded by the number of free flags available in the RevWalk when the generator is initialized. These
 * flags will be automatically released on the next reset of the RevWalk, but not until then, as they are assigned to
 * commits throughout the history.
 * <p>
 * Several internal flags are reused here for a different purpose, but this should not have any impact as this generator
 * should be run alone, and without any other generators wrapped around it.
 */
class MergeBaseGenerator {
    /**
     * Set on objects whose important header data has been loaded.
     * <p>
     * For a RevCommit this indicates we have pulled apart the tree and parent references from the raw bytes available
     * in the repository and translated those to our own local RevTree and RevCommit instances. The raw buffer is also
     * available for message and other header filtering.
     * <p>
     * For a RevTag this indicates we have pulled part the tag references to find out who the tag refers to, and what
     * that object's type is.
     */
    static final int PARSED = 1 << 0;

    /**
     * Set on RevCommit instances added to our {@link #pending} queue.
     * <p>
     * We use this flag to avoid adding the same commit instance twice to our queue, especially if we reached it by more
     * than one path.
     */
    static final int SEEN = 1 << 1;

    /**
     * Set on RevCommit instances added to our {@link #pending} queue.
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
     * Set on a RevCommit that can collapse out of the history.
     * <p>
     * If the {@link #treeFilter} concluded that this commit matches his parents' for all of the paths that the filter
     * is interested in then we mark the commit REWRITE. Later we can rewrite the parents of a REWRITE child to remove
     * chains of REWRITE commits before we produce the child to the application.
     *
     * @see RewriteGenerator
     */
    private static final int MERGE_BASE = 1 << 3;

    /**
     * Set on RevCommit instances the caller does not want output.
     * <p>
     * We flag commits as uninteresting if the caller does not want commits reachable from a commit given to
     * {@link #markUninteresting(RevCommit)}. This flag is always carried into the commit's parents and is a key part of
     * the "rev-list B --not A" feature; A is marked UNINTERESTING.
     */
    static final int UNINTERESTING = 1 << 2;

    /**
     * Field description
     */
    static final int         RESERVED_FLAGS = 6;
    private static final int APP_FLAGS      = -1 & ~((1 << RESERVED_FLAGS) - 1);

    /**
     * Field description
     */
    int                                   carryFlags = UNINTERESTING;
    private int                           freeFlags  = APP_FLAGS;
    private final ArrayList<CommitInfo>   roots;
    private DateRevQueue                  initQueue;
    private DateRevQueue                  pending;
    private final Map<String, CommitInfo> commitMap;
    private int                           branchMask;
    private int                           recarryTest;
    private int                           recarryMask;
    private boolean                       initialized = false;

    /**
     * Constructs ...
     *
     *
     * @param cis
     */
    MergeBaseGenerator(final Map<String, CommitInfo> cis) {
        commitMap = cis;
        pending   = new DateRevQueue();
        initQueue = new DateRevQueue();
        roots     = new ArrayList<CommitInfo>();
    }

    private void init(DateRevQueue p) {
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
        pending.add(c);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     */
    CommitInfo getBase() {

        // Initializes pending queue based on initQueue (nodes marked to start from)
        if (!initialized) {
            init(initQueue);
        }

        for (;;) {
            final CommitInfo c = pending.next();
            if (c == null) {
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
                }

                p.setFlags(p.getFlags() | IN_PENDING);
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
                    return null;
                }

                continue;
            }

            c.setFlags(c.getFlags() | POPPED);

            if (mb) {
                c.setFlags(c.getFlags() | MERGE_BASE);

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

        if ((p.getFlags() & recarryMask) == recarryTest) {

            // We were popped without being a merge base, but we just got
            // voted to be one. Inject ourselves back at the front of the
            // pending queue and tell all of our ancestors they are within
            // the merge base now.
            //
            p.setFlags(p.getFlags() & ~POPPED);
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
     * <p>
     * Callers are encouraged to use {@link #parseCommit(AnyObjectId)} to obtain the commit reference, rather than
     * {@link #lookupCommit(AnyObjectId)}, as this method requires the commit to be parsed before it can be added as a
     * root for the traversal.
     * <p>
     * The method will automatically parse an unparsed commit, but error handling may be more difficult for the
     * application to explain why a RevCommit is not actually a commit. The object pool of this walker would also be
     * 'poisoned' by the non-commit RevCommit.
     *
     * @param c the commit to start traversing from. The commit passed must be from this same revision walker.
     * invocation to {@link #lookupCommit(AnyObjectId)}. {@link #lookupCommit(AnyObjectId)}.
     */
    public void markStart(final CommitInfo c) {
        if ((c.getFlags() & SEEN) != 0) {
            return;
        }

        if ((c.getFlags() & PARSED) == 0) {
            c.markInWalk();
            c.setFlags(c.getFlags() | PARSED);
        }

        c.setFlags(c.getFlags() | SEEN);
        roots.add(c);
        initQueue.add(c);
    }

    /**
     * Resets internal state and allows this instance to be used again.
     * <p>
     * Unlike {@link #dispose()} previously acquired CommitInfo instances are not invalidated.
     *
     */
    protected void reset() {
        int retainFlags = 0;
        retainFlags |= PARSED;
        final int          clearFlags = ~retainFlags;

        final FIFORevQueue q          = new FIFORevQueue();
        for (final CommitInfo c : roots) {
            if ((c.getFlags() & clearFlags) == 0) {
                continue;
            }

            c.setFlags(c.getFlags() & retainFlags);
            q.add(c);
        }

        for (;;) {
            final CommitInfo c = q.next();
            if (c == null) {
                break;
            }

            if ((c.getParents() == null) || (c.getParents().isEmpty())) {
                continue;
            }

            for (final String id : c.getParents()) {
                CommitInfo p = commitMap.get(id);
                if ((p.getFlags() & clearFlags) == 0) {
                    continue;
                }

                p.setFlags(p.getFlags() & retainFlags);
                q.add(p);
            }

            c.resetWalk();
        }

        roots.clear();
        pending = new DateRevQueue();
    }

    /**
     * Dispose all internal state and invalidate all RevObject instances.
     * <p>
     * All RevObject (and thus RevCommit, etc.) instances previously acquired from this RevWalk are invalidated by a
     * dispose call. Applications must not retain or use RevObject instances obtained prior to the dispose call. All
     * RevFlag instances are also invalidated, and must not be reused.
     */
    public void dispose() {
        freeFlags  = APP_FLAGS;
        carryFlags = UNINTERESTING;
        pending    = new DateRevQueue();
        initQueue  = new DateRevQueue();
        roots.clear();
    }
}
