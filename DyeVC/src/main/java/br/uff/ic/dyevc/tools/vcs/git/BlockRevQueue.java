package br.uff.ic.dyevc.tools.vcs.git;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Class description
 * @author         Cristiano Cesario
 */
abstract class BlockRevQueue {
    /** Field description */
    protected BlockFreeList free;

    /** Create an empty revision queue. */
    protected BlockRevQueue() {
        free = new BlockFreeList();
    }

    /**
     * Reconfigure this queue to share the same free list as another.
     * <p>
     * Multiple revision queues can be connected to the same free list, making
     * it less expensive for applications to shuttle commits between them. This
     * method arranges for the receiver to take from / return to the same free
     * list as the supplied queue.
     * <p>
     * Free lists are not thread-safe. Applications must ensure that all queues
     * sharing the same free list are doing so from only a single thread.
     *
     * @param q
     *            the other queue we will steal entries from.
     */
    public void shareFreeList(final BlockRevQueue q) {
        free = q.free;
    }

    /**
     * Class description
     * @author         Cristiano Cesario
     */
    static final class BlockFreeList {
        private Block next;

        /**
         * Method description
         *
         * @return
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
         * Method description
         *
         * @param b
         */
        void freeBlock(final Block b) {
            b.next = next;
            next   = b;
        }

        /**
         * Method description
         *
         */
        void clear() {
            next = null;
        }
    }


    /**
     * Class description
     * @author         Cristiano Cesario
     */
    static final class Block {
        /** Field description */
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
         * Method description
         *
         * @return
         */
        boolean isFull() {
            return tailIndex == BLOCK_SIZE;
        }

        /**
         * Method description
         *
         * @return
         */
        boolean isEmpty() {
            return headIndex == tailIndex;
        }

        /**
         * Method description
         *
         * @return
         */
        boolean canUnpop() {
            return headIndex > 0;
        }

        /**
         * Method description
         *
         * @param c
         */
        void add(final CommitInfo c) {
            commits[tailIndex++] = c;
        }

        /**
         * Method description
         *
         * @param c
         */
        void unpop(final CommitInfo c) {
            commits[--headIndex] = c;
        }

        /**
         * Method description
         *
         * @return
         */
        CommitInfo pop() {
            return commits[headIndex++];
        }

        /**
         * Method description
         *
         * @return
         */
        CommitInfo peek() {
            return commits[headIndex];
        }

        /**
         * Method description
         *
         */
        void clear() {
            next      = null;
            headIndex = 0;
            tailIndex = 0;
        }

        /**
         * Method description
         *
         */
        void resetToMiddle() {
            headIndex = tailIndex = BLOCK_SIZE / 2;
        }

        /**
         * Method description
         *
         */
        void resetToEnd() {
            headIndex = tailIndex = BLOCK_SIZE;
        }
    }
}
