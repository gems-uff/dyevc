package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

/**
 * Stores a number of commits as a queue which is accessed in blocks to avoid memory fragmentation.
 * @author  Cristiano Cesario
 */
abstract class BlockCommitQueue {
    /** A block with free space to receive more commits. */
    protected BlockFreeList free;

    /** Create an empty commit queue. */
    protected BlockCommitQueue() {
        free = new BlockFreeList();
    }

    /**
     * Reconfigure this queue to share the same free list as another.
     * <p>
     * Multiple commit queues can be connected to the same free list, making
     * it less expensive for applications to shuttle commits between them. This
     * method arranges for the receiver to take from / return to the same free
     * list as the supplied queue.
     * <p>
     * Free lists are not thread-safe. Applications must ensure that all queues
     * sharing the same free list are doing so from only a single thread.
     *
     * @param q the other queue we will steal entries from.
     */
    public void shareFreeList(final BlockCommitQueue q) {
        free = q.free;
    }

    /**
     * Stores a list queue of blocks
     * @author         Cristiano Cesario
     */
    static final class BlockFreeList {
        private Block next;

        /**
         * Returns an empty block from the chain. If the next block is null, then create a new one.
         *
         * @return An empty block.
         */
        Block newBlock() {
            Block b = next;
            if (b == null) {
                return new Block();
            }

            next = b.next;
            b.clear();

            return b;
        }

        /**
         * Releases the reference to the specified block, freeing it.
         * @param b the block to be released.
         */
        void freeBlock(final Block b) {
            b.next = next;
            next   = b;
        }

        /**
         * Clears the block list by releasing the reference to its next element.
         */
        void clear() {
            next = null;
        }
    }


    /**
     * A block of commits.
     * @author Cristiano Cesario
     */
    static final class Block {
        /** Size of the block. */
        static final int BLOCK_SIZE = 256;

        /** Next block in our chain of blocks; null if we are the last. */
        Block next;

        /** Our table of queued commits. */
        final CommitInfo[] commits = new CommitInfo[BLOCK_SIZE];

        /** Next valid entry in {@link #commits}. */
        int headIndex;

        /** Next free entry in {@link #commits} for addition at. */
        int tailIndex;

        /**
         * Verifies if this block is full, i.e., if the {@link #tailIndex} is equal to the {@link #BLOCK_SIZE}.
         * @return True, if this block is full.
         */
        boolean isFull() {
            return tailIndex == BLOCK_SIZE;
        }

        /**
         * Verifies if this block is empty, i.e., the {@link #tailIndex} is equal to the {@link #headIndex}
         * @return True, if the block has no elements.
         */
        boolean isEmpty() {
            return headIndex == tailIndex;
        }

        /**
         * Verifies if the queue can be unpoped.
         * @return True, if the queue can be unpoped.
         */
        boolean canUnpop() {
            return headIndex > 0;
        }

        /**
         * Adds a CommitInfo to the queue.
         * @param c The CommitInfo to be added.
         */
        void add(final CommitInfo c) {
            commits[tailIndex++] = c;
        }

        /**
         * Unpops a CommitInfo to the queue.
         * @param c The CommitInfo to be unpoped.
         */
        void unpop(final CommitInfo c) {
            commits[--headIndex] = c;
        }

        /**
         * Pops from the queue.
         * @return the popped CommitInfo.
         */
        CommitInfo pop() {
            return commits[headIndex++];
        }

        /**
         * Gets the head element from the queue, without popping it.
         * @return The head CommitInfo.
         */
        CommitInfo peek() {
            return commits[headIndex];
        }

        /**
         * Clears this block.
         */
        void clear() {
            next      = null;
            headIndex = 0;
            tailIndex = 0;
        }

        /**
         * Resets the {@link #headIndex} to the middle of the queue.
         */
        void resetToMiddle() {
            headIndex = tailIndex = BLOCK_SIZE / 2;
        }

        /**
         * Resets the {@link #headIndex} to the end of the queue.
         */
        void resetToEnd() {
            headIndex = tailIndex = BLOCK_SIZE;
        }
    }
}
