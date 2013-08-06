package br.uff.ic.dyevc.exception;

/**
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class ServiceException extends DyeVCException{
    public ServiceException() {
        super();
    }

    public ServiceException(String msg, Throwable t) {
        super(msg, t);
    }

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(Throwable t) {
        super(t);
    }
}
