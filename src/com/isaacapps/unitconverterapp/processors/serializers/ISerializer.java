package com.isaacapps.unitconverterapp.processors.serializers;

import java.util.Locale;

/**
 * Implementation contract for serializer that transforms an object to string representation.
 * Enssentially, reverses the result of a parser
 * @param <T> Type of the object that will be transformed
 */
public interface ISerializer<T> {
    /**
     * Transforms a provided obeject into a well formatted string representation.
     * @param itemToBeSerialized Object that will be transformed.
     * @return Well formatted string representing object
     * @throws SerializingException
     */
    String serialize(T itemToBeSerialized) throws SerializingException;

    /**
     * Locale used to create string representation
     */
    Locale getLocale();
    void setLocale(Locale locale);
}
