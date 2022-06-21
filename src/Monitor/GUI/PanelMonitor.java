package Monitor.GUI;

import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import Common.Entities.EMessage;
import Common.Enums.EStatus;
import Common.GUI.CustomTable;
import Common.GUI.StatusBox;

public class PanelMonitor extends JPanel {

    private CustomTable table;
    private StatusBox statusBox;
    private JLabel statusLabel = new JLabel();
    private JLabel requestsLabel = new JLabel();
    private Object[][] data={};
    private ReentrantLock rl = new ReentrantLock();

    public PanelMonitor(){
        this.setBackground(new java.awt.Color(255, 255, 255));
        this.setLayout(null);

        statusLabel.setText("Status:");
        statusLabel.setBounds(290,10,50,30);
        this.add(statusLabel);

        statusBox=new StatusBox(340, 20);
        this.add(statusBox);

        requestsLabel.setText("Requests");
        requestsLabel.setBounds(310,50,80,30);
        this.add(requestsLabel);

        table=new CustomTable();
        table.setEnabled(false);
        table.setModel(new javax.swing.table.DefaultTableModel(data, new String[] {"RequestID","ClientID","ServerID","Status"}));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20,100,615,300);
        this.add(scrollPane);
        scrollPane.setViewportView(table);
        table.setFillsViewportHeight(true);
    }

    public void addRequest(Object[] data){
        table.addRow(data);
    }

    public void updateRequest(Object[] data){
        table.updateRow(data);
    }

    public void deleteRequest(Object idx){
        table.deleteRow(idx);
    }

    public void highlightRow(EMessage message){
        table.highlightRow(message);
    }
    
    public void heartbeat(EStatus status){
        try{
            rl.lock();
            statusBox.changeColor(status);
        }catch(Exception e){
            rl.unlock();
        }
    }
}
