package Common.GUI;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Common.Entities.EMessage;

public class CustomTable extends JTable {

    private int index;
    
    public CustomTable(){
        super();
    }

    public void addRow(EMessage message){
        DefaultTableModel model=(DefaultTableModel)this.getModel();
        Object[] data={index++,"idk","idk","idk"};
        model.insertRow(0,data);
    }

    public void deleteRow(EMessage message){
        DefaultTableModel model=(DefaultTableModel)this.getModel();
        for(int i=0;i<model.getRowCount();i++){
            if(model.getValueAt(i, 0)=="0"){
                model.removeRow(i);
            }
        }
    }

    public void highlightRow(EMessage message){
        DefaultTableModel model=(DefaultTableModel)this.getModel();
        for(int i=0;i<model.getRowCount();i++){
            if((int)model.getValueAt(i, 0)==1){
                try{
                    this.addRowSelectionInterval(i, i);
                    Thread.sleep(200);
                }catch(Exception e){}
                finally{
                    this.removeRowSelectionInterval(i, i);
                }
            }
        }
    }
}
