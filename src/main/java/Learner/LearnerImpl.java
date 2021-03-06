// this is the version that runs with db
package Learner;

import Database.Database;
import Utility.Request;
import Utility.Response;

import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * The learner interacts with database.
 */
public class LearnerImpl extends java.rmi.server.UnicastRemoteObject implements Learner {
    String port;
    ExecutorService pool;
    Logger logger = Logger.getLogger("LearnerImpl");
    Database db;

    public LearnerImpl(String port) throws java.rmi.RemoteException {
        super();
        this.port = port;
        this.pool = Executors.newFixedThreadPool(5);
        this.db = new Database("defaultDatabase", "myCollection");
    }

    /**
     * Multithreaded learner for committing the operations.
     *
     * @param request the consensus result
     * @return a Response object that contains the result of the operation
     */
    @Override
    public Response commit(Request request) {
        // send initial requests
        Process myProcess = new Process(request, this.db);
        Future future;
        // submit a task to the thread pool and this will return a Future object
        future = this.pool.submit(myProcess);
        // keep checking until the task is finished and then return the response to client
        for (;;) {
            if (future.isDone()) {
                // when task is finished, return the response object
                logger.info(request.getOperation().toString());
                logger.info(myProcess.getResponse().toString());
                return myProcess.getResponse();
            }
        }
    }
}

/**
 * This is a class that implements the Runnable interface. It will run on a separate thread.
 * This class will take the client input and call the correct method to perform the operation
 * towards the data store.
 */
class Process implements Runnable {
    Request request;
    Response response;
    Database db;
//    TODO: add newStr for updating the db
//    String newStr;

    public Process (Request request, Database db
    ) {
        this.request = request;
        this.response = null;
        this.db = db;
    }

    public Response getResponse() {
        return response;
    }
    public void run(){
        try {
            processRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this method will perform the actual operations
    public void processRequest() throws Exception {
        Logger logger = Logger.getLogger("DatabaseCrud");
        // retrieve the operation, filepath from the request message
        Request.Operation operation = request.getOperation();
        String filename = request.getFilename();
        String filepath = System.getProperty("user.dir") + "/" + request.getClientFolderName() + "/" + filename;
        System.out.println("!!!!!!!!!!!!!!!!!!");
        System.out.println(filepath);
        // if the operation is UPLOAD
        if (operation.equals(Request.Operation.UPLOAD)) {
            // ?????
//            try (FileOutputStream fos = new FileOutputStream(filepath)) {
//                fos.write(request.getData());
//            }
            // Upload to db
            System.out.println("executing UPLOAD operation");
            String content = db.upload(filepath);
            System.out.println("content is:" + content);
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, content);
//            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, "upload");
            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully uploaded " + "\"" + filepath + "\"" );
        } else if (operation.equals(Request.Operation.UPDATE)) {
            // Update on db
            System.out.println("executing UPDATE operation");
            String content = db.update(filepath);
//            String content = db.update(filepath, "test update");
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, content);
//            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, "update");
            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully updated " + "\"" + filepath + "\"" );
        } else if (operation.equals(Request.Operation.DELETE)) {
            // Delete on db
            System.out.println("executing DELETE operation");
            db.delete(filepath);
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename);
            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully deleted " + "\"" + filepath + "\"" );
        } else if (operation.equals(Request.Operation.DOWNLOAD)) {
            // Download on db
            System.out.println("executing DOWNLOAD operation");
            String content = db.download(filepath);
            System.out.println("content is:" + content);
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, content);
//            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, "download");
            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully downloaded " + "\"" + filepath + "\"");
        }
        else {
            System.out.println("ERROR! not executing anything");
            this.response = new Response("400", operation.toString(), Response.Status.FAILED, filename);
            logger.warning(new Timestamp(System.currentTimeMillis()) + "ERROR: Process request " + request.getOperation().toString());
        }
    }
}






