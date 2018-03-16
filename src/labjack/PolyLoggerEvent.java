package labjack;
/*
 *  DataLoggerEvent.java
 *
 *  GUI Frame Event handler for Labjack data logger.
 *
 *  brian@omnificsolutions.com
 *  Sept. 9, 2011 updated 11/1/2012
 *          increased max RPM 
 */

import javax.swing.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.io.*;
import com.labjack.LJUD;

public class PolyLoggerEvent implements  ItemListener, ActionListener, 
    Runnable {
    
    PolyLoggerGui gui; 
    Thread running ;
    long elapsedTime, now ;
    long cycleCount = 0;
    double timeIncrMillis;
    double polyFreq = 720;
    double polySSper = 2.0;
    double polySSperMin = 0.1;  // test set low
    double polyMaxFreq = 2800;
    DaqTime time = new DaqTime();
    LabjackClass dataLog;
    double [] result = {0,0,0,0,0,0,0,0};
    boolean [] LockStatOn = {true,true,true,true,true,true,true,true};
    boolean [] LockStatOff = {false,false,false,false,false,false,false,false};
    int LJType = 0; // UE9=0, U6= 1
    double LJSerNum = 0 ;
   
    private String path;
    private WriteFile data;
    
    public PolyLoggerEvent(PolyLoggerGui in){
        gui = in;
        dataLog = new LabjackClass() ;
      if(true){  
        dataLog.getLabJack();
        LJType = dataLog.getLabJackType();
        LJSerNum = dataLog.getLabJackSerNum();
        // any global initialization goes here
      }
   
    }
    
    void openLogFile(){
        String DateOut;
        Date today ;
        // form logfile name from current time stamp
        Format formatter = new SimpleDateFormat("yyMMdd_HHmmss");
        today = new Date();
        DateOut = LJSerNum + "_"+ formatter.format(today);
        DateOut+=".txt";

        // DateOut = "logData.txt"; 
        path = DateOut;
        createLogFile(DateOut);
    } 
    
    void createLogFile(String text){
        // creates file only & adds header
        data = new WriteFile(text,true);
        String hdr = "date," + gui.getHeader() ;
        writeLogFile(String.valueOf(hdr));
    }
     
    void closeLogFile(){
  
    }
    
    void writeLogFile(String text){
          // must have already created file
          try {
               text= text + "\r\n";
               data.   writeToFile(text);
          } 
          catch (IOException e) {
              System.out.println("error writing file");
          }
       
    }
    
      void startRunning(){
        gui.run.setEnabled(false);
        gui.end.setEnabled(true);
        gui.pause.setEnabled(true);
        time.setStartTime(); 
        elapsedTime = time.getElapsedTime();   
        if(!getPolyFreq()){
            gui.freqVal.setText("ERROR!!");
        } else if (!getSSperiod()) {
            gui.perVal.setText("ERROR!!");
        } else {
            timeIncrMillis = polySSper * 30000; // min to millis
            cycleCount = 0 ;
            for(int j=0; j<8; j++)
            {
                LockStatOn[j]= true;
            }
            setPolyFreq();
            openLogFile();
            running = new Thread(this);
            running.start();
        } 
    }
      
    void endRunning(){
        gui.run.setEnabled(true);
        gui.end.setEnabled(false);
        gui.pause.setEnabled(false);
        running = null;
        setPolyAllOff() ;
        
        closeLogFile();
    }
       
    void setPause(){
        gui.run.setEnabled(true);
        gui.end.setEnabled(true);
        gui.pause.setEnabled(false);
        running = null;
    }
    
    boolean getPolyFreq(){
        // get frequency from gui
        try {
            polyFreq = Double.parseDouble(gui.freqVal.getText()); 

        } catch (Exception e){
            System.out.println("Number exception in Freq. " + e.getMessage()) ;
            return false;
        }
        
        boolean lres = true;
        if (polyFreq > polyMaxFreq){
            lres = false;
        }
        else {
            setPolyFreq();
        }
        
        return lres;
    }
    
    boolean setPolyFreq(){
        // get UI frequency value and set up Labjack on timer 0
        long val = Math.round(250000 / (2 * polyFreq));
        dataLog.setPolyClk(val) ;
       
        return true;
        
    }
    
    boolean getSSperiod(){
        // get s/s period from gui
        try {
            polySSper = Double.parseDouble(gui.perVal.getText()); 
        } 
        catch (Exception e){
            System.out.println("Number exception in s/s period " + e.getMessage()) ;
            return false;
        }
        
        boolean lres = true;
        if ( polySSper <  polySSperMin){
            lres = false;
        }
        
        return lres;
    }
    
    void getMeasure() {
         // get Labjack measurement and store data to file
         for(int j=0; j<8; j++) {
             dataLog.requestAdcChannel(j) ;
         }

         dataLog.getResults(result);
         dispResult(result);  
    }
    
    void setPolyAllOff() {
        // turn off all polygon channels (active low)
        // uses EIO0-7
        for(int j=0; j<8; j++)
        {
             dataLog.WriteEIOChannel(j,1);
             // delay to account for turn-on delay
             try 
             {
                 Thread.sleep(400); 
             } 
             catch(InterruptedException e)
             {
                 e.printStackTrace();
             } 
        }
    }
    
    void setPolyOff(int j) {
        // turn off selected polygon channel (active low)
        // uses EIO0-7
             dataLog.WriteEIOChannel(j,1);
    }
    
    void setPolyOn(int j) {
        // turn on selected polygon channel (active low)
        // uses EIO0-7
        dataLog.WriteEIOChannel(j,0);      
    }
    
    void readPolyStat( boolean [] result) {
        // read ADCinput channels 0-7
        double [] adc= {0,0,0,0,0,0,0,0} ;
        if(path != null)
        {
           // writeLogFile(String.valueOf(x));
           for(int j=0; j<8; j++){
              dataLog.requestAdcChannel(j) ;
           }
                             
            dataLog.getResults(adc);
            for(int k=0; k<8; k++) {
                result[k] = true;
                if(adc[k]>1.50) result[k]= false;
            }
        }
    }
    
    void saveResult(int [] result) {
        NumberFormat formatNum = new DecimalFormat("0.0");
        Format formatDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        // form logfile name from current time stamp
        String outLn = formatDate.format(new Date()) + ",";
        for(int i=0;i<8;i++)
        {
            outLn+= formatNum.format(result[i])+",";
        }
        writeLogFile(outLn);
    }
    
    void dispResult(double [] result ) {
        NumberFormat formatter = new DecimalFormat("0.0");
        for(int i=0;i<8;i++)
        {
            gui.cycVal[i].setText(formatter.format(result[i]));
        } 
    }
    
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if(command.equals("run")){
           startRunning();
        }
        if(command.equals("end")){
            endRunning();
            elapsedTime = 0;
        }
        if(command.equals("pause")){
            setPause();
        }
        if(command.equals("measure")){
            getMeasure();
        }
    }
    
    public void itemStateChanged(ItemEvent event) {
        Object item = event.getItem() ;
    } 
        
        
        // more...
    public void run() {
        Thread thisThread = Thread.currentThread();
        
        int state=0 ; // 1==running, 0==stopped
        boolean ET = false;
        boolean RUNmode = false ;
        int FailCnt = 0;
        int [] polyCnt = {0,0,0,0,0,0,0,0};
        
        while(running == thisThread) {
            //System.out.println(String.valueOf(time.getElapsedTime()));
            if( time.getElapsedTime()>timeIncrMillis || RUNmode==false ){
                ET = true ;
                RUNmode = true;
            }
            else ET = false;
            // -----------------------------------------------------------
            // take a measurement & save to file
            //System.out.format(" %b %d %d %n", ET, state, FailCnt);
            if(ET) {  
                // reset start time
                time.setStartTime();
                switch (state) {
                    case 0:
                        // set state to ON
                        state = 0 ;
                        // read status of tach (using AIN0-7) & update PassStat
                        readPolyStat(LockStatOff);
                        // enable polygons that PASS (using EIO0-7)
                        FailCnt = 0;
                        for(int j=0; j<8; j++) {
                            if(LockStatOn[j]==false || LockStatOff[j]==true) {
                                FailCnt++;
                                setPolyOff(j);
                            }
                            if(LockStatOn[j]==true && LockStatOff[j]==false) {
                                setPolyOn(j);
                                polyCnt[j]++;
                            }
                            // delay to reduce current surge
                            try {
                                Thread.sleep(5000); // let start current decay
                            } 
                            catch(InterruptedException e) {
                                e.printStackTrace();
                            } 
                        } 
                        //System.out.format( "FailCnt %d %n", FailCnt);
                        if(FailCnt>=8){ endRunning(); }
                        break;
                    case 1:                         
                        // set state to OFF
                        state = 0 ;
                        // read status of tach (using AIN0-7) & update PassStat
                        readPolyStat(LockStatOn);
                        
                        // disable all polygons (using EIO0-7)
                        setPolyAllOff() ;                     
                        cycleCount++; 
                        
                        for(int j=0; j<8; j++) {
                            if(LockStatOn[j]) {
                                gui.cycVal[j].setText(String.valueOf(polyCnt[j]));
                            }
                        }                       
                        // update logging
                        saveResult(polyCnt);
                }
            }                  
        //
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
  
}