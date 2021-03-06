package Utility;

import java.io.*;

/**
 * This class is used to serialize/deserialize the request.
 */
public class Request implements Serializable {
    public enum Operation {
        UPLOAD,
        DOWNLOAD,
        UPDATE,
        DELETE
    }

    private Operation operation;
    private String filename;
    private byte[] data;
    private String clientFolderName;

    public Operation getOperation() {
        return operation;
    }
    public String getFilename() {
        return filename;
    }
    public byte[] getData() {
        return data;
    }
    public String getClientFolderName() { return clientFolderName;}

    /**
     * Constructor for the request UPLOAD|UPDATE.
     *
     * @param operation
     * @param filename
     * @param data
     */
    public Request (Operation operation, String filename, byte[] data) {
        this.operation = operation;
        this.filename = filename;
        this.data = data;
    }

    public Request (Operation operation, String filename, byte[] data, String clientFolderName) {
        this.operation = operation;
        this.filename = filename;
        this.data = data;
        this.clientFolderName = clientFolderName;
    }


    /**
     * Constructor for the request DOWNLOAD|DELETE.
     *
     * @param operation
     * @param filename
     */
    public Request (Operation operation, String filename) {
        this(operation, filename, null);
    }

    /**
     * Validate the request input based on different operations.
     * If it's UPLOAD|UPDATE, then it should contain both filename and data.
     * If it's DOWNLOAD|DELETE, then it should only contain a filename.
     *
     * @param request
     * @return
     */
    private static boolean isValid(Request request) {
        if (request.getOperation() == Operation.UPLOAD || request.getOperation() == Operation.UPDATE) {
            if (request.filename == null || request.data == null) {
                return false;
            }
        } else if (request.getOperation() == Operation.DOWNLOAD || request.getOperation() == Operation.DELETE) {
//            if (request.filename == null || request.data != null) {
//                return false;
//            }
            if (request.filename == null) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Serialize the request.
     * Print out the request in a format of "operation: , filename: , data: size.".
     *
     * @return
     */
    @Override
    public String toString() {
        String res = String.format("operation: %s, filename: %s, clientFolderName: %s", operation.toString(), filename, clientFolderName);
        if (data != null) {
            res += ", data: " + data.length + " bytes.";
        } else {
            res += ", no data.";
        }
        return res;
    }

    /**
     * Deserialize the message (string) and then convert it to a Request object.
     * Based on the format we set.
     *
     * @param input
     * @return
     */
    public static Request createRequest(String input, String clientFolderName) {
        if (input == null || input.length() == 0) {
            throw new IllegalArgumentException("The input is invalid.");
        }

        // The input should be in a format of "operation, filename".
        String[] parts = input.trim().split(",");
        if (parts.length != 2 && parts.length != 3 && parts.length != 4) {
            throw new IllegalArgumentException("Malformed request with " + parts.length + " parts.");
        }

        // Parse the string.
        Operation operation = null;
        String filename = null;
        String folder = null;
        byte[] data = null;
        for (String part : parts) {
            String[] pair = part.trim().split(":");
            // It should contain the name and the corresponding value.
            if (pair.length != 3 && pair.length != 2) {
                throw new IllegalArgumentException("The value of the element is missing.");
            }
            if (pair[0].trim().equals("operation")) {
                operation = Operation.valueOf(pair[1].trim());
            } else if (pair[0].trim().equals("filename")) {
                filename = pair[1].trim();
            } else if (pair[0].trim().equals("clientFolderName")) {
                folder = pair[1].trim();
            }
        }

        // Load the file data.
        try {
            if (clientFolderName.equals("")) {
                data = loadFile(filename, folder);
            } else{
                data = loadFile(filename, clientFolderName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Request result;
        if (clientFolderName.equals("")) {
            result = new Request(operation, filename, data, folder);
        } else {
            result = new Request(operation, filename, data, clientFolderName);
        }

        // Validate the request before finishing.
        if (!isValid(result)) {
            throw new IllegalArgumentException("Malformed request with Syntax error.");
        }
        return result;
    }

    /**
     * Read the file data into the byte[].
     *
     * @param filename
     * @return
     * @throws IOException
     */
    private static byte[] loadFile(String filename, String clientFolderName) throws IOException {
//        File file = new File(System.getProperty("user.dir") + "/src/main/java" + filename);
        File file = new File(System.getProperty("user.dir") + "/" + clientFolderName + "/" + filename);

        // Assuming files are small and can fit in memory
        byte[]  data= new byte[(int) (file.length())];

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            // Read file into bytes.
            fileInputStream.read(data);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        return data;
    }
}
