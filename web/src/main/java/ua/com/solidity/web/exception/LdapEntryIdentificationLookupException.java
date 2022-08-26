package ua.com.solidity.web.exception;

public class LdapEntryIdentificationLookupException extends RuntimeException{

    public LdapEntryIdentificationLookupException(String message) {
        super(message);
    }

    public LdapEntryIdentificationLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
