package br.uff.ic.dyevc.exception;

/**
 *
 * @author Cristiano
 */
public class VCSException extends DyeVCException{
    private static final long serialVersionUID = 838156896036443991L;

    public VCSException() {
        super();
    }

    public VCSException(String msg, Throwable t) {
        super(msg, t);
    }

    public VCSException(String msg) {
        super(msg);
    }

    public VCSException(Throwable t) {
        super(t);
    }
}
