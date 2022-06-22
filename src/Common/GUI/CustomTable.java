package Common.GUI;

import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class CustomTable extends JTable{
    
    private final ReentrantLock rl=new ReentrantLock();
    public CustomTable(){
        super();
    }

    public void addRow(Object[] data){
        try{
            rl.lock();
            DefaultTableModel model=(DefaultTableModel)this.getModel();
            model.insertRow(0,data);
        }catch(Exception e){}
        finally{
            rl.unlock();
        }
        
    }

    public void updateRow(Object[] data){
        try{
            rl.lock();
            DefaultTableModel model=(DefaultTableModel)this.getModel();
            for(int i=0;i<model.getRowCount();i++){
                if(model.getValueAt(i, 0)==data[0]){
                    for(int j=1;j<model.getColumnCount();j++){
                        model.setValueAt(data[j], i, j);
                    }
                }
            }
        }catch(Exception e){}
        finally{
            rl.unlock();
        }
    }

    public void deleteRow(Object idx){
        try{
            rl.lock();
            DefaultTableModel model=(DefaultTableModel)this.getModel();
            for(int i=0;i<model.getRowCount();i++){
                if(model.getValueAt(i, 0)==idx){
                    model.removeRow(i);
                }
            }
        }catch(Exception e){}
        finally{
            rl.unlock();
        }
        
    }

    public void deleteAllRows(){
        try{
            rl.lock();
            DefaultTableModel model=(DefaultTableModel)this.getModel();
            for(int i=0;i<model.getRowCount();i++){
                model.removeRow(i);
            }
        }catch(Exception e){}
        finally{
            rl.unlock();
        }
        DefaultTableModel model=(DefaultTableModel)this.getModel();
        for(int i=0;i<model.getRowCount();i++){
            model.removeRow(i);
        }
    }

    public void highlightRow(Object idx){
        try{
            rl.lock();
            DefaultTableModel model=(DefaultTableModel)this.getModel();
            for(int i=0;i<model.getRowCount();i++){
                if(model.getValueAt(i, 0)==idx){
                    try{
                        this.addRowSelectionInterval(i, i);
                        Thread.sleep(200);
                    }catch(Exception e){}
                    finally{
                        this.removeRowSelectionInterval(i, i);
                    }
                }
            }
        }catch(Exception e){}
        finally{
            rl.unlock();
        }
        
    }
}
