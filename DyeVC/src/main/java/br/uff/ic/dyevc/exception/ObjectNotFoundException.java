package br.uff.ic.dyevc.exception;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.topology.RepositoryInfo;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

/**
 * Exception to mark that a searched object was not found.
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class ObjectNotFoundException extends DyeVCException {
    /**
     * Constructs ...
     */
    public ObjectNotFoundException() {
        super();
    }

    /**
     * Constructs ...
     *
     * @param msg
     * @param t
     */
    public ObjectNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Constructs ...
     *
     * @param msg
     */
    public ObjectNotFoundException(String msg) {
        super(msg);
    }

    /**
     * Constructs ...
     *
     * @param t
     */
    public ObjectNotFoundException(Throwable t) {
        super(t);
    }
}
