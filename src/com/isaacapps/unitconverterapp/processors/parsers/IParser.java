package com.isaacapps.unitconverterapp.processors.parsers;

/**
 * Implementation contact for parsers that transforms an string to an instance of an object.
 * @param <T> Type of the object that a provided string will be parsed into.
 */
public interface IParser<T> {

    /**
     * Parses a provided string into an instance of an object
     * @param parsableItem Well-formatted and parsable string.
     * @return Object pasred from string
     * @throws ParsingException
     */
    T parse(String parsableItem) throws ParsingException;

}
