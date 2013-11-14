package br.uff.ic.dyevc.exception;

/**
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class MonitorException extends DyeVCException{
    public MonitorException() {
        super();
    }

    public MonitorException(String msg, Throwable t) {
        super(msg, t);
    }

    public MonitorException(String msg) {
        super(msg);
    }

    public MonitorException(Throwable t) {
        super(t);
    }
}
