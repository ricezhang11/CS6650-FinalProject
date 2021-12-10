package Learner;

import Utility.Request;
import Utility.Response;

import java.rmi.RemoteException;

public interface Learner extends java.rmi.Remote {
    Response commit(Request request) throws RemoteException;
}
