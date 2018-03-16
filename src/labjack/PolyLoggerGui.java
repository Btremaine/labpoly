package labjack;
/*
 *  PolyLogger.java
 *
 *  GUI setuo for Polygon life tester data logger.
 * 
 * Use timer 0 for polygon clock
 * use 
 *
 *  brian@omnificsolutions.com
 *  Sept. 4, 2011
 */

import java.awt.*;
import javax.swing.*; 
public class PolyLoggerGui extends JFrame {
    
    PolyLoggerEvent DLevent = new PolyLoggerEvent(this);
    
    // set up GUI variables
    private String header;   // logfile header
    private static final int CHNUM = 8;
    
    int LJType = 0 ;
    double LJsnum = 0;
    
    JLabel DevType0 = new JLabel("Dev Type: UE9",JLabel.RIGHT);
    JLabel DevType1 = new JLabel("Dev Type: U6",JLabel.RIGHT);
    
    
    JPanel row1 = new JPanel();
    JLabel chNum = new JLabel("chan #",JLabel.RIGHT);
    JLabel ch1Label = new JLabel("ch1",JLabel.CENTER);
    JLabel ch2Label = new JLabel("ch2",JLabel.CENTER);
    JLabel ch3Label = new JLabel("ch3",JLabel.CENTER);
    JLabel ch4Label = new JLabel("ch4",JLabel.CENTER);
    JLabel ch5Label = new JLabel("ch5",JLabel.CENTER);
    JLabel ch6Label = new JLabel("ch6",JLabel.CENTER);
    JLabel ch7Label = new JLabel("ch7",JLabel.CENTER);
    JLabel ch8Label = new JLabel("ch8",JLabel.CENTER);
    
    JPanel row2 = new JPanel();
    JLabel serLab = new JLabel("serial #",JLabel.RIGHT);
    JTextField[] SerNum = new JTextField[10]; 
    
    JPanel row3 = new JPanel();
    JLabel cycLab = new JLabel("s/s cycles",JLabel.RIGHT);
    JTextField[] cycVal = new JTextField[10];
      
    JLabel blank = new JLabel(" ");
    
    JLabel freqLab = new JLabel("Clk Freq [Hz]",JLabel.RIGHT);
    JTextField freqVal = new JTextField("720",10);
    JLabel perLab = new JLabel("s/s period [min]",JLabel.RIGHT);
    JTextField perVal = new JTextField("2",10);
    JButton pause = new JButton("pause");
    JButton run = new JButton("run");
    JButton end = new JButton("end");
    
    JPanel P1= new JPanel();
    JPanel P2= new JPanel();
    JPanel P3= new JPanel();
    JPanel P4= new JPanel();
    
    JTextField LJ_snum = new JTextField("--",JLabel.RIGHT);
    
    public PolyLoggerGui() {
        super("LabJack PolyLogger 1.4");
        LJType = DLevent.dataLog.getLabJackType() ; // UE(=0, U6=1
        LJsnum = DLevent.dataLog.getLabJackSerNum() ;
                
        setSize(800,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridLayout container = new GridLayout(2,1,20,20);
        setLayout(container);
        
        // add listeners for buttons
        pause.addActionListener(DLevent);
        run.addActionListener(DLevent);
        end.addActionListener(DLevent);
        
        
        
        // GUI layout setup
        GridLayout layout1 = new GridLayout(1,11,10,10);
        row1.setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
        
        GridLayout layoutP1 = new GridLayout(3,1,10,10);
        //P1.setBorder(BorderFactory.createRaisedBevelBorder());    
        //row1.setPreferredSize(20,20);
        row1.setLayout(layout1);
        row1.add(chNum);
        row1.add(ch1Label) ;
        row1.add(ch2Label) ;
        row1.add(ch3Label) ;
        row1.add(ch4Label) ;
        row1.add(ch5Label) ;
        row1.add(ch6Label) ;
        row1.add(ch7Label) ;
        row1.add(ch8Label) ;
        
        P1.setLayout(layoutP1);
        P1.add(row1);
        
        // display serial # for 10 channels
        GridLayout layout2 = new GridLayout(1,11,10,10);
        row2.setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
        row2.setLayout(layout2);
        row2.add(serLab);
        for (int i=0; i<CHNUM; i++){
            SerNum[i]= new JTextField();
            SerNum[i].setText("-");
            SerNum[i].setHorizontalAlignment(JTextField.CENTER);
            row2.add(SerNum[i]) ;
        } 
        P1.add(row2);
        
        // display s/s cycles for 10 channels
        GridLayout layout3 = new GridLayout(1,11,10,10);
        row3.setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
        row3.setLayout(layout3);
        row3.add(cycLab);
        for (int i=0; i<CHNUM; i++){
            cycVal[i]= new JTextField();
            row3.add(cycVal[i]) ;
            cycVal[i].setHorizontalAlignment(JTextField.CENTER);
        }  
        
        
        P1.add(row3);
        
        // freq & period settings
        GridLayout layout4 = new GridLayout(3,2,10,10);
           // borders top, Left, Bot, Right
        P2.setLayout(layout4);
        P2.setBorder(BorderFactory.createEmptyBorder(10,40,10,40));
        P2.add(freqLab);
        P2.add(freqVal);
        P2.add(perLab);
        P2.add(perVal);
        
        // display Device type & serioal number
        if(LJType==0){
             P2.add(DevType0);
        }
        else P2.add(DevType1);
        
        P2.add(LJ_snum);
        double val = LJsnum; 
        LJ_snum.setText(String.valueOf((long)val));
        
        freqVal.setHorizontalAlignment(JTextField.CENTER);
        perVal.setHorizontalAlignment(JTextField.CENTER);
                   
        // run/pause/end buttons
        GridLayout layout5 = new GridLayout(3,1,10,10);
           // borders top, Left, Bot, Right
        P3.setLayout(layout5);
        P3.setBorder(BorderFactory.createEmptyBorder(5,150,5,150));
        P3.add(run) ;
        P3.add(pause) ;
        P3.add(end) ;
       
        // main container
        GridLayout layout6 = new GridLayout(1,2,10,10);
           // borders top, Left, Bot, Right
        P4.setLayout(layout6);
        P4.add(P2);
        P4.add(P3);
        
        add(P1);
        add(P4);
        
        // initialize
        run.setEnabled(true);
        pause.setEnabled(false);
        end.setEnabled(false);
        
        setVisible(true);
    }
    
    String getHeader() {
       header = "";
       for(int i=0; i<CHNUM; i++){
           header += " " + SerNum[i].getText() + ",";
       }  
       return header ; 
    }
    
    public static void main(String[] arguments) {
        PolyLoggerGui frame = new PolyLoggerGui();
    }
    
  
}