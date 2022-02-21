package ua.com.solidity.web.exception;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(Integer id) {
        super("Could not find person with id " + id);
    }

    public PersonNotFoundException(String login) {
        super("Користувача з логіном " + login + " не існує.");
    }

}
