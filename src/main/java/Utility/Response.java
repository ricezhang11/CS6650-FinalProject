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
    private String content;

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
    public String getContent() {
        return content;
    }

    public Response(String responseCode, String operation, Status status, String filename) {
        this(responseCode, operation, status, filename, null);
    }

    public Response(String responseCode, String operation, Status status, String filename, String content) {
        this.responseCode = responseCode;
        this.operation = operation;
        this.status = status;
        this.filename = filename;
        this.content = content;
    }

    /**
     * Serialize the response.
     * Print out the response in a format of "code: , operation: , filename: , status: ".
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("Code: %s, + Operation: %s, + Filename: %s, + Content: %s, + Status: %s.", responseCode, operation, filename, content, status);
    }

    // use by clients to deserialize response string retrieved from the message queue
    // there will not be illegal formatted responses since this is coming from the server
    public static Response createResponse(String response) {
        String[] parts = response.trim().split(",");
        String responseCode = "";
        String operation = "";
        Status status = null;
        String filename = "";
        String content = "";

        for (String part : parts) {
            String[] pair = part.trim().split(":");
            if (pair[0].trim().equals("Code")) {
                responseCode = pair[1].trim();
            } else if (pair[0].trim().equals("Operation")) {
                operation = pair[1].trim();
            } else if (pair[0].trim().equals("Filename")) {
                filename = pair[1].trim();
            } else if (pair[0].trim().equals("Content")) {
                content = pair[1].trim();
            } else {
                if (pair[1].trim().equals("SUCCEED")) {
                    status = Status.SUCCEED;
                } else {
                    status = Status.FAILED;
                }
            }
        }

        return new Response(responseCode, operation, status, filename, content);
    }
}

