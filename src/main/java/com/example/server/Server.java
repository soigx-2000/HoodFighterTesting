package com.example.server;

import java.net.*;
import java.rmi.ConnectIOException;
import java.sql.Connection;
import java.io.*;
import java.util.Date;

import com.example.PlayerInput;

import java.util.ArrayList;

/**
 * remember to flush to use writeObject
 * This program is a server that takes connection requests on
 * the port specified by the constant LISTENING_PORT. When a
 * connection is opened, the program should allow the client to send it
 * messages. The messages should then
 * become visible to all other clients. The program will continue to receive
 * and process connections until it is killed (by a CONTROL-C,
 * for example).
 * 
 * This version of the program creates a new thread for
 * every connection request.
 */
public class Server {
    private ServerSocket listener; // Listens for incoming connections.
    private Socket connection; // For communication with the connecting program.
    private final int serverSize;
    private ConnectionHandler[] handlers;
    private Game game = new Game();
    public static final int LISTENING_PORT = 9880;
    private static int IDcount = 1;
    public Server(int serverSize) {
        System.out.println("Creating server...");
        this.serverSize = serverSize;
        handlers = new ConnectionHandler[serverSize];
        try {
            listener = new ServerSocket(LISTENING_PORT);
            System.out.println("Listening on port " + LISTENING_PORT);
            tryAcceptingNewClient();
        } catch (Exception e) {
            System.out.println("Sorry, the server has shut down.");
            System.out.println("Error:  " + e);
        }
    }
    //postcondition: return server size
    public int getServerSize(){
        return serverSize;
    }
    public void tryAcceptingNewClient() {
        while (true) {
            try {
                connection = listener.accept();
                System.out.println("connection found");
                for(int i=0; i<serverSize; i++){
                    ConnectionHandler handler = handlers[i];
                    if(i == 1){//both client are connected
                        game.start();
                    }
                    if(handler == null){//if there is no thread
                        System.out.println("Creating new Handler at " + i);
                        handlers[i] = new ConnectionHandler(connection);
                        break;
                    }
                    else if(!handler.isActive()){// if we find a inactive thread
                        System.out.println(String.valueOf(handler.client));
                        System.out.println("Activating Handler " + i);
                        handlers[i].changeToNewClient(connection);//put the connection there
                        //spawn a handler and it will some work
                        // Accept next connection request and handle it.
                        break;
                    }
                    if(i == serverSize-1){//last iteration
                        System.out.println("Server filled");//if we reach here, the server have reach its maximum capacity.
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Oops, something went wrong: " + e);
            }
        }
    }

    public static void main(String[] args) {
        Server c = new Server(2);
    } // end main()

    /**
     * Defines a thread that handles the connection with one
     * client.
     */
    private class ConnectionHandler extends Thread {
        public Socket client;
        private static ArrayList<ConnectionHandler> handlers;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        public ConnectionHandler(Socket socket) {
            client = socket;
            try {
                ois = new ObjectInputStream(client.getInputStream());
                oos = new ObjectOutputStream(client.getOutputStream());
                oos.flush();
                if(handlers == null){//if there are no handlers to begin with
                    handlers = new ArrayList<ConnectionHandler>();
                }
                handlers.add(this);
                this.start();
                System.out.println("handler spawned");
                oos.writeObject(new Integer(IDcount));//send player ID to client, this is just for testing, we will change this later
                IDcount++;
            }catch (IOException e) {
                System.out.println("Spawning handler falied: " + e);
            }
        }
        //precondition: socket connected to a client that is connected to us
        //current client == null;
        public void changeToNewClient(Socket socket){
            client = socket;
            try {
                ois = new ObjectInputStream(client.getInputStream());
                oos = new ObjectOutputStream(client.getOutputStream());
                ///oos.flush();///Claude wrote this, I have no idea why.
                if(handlers == null){// creat an arraylist if there isn't one
                    handlers = new ArrayList<ConnectionHandler>();
                }
                handlers.add(this);
                System.out.println("handler activated");
                synchronized(this) {
                    notifyAll();
                }
            }catch (IOException e) {
                System.out.println("Activating handler falied: " + e);
            }
        }
        //postcondition: return if the handler still have a client
        // (if this is false, the handlers will go to sleep until it is notified)
        public boolean isActive(){
            return (client != null);
        }

        //precondition: received String s from inputStream
        //postcondition: broadcast this information to all handlers
        private void broadCast(Game game){
            int count =0;
            for(ConnectionHandler h : handlers){// iterate through every handlers
                if(h.isActive()){
                    System.out.println();
                    System.out.println("Broadcasting to Client "+ count++);
                    h.sendGame(game);
                    //send message to by using every thread with a client
                }
            }
        }
        //precondition: s != null 
        //postcondition: send message Sting s to the client
        private void sendGame(Game game){
            try{
                oos.writeObject(game);
                oos.flush();
                System.out.println();
                System.out.println("game sent");
            }
            catch(IOException e){
                System.err.println("connection failed");
            }
        }
        public void run() {
        String clientAddress = client.getInetAddress().toString();
            while(true){
                synchronized(this){
                    double currentTime = System.nanoTime()/1000000.0;//current time in milisecond
                    double interval = 1000.0/60.0;
                    double nextDrawTime =  currentTime + interval;
                    if (isActive()) {
                        try {//this try test if the input stream is ongoing and sending strings
                            PlayerInput input = (PlayerInput) ois.readObject();
                            if (input != null){
                                if(input.playerControl == false){//player 1 input;
                                    game.input1 = input;
                                }
                                else{//player 2 input
                                    game.input2 = input;
                                }
                            }
                            oos.writeObject(game);
                            oos.flush();
                        } 
                        catch (EOFException e) {// client gone
                            try{
                                System.out.println("thread deactivated");
                                client = null;
                                handlers.remove(this);//remove ourself from the ArrayList
                                wait();//we will wait until notified to pick up a new client
                            }
                            catch(InterruptedException e2){
                                e2.printStackTrace();
                            }
                            //whatever you want to do when they disconnect(waiting for a new client in my case)
                        }
                        catch(Exception e){
                            System.err.println("client sent me some crazy stuff");
                            e.printStackTrace();
                        }
                    }
                    long sleepTime = (long) (nextDrawTime - System.nanoTime()/1000000.0);//this is negative some time for some reason
                    try{//this bit does the tick limitation
                        Thread.sleep(sleepTime);
                    }
                    catch(InterruptedException e){
                        System.err.println("Client disconnect");
                    }
                }
            }
        }
    }

}
