package duelmonster.superminer.objects;

/**
 * An unchecked exception for ThebombzenAPI's own reflection classes.
 * It is used if there is an error accessing a field by name.
 * This Exception should not be thrown at runtime except in a development environment,
 * assuming everything was done correctly and thus it's an unchecked wrapper.
 * @author thebombzen
 */
public class FieldNotFoundException extends RuntimeException {
	
	/**
	 * Throwable is serializable so we need this or the compiler will whine.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct an empty FieldNotFoundException.
	 */
	public FieldNotFoundException(){
		super();
	}
	
	/**
	 * Construct a FieldNotFoundException with the given informational messsage.
	 * @param message A short bit of information describing the Exception.
	 */
	public FieldNotFoundException(String message){
		super(message);
	}
	
	/**
	 * Construct a FieldNotFoundException with the given informational messsage
	 * and with the given Throwable as the cause. This means that this exception wraps that one.
	 * @param message A short bit of information describing the Exception.
	 * @param cause The exception to wrap with this one.
	 */
	public FieldNotFoundException(String message, Throwable cause){
		super(message, cause);
	}
	
	/**
	 * Construct a FieldNotFoundException with the given Throwable as the cause.
	 * This means that this exception wraps that one.
	 * @param cause The exception to wrap with this one.
	 */
	public FieldNotFoundException(Throwable cause){
		super(cause);
	}
}