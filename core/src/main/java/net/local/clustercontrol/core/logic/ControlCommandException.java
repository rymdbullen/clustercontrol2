package net.local.clustercontrol.core.logic;

public class ControlCommandException extends RuntimeException {
	/**
	 * Generated Serial Verion UID
	 */
	private static final long serialVersionUID = -8706871090987172319L;
	
	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ControlCommandException() {
    	super();
    }
    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ControlCommandException(String message) {
    	super(message);
    }
    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ControlCommandException(String message, Exception e) {
    	super(message, e);
    }

}
