package Common.GUI;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

import Common.Enums.EStatus;

import java.awt.Color;

public class StatusBox extends javax.swing.JTextField{    
    //Border Settings and Colors
    private static final int borderWidth=2;
    private static final Border black=BorderFactory.createLineBorder(Color.black, borderWidth);;
    private static final Border red = BorderFactory.createLineBorder(Color.red,borderWidth);     
    
    public StatusBox(int x, int y){
        this.setHorizontalAlignment(JTextField.CENTER);
        this.setBackground(Color.GREEN);
        this.setFocusable(false);
        this.setBorder(black);
        this.setBounds(x, y, 15, 15);
    }

    public void changeColor(EStatus c){
        switch(c){
            case heartBeat:
                this.setBackground(Color.YELLOW);
                try{
                    Thread.sleep(80);
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

    }
}
