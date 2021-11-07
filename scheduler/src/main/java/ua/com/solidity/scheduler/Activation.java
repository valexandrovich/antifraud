package ua.com.solidity.scheduler;

public enum Activation {
    IGNORED,     // ItemSet is ignored
    PERIODIC,   // Periodic execution
    SET,        // Set of items
    ONCE        // Same as Set of items with one item only
}
