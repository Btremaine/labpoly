package labjack;
/*
 *  LabjackClass.java
 *
 *  Basic command/response for Labjack UE9/U6 using the UD driver with the 
 *  jna/ljud.jar wrapper.
 *
 *  LabPoly uses EIO pin for polygon start/stop and uses ADC inputs to read
 *  level on polygon 'LOCK' pin. Code handles 8 polygons.
 *
 *  brian@tremaineConsultingGroup.com dba Omnific Solutions
 *  Mar 6, 2018
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.labjack.LJUD;
import com.labjack.LJUDException;

public class LabjackClass {
    IntByReference refHandle = new IntByReference(0);
    int intHandle = 0;
    int LJType = 1;            // 0= UE9, 1=U6
    double LJSerNum = 0 ;    // device serial number
    
  
    //This is our simple error handling function that is called after every UD
    //function call.  This function displays the errorcode and string description
    //of the error.  It also has an iteration input is useful when processing
    //results in a loop (getfirst/getnext).  A stack trace is also displayed so
    //that you can find the line of code where the error occurred.
    public static void ErrorHandler(int Errorcode, int Iteration, Exception excep) {
        Pointer errorStringPtr = new Memory(256);
        
        if (Errorcode != LJUD.Errors.NOERROR.getValue()) {
            LJUD.errorToString(Errorcode, errorStringPtr);
            System.out.println();
            System.out.println("Warning: " + errorStringPtr.getString(0).trim());
            System.out.println("Iteration = " + Iteration);
            System.out.println("Stack Trace : ");
            if(Errorcode > LJUD.Errors.MIN_GROUP_ERROR.getValue()) {
                //Quit if this is a group error.
                System.exit(1);
            }
        }
    }
    
    private void checkForWarning(int error) {
	Pointer errorStringPtr = new Memory(256);
	if(error < 0) {
            LJUD.errorToString(error, errorStringPtr);
            System.out.println("Warning: " + errorStringPtr.getString(0).trim());
            }
	}

    // methods follow 
    
    public void getLabJack() {
        int intErrorcode;
        boolean isDone;
        
	// Open the first found LabJack U6 or UE9.
        try {
            intErrorcode = LJUD.openLabJack(LJUD.Constants.dtUE9, LJUD.Constants.ctUSB, "1", 1, refHandle);
            checkForWarning(intErrorcode);
            intHandle = refHandle.getValue();
            isDone = true;
            LJType = LJUD.Constants.dtUE9;
        }
        catch(LJUDException le ) {
            isDone = false;
            LJType = LJUD.Constants.dtU6;
            System.out.println("Labjack error didn't find UE9");
        }
       
        if (!isDone) {
            try {
                intErrorcode = LJUD.openLabJack(LJUD.Constants.dtU6, LJUD.Constants.ctUSB, "1", 1, refHandle);
                checkForWarning(intErrorcode);
                intHandle = refHandle.getValue();
                LJType = LJUD.Constants.dtU6;
            }
            catch(LJUDException le) {
                System.out.println("Labjack error can't find U6 either");
                throw le;
            }      
        } 
        if(LJType == LJUD.Constants.dtUE9)
            System.out.println("found Labjack UE9");
        else
            System.out.println("found Labjack U6");
        
        // get serial number of device
        try {
            DoubleByReference refLJSerNum = new DoubleByReference(0);
            intErrorcode = LJUD.eGet (intHandle, LJUD.Constants.ioGET_CONFIG, LJUD.Constants.chSERIAL_NUMBER, refLJSerNum, 0);
            checkForWarning(intErrorcode);
            LJSerNum = refLJSerNum.getValue();
        }
        
        catch (LJUDException le){
            System.out.println("Labjack error can't find get serial num");
            throw le;
        }
    }
    public int getLabJackType() {
        return LJType;
    }
    
    public double getLabJackSerNum() {
        return  LJSerNum;
    }
    
    public void setAdcRes(int res) {
        //Configure the analog input resolution
        int intErrorcode;
        try {
            intErrorcode = LJUD.ePut(intHandle, LJUD.Constants.ioPUT_CONFIG, LJUD.Constants.chAIN_RESOLUTION, res, 0);
            checkForWarning(intErrorcode);
            intHandle = refHandle.getValue();
        }
        catch (LJUDException le) {
            System.out.println("Labjack error setAdcRes");
            throw le;
        }
    }
    
    public void setAdcRnge(int ch, int mode) {
        //Configure the analog input range.
        int intErrorcode;
        try {
            intErrorcode = LJUD.ePut(intHandle,  LJUD.Constants.ioPUT_AIN_RANGE, ch, LJUD.Constants.rgBIP5V, 0);
            checkForWarning(intErrorcode);
            intHandle = refHandle.getValue();
        }
        catch (LJUDException le) {
            System.out.println("Labjack error setAdcRnge");
            throw le;
        }
    }
    
    public void requestAdcChannel(int ch) {
        //Request analog channel ch
        int intErrorcode;
        try {
            intErrorcode = LJUD.addRequest(intHandle, LJUD.Constants.ioGET_AIN, ch, 0, 0, 0);
            checkForWarning(intErrorcode);
            intHandle = refHandle.getValue();
        }
        catch (LJUDException le) {
            System.out.println("Labjack error requestAdcChannel");
            throw le;      
        }
    }
    
    public void WriteEIOChannel(int ch, int val) {
        //Write channel ch
        int intErrorcode;
        try {
            ch = ch&0x7;
            ch = ch + 8;
            val = val&0x01;
            intErrorcode = LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_DIGITAL_BIT, ch, val, 0, 0);
            checkForWarning(intErrorcode);
            intHandle = refHandle.getValue();
            
            intErrorcode = LJUD.goOne(intHandle);
            checkForWarning(intErrorcode);

        }
        catch(LJUDException le) {
            System.out.println("Labjack error WriteEIOChannel");
            throw le; 
        }
     }
    
    public void setPolyClk(long val) {
        int intErrorcode;
        
	IntByReference refIOType = new IntByReference(0);
	IntByReference refChannel = new IntByReference(0);
	DoubleByReference refValue = new DoubleByReference(0.0);
        IntByReference dummyInt = new IntByReference(0);
	DoubleByReference dummyDouble = new DoubleByReference(0.0);  
        
        try {
            // configure timer 0 for frequency mode, set the value and enable
            if(LJType == LJUD.Constants.dtUE9) {
            //Set the timer clock base to 750Khz in UE9
                LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_CONFIG, LJUD.Constants.chTIMER_CLOCK_BASE, LJUD.Constants.tc750KHZ, 0, 0);
       
                //Set the timer clock divisor to 3, creating a 250kHz timer clock.
                LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_CONFIG, LJUD.Constants.chTIMER_CLOCK_DIVISOR, 3, 0, 0);
            }
            else if(LJType == LJUD.Constants.dtU6){
                //Set the timer clock base to 1MHz in U6
                LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_CONFIG, LJUD.Constants.chTIMER_CLOCK_BASE, LJUD.Constants.tc1MHZ_DIV, 0, 0);
       
                //Set the timer clock divisor to 4, creating a 250kHz timer clock.
                LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_CONFIG, LJUD.Constants.chTIMER_CLOCK_DIVISOR, 4, 0, 0); 
            }
            
            //Enable 0 timer.  It will use FIO0.
            LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_CONFIG, LJUD.Constants.chNUMBER_TIMERS_ENABLED, 1, 0, 0);
        
            //Configure Timer0 as frequency output.
            LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_TIMER_MODE, 0, LJUD.Constants.tmFREQOUT, 0, 0);
        
            //Initialize frequency output at 250kHz/(2*val) = polyFreq
            LJUD.addRequest (intHandle, LJUD.Constants.ioPUT_TIMER_VALUE, 0, val, 0, 0);  
                       
            //Execute the requests.
            intErrorcode = LJUD.goOne(intHandle);
            checkForWarning(intErrorcode);
        
            //Get all the results just to check for errors.
            intErrorcode = LJUD.getFirstResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);
            checkForWarning(intErrorcode);
                
            intErrorcode = LJUD.getNextResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);                
            intErrorcode = LJUD.getNextResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);
            intErrorcode = LJUD.getNextResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);
            intErrorcode = LJUD.getNextResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);   
         }
        catch(LJUDException le) {
            throw le;
        }
    }
  
    public void getResults(double [] result) {	
    /* Perform goOne() and get adc values */
        int intErrorcode;
	IntByReference refIOType = new IntByReference(0);
	IntByReference refChannel = new IntByReference(0);
	DoubleByReference refValue = new DoubleByReference(0.0);
        IntByReference dummyInt = new IntByReference(0);
	DoubleByReference dummyDouble = new DoubleByReference(0.0);  
        
        //Execute the requests.
        try {
            intErrorcode = LJUD.goOne(intHandle);
            checkForWarning(intErrorcode);
        }
        
        catch (LJUDException le) {
            System.out.println("Labjack error getResults");
            throw le; 
        }
                 
        //Get all the results.  The input measurement results are stored.  All other
        //results are for configuration or output requests so we are just checking
        //whether there was an error.
        try {
            intErrorcode = LJUD.getFirstResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);
            checkForWarning(intErrorcode);  
            if(refIOType.getValue() == LJUD.Constants.ioGET_AIN) {
                    for(int k=0; k<10; k++) {
                        if(refChannel.getValue() == k) result[k]= refValue.getValue();
                    }
            }
        }
        
        catch(LJUDException le) {
            System.out.println("Labjack error getResults");
            throw le;  
        }
          
        boolean isDone = false;
        while(!isDone) {
            try {            
                intErrorcode = LJUD.getNextResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);
                checkForWarning(intErrorcode);
                if(refIOType.getValue() == LJUD.Constants.ioGET_AIN) {
                    for(int k=0; k<10; k++) {
                        if(refChannel.getValue() == k) result[k]= refValue.getValue();
                    }
                }    
            }
            catch(LJUDException le) {
		if(le.getError() == LJUD.Errors.NO_MORE_DATA_AVAILABLE.getValue()) {
                    isDone = true;
		}
		else {
                    throw le;
		}
            }   
        }   
    }
    
}