// this is the version that runs without database
//package Learner;
//
////import Database.Database;
//import Utility.Request;
//import Utility.Response;
//
//import java.io.FileOutputStream;
//import java.sql.Timestamp;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.logging.Logger;
//
///**
// * The learner interacts with database.
// */
//public class LearnerImpl extends java.rmi.server.UnicastRemoteObject implements Learner {
//    String port;
//    ExecutorService pool;
//    Logger logger = Logger.getLogger("LearnerImpl");
////    Database db;
//
//    public LearnerImpl(String port) throws java.rmi.RemoteException {
//        super();
//        this.port = port;
//        this.pool = Executors.newFixedThreadPool(5);
////        this.db = new Database("defaultDatabase", "myCollection");
//    }
//
//    /**
//     * Multithreaded learner for committing the operations.
//     *
//     * @param request the consensus result
//     * @return a Response object that contains the result of the operation
//     */
//    @Override
//    public Response commit(Request request) {
//        // send initial requests
//        Process myProcess = new Process(request
////                this.db
//        );
//        Future future;
//        // submit a task to the thread pool and this will return a Future object
//        future = this.pool.submit(myProcess);
//        // keep checking until the task is finished and then return the response to client
//        for (;;) {
//            if (future.isDone()) {
//                // when task is finished, return the response object
//                logger.info(request.getOperation().toString());
//                logger.info(myProcess.getResponse().toString());
//                return myProcess.getResponse();
//            }
//        }
//    }
//}
//
///**
// * This is a class that implements the Runnable interface. It will run on a separate thread.
// * This class will take the client input and call the correct method to perform the operation
// * towards the data store.
// */
//class Process implements Runnable {
//    Request request;
//    Response response;
////    Database db;
////    TODO: add newStr for updating the db
////    String newStr;
//
//    public Process (Request request
////                    Database db
//    ) {
//        this.request = request;
//        this.response = null;
////        this.db = db;
//    }
//
//    public Response getResponse() {
//        return response;
//    }
//    public void run(){
//        try {
//            processRequest();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // this method will perform the actual operations
//    public void processRequest() throws Exception {
//        Logger logger = Logger.getLogger("DatabaseCrud");
//        Request.Operation operation = request.getOperation();
//        String filename = request.getFilename();
//        String filepath = System.getProperty("user.dir") + "/" + request.getClientFolderName() + "/" + filename;
//        System.out.println("!!!!!!!!!!!!!!!!!!");
//        System.out.println(filepath);
//        if (operation.equals(Request.Operation.UPLOAD)) {
//            try (FileOutputStream fos = new FileOutputStream(filepath)) {
//                fos.write(request.getData());
//            }
//            // Upload to db
////            String content = db.upload(filepath);
////            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, content);
//            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, "upload");
//            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully uploaded " + "\"" + filepath + "\"" );
//        } else if (operation.equals(Request.Operation.UPDATE)) {
//            // Update on db
////            String content = db.update(filepath, new String(request.getData()));
////            String content = db.update(filepath, "test update");
////            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, content);
//            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, "update");
//            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully updated " + "\"" + filepath + "\"" );
//        } else if (operation.equals(Request.Operation.DELETE)) {
//            // Delete on db
////            db.delete(filepath);
//            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename);
//            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully deleted " + "\"" + filepath + "\"" );
//        } else if (operation.equals(Request.Operation.DOWNLOAD)) {
//            // Download on db
////            String content = db.download(filepath);
////            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, content);
//            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename, "download");
//            logger.info(new Timestamp(System.currentTimeMillis()) + "Successfully downloaded " + "\"" + filepath + "\"");
//        }
//        else {
//            this.response = new Response("400", operation.toString(), Response.Status.FAILED, filename);
//            logger.warning(new Timestamp(System.currentTimeMillis()) + "ERROR: Process request " + request.getOperation().toString());
//        }
//    }
//}
//
