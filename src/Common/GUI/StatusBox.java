package Common.GUI;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

import Common.Enums.EStatus;

import java.awt.Color;
import java.util.concurrent.locks.ReentrantLock;

public class StatusBox extends javax.swing.JTextField{    
    //Border Settings and Colors
    private static final int borderWidth=2;
    private static final Border black=BorderFactory.createLineBorder(Color.black, borderWidth);
    private final ReentrantLock rl=new ReentrantLock();     
    
    public StatusBox(int x, int y){
        this.setHorizontalAlignment(JTextField.CENTER);
        this.setVisible(true);
        this.setBackground(Color.RED);
        this.setFocusable(false);
        this.setBorder(black);
        this.setBounds(x, y, 15, 15);
    }

    public void changeColor(EStatus c){
        try{
            rl.lock();
            switch(c){
                case heartBeat:
                    this.setBackground(Color.YELLOW);
                    try{
                        Thread.sleep(30);
                    }catch(Exception e){}
                    break;
                case active:
                    this.setBackground(Color.GREEN);
                    break;
                case stopped:
                    this.setBackground(Color.RED);
                    break;
                case notResponding:
                    this.setBackground(Color.ORANGE);
                    break;
            }
        }catch(Exception e){}
        finally{
            rl.unlock();
        }
        

    }
}
