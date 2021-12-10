package Learner;

import Utility.Request;
import Utility.Response;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * The learner has the data store and all the operation logic.
 */
public class LearnerImpl extends java.rmi.server.UnicastRemoteObject implements Learner {
    String port;
    Store store;
    ExecutorService pool;
    Logger logger = Logger.getLogger("LearnerImpl");

    public LearnerImpl(String port) throws java.rmi.RemoteException {
        super();
        this.port = port;
        this.store = new Store();
        this.pool = Executors.newFixedThreadPool(5);
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
        Process myProcess = new Process(store, request);
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
 * This is the actual data store. The data is stored inside a hashmap. This class also defines
 * put, delete and get operations that can be performed on the hashmap.
 */
class Store {
    HashMap<String, String> storage;
    public Store() {
        this.storage = new HashMap<String, String>();
    }

    // put operation need to be synchronized
    public synchronized void put(String key, String value) {
        this.storage.put(key, value);
    }

    // delete operation need to be synchronized
    public synchronized void delete(String key) {
        this.storage.remove(key);
    }
    // get operation need to be synchronized
    public synchronized String get(String key) {
        return this.storage.get(key);
    }
}

/**
 * This is a class that implements the Runnable interface. It will run on a separate thread.
 * This class will take the client input and call the correct method to perform the operation
 * towards the data store.
 */
class Process implements Runnable {
    Store store;
    Request request;
    Response response;

    public Process (Store store, Request request) {
        this.store = store;
        this.request = request;
        this.response = null;
    }

    public Response getResponse() {
        return response;
    }

    // this method will perform the actual operations
    public void run() {
        Logger logger = Logger.getLogger("DataStoreImpl");
        Request.Operation operation = request.getOperation();
        String filename = request.getFilename();
        if (operation.equals(Request.Operation.UPLOAD)) {
            // Upload to db
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename);
        } else if (operation.equals(Request.Operation.UPDATE)) {
            // Update on db
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename);
        } else if (operation.equals(Request.Operation.DELETE)) {
            // Delete on db
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename);
        } else if (operation.equals(Request.Operation.DOWNLOAD)) {
            // DOWNLOAD on db
            this.response = new Response("200", operation.toString(), Response.Status.SUCCEED, filename);
        } else {
            this.response = new Response("400", operation.toString(), Response.Status.FAILED, filename);
        }
    }
}

