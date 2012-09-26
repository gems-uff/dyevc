package br.uff.ic.dyevc.exception;

/**
 *
 * @author Cristiano
 */
public class DyeVCException extends Exception {
    private static final long serialVersionUID = -7556308078062184536L;
    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to initCause.
     */
    public DyeVCException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to initCause.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the getMessage() method.
     */
    public DyeVCException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the getMessage() method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         getCause() method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public DyeVCException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         getCause() method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public DyeVCException(Throwable cause) {
        super(cause);
    }
}
