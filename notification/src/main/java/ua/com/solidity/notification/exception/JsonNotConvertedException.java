package ua.com.solidity.notification.exception;

public class JsonNotConvertedException extends RuntimeException{

    public JsonNotConvertedException(){super("Couldn't convert request fields. Please check your json");}
}
