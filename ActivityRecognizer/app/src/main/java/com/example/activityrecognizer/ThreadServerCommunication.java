package com.example.activityrecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.CookieStore;
import java.net.Socket;

public class ThreadServerCommunication extends Thread{
    private final MyForegroundService mfs;

    ThreadServerCommunication(MyForegroundService mfs){
        this.mfs=mfs;
    }

    public void run(){
        Socket s;
        try{
            s = new Socket(mfs.ip, Constants.PORTNUMBER);
        } catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
        while (true) {
            try {
                if(isInterrupted()){
                    s.close();
                    return;
                }
                // sleeps for SLEEPTIME
                Thread.sleep(Constants.SLEEPTIME);

                // variables for reading and writing in the socket
                OutputStream output = s.getOutputStream();
                BufferedReader input=new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter writer = new PrintWriter(output, false);

                // collects data from the foreground service
                String str = mfs.getValues();
                // sends the length into the socket
                writer.print(Integer.valueOf(str.length()));
                writer.flush();
                // sends the collected data into the socket as a string
                writer.print(str);
                writer.flush();
                // reads the server's response
                String pred = input.readLine();
                // adds the prediction into the CollectedData class
                MyForegroundService.cd.addPrediction(pred);
            } catch (IOException e){
                System.err.println(e.getMessage());
            }
            catch (InterruptedException ie){
                return;
            }
        }
    }
}
