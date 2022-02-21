package ua.com.solidity.web.exception;

public class PersonAlreadyExistException extends RuntimeException {

    public PersonAlreadyExistException(Long id) {
        super("User with id " + id + " is already exists.");
    }

    public PersonAlreadyExistException(String email) {
        super("Користувач з емейлом " + email + " вже існує.");
    }

}
