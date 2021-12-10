package Utility;

import java.io.Serializable;

/**
 * This class is used to serialize/deserialize the responses.
 */
public class Response implements Serializable {
    public enum Status {
        SUCCEED,
        FAILED
    }

    private String responseCode;
    private String operation;
    private Status status;
    private String filename;

    public String getResponseCode() {
        return responseCode;
    }
    public String getOperation() {
        return operation;
    }
    public Status getStatus() {
        return status;
    }
    public String getFilename() {
        return filename;
    }

    public Response(String responseCode, String operation, Status status, String filename) {
        this.responseCode = responseCode;
        this.operation = operation;
        this.status = status;
        this.filename = filename;
    }

    /**
     * Serialize the response.
     * Print out the response in a format of "code: , operation: , filename: , status: ".
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("Code: %s, + Operation: %s, + Filename: %s, + Status: %s.", responseCode, operation, filename, status);
    }
}

