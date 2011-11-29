package net.local.clustercontrol.core.logic;

public class WorkerNotFoundException extends Exception {

	/**
	 * Generated Serial Verion UID
	 */
	private static final long serialVersionUID = -1432289180104926231L;
	
    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public WorkerNotFoundException() {
    	super();
    }
    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public WorkerNotFoundException(String message) {
    	super(message);
    }
    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public WorkerNotFoundException(String message, Exception e) {
    	super(message, e);
    }
}
