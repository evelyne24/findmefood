package org.codeandmagic.findmefood.service;

/**
 * Created by evelyne24.
 */
public class MalformedRequestException extends RuntimeException {

    public MalformedRequestException(String message) {
        super(message);
    }
}
