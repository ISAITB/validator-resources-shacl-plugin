package eu.europa.ec.itb.shacl.plugin.error;

public class PluginException extends RuntimeException {

    private static final String MESSAGE_DEFAULT = "An unexpected error was raised during validation.";

    public PluginException(Throwable cause) {
        this(MESSAGE_DEFAULT, cause);
    }

	public PluginException(String message) {
		this(message, null);
	}

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

}
