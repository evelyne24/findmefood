package org.codeandmagic.findmefood.service;

/**
 * Created by evelyne24.
 */
public class HttpResponse {

    private final int statusCode;
    private final String response;
    private final Exception exception;

    public HttpResponse(int statusCode, String response, Exception exception) {
        this.statusCode = statusCode;
        this.response = response;
        this.exception = exception;
    }

    public HttpResponse(int statusCode, String response) {
        this(statusCode, response, null);
    }

    public String getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Exception getException() {
        return exception;
    }
}
