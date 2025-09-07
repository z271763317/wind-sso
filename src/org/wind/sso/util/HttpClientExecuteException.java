package org.wind.sso.util;

public class HttpClientExecuteException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7105719993194635706L;
	private int statusCode = -1;
    
    public HttpClientExecuteException(String msg) {
        super(msg);
    }

    public HttpClientExecuteException(Exception cause) {
        super(cause);
    }

    public HttpClientExecuteException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;

    }

    public HttpClientExecuteException(String msg, Exception cause) {
        super(msg, cause);
    }

    public HttpClientExecuteException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
