package labjack;

/*
 *  DaqTime.java
 *
 *  Utilities to handle elapsed time for data acquisition
 *
 *  brian@omnificsolutions.com
 *  Sept. 5, 2011
 */

import java.util.*;

public class DaqTime  {
    
    long startTime;    // 
    long elapsedTime;  //
        
    public void setStartTime(){
        Date now = new Date();
        startTime = now.getTime() ;
        elapsedTime = 0;
    }
    public long getStartTime(){
        return startTime;   
    }
    
    public long getNow(){
        Date now = new Date();
        return now.getTime();   
    }
    
    public long getElapsedTime(){
        // in millisec
        Date now = new Date();
        elapsedTime = now.getTime() - startTime ;
        return elapsedTime;
    } 
    
    public DaqTime(){
    }
}