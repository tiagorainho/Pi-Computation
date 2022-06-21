package Common.GUI;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class CustomTable extends JTable{
    
    public CustomTable(){
        super();
    }

    public void addRow(Object[] data){
        DefaultTableModel model=(DefaultTableModel)this.getModel();
        model.insertRow(0,data);
    }

    public void updateRow(Object[] data){
        DefaultTableModel model=(DefaultTableModel)this.getModel();
        for(int i=0;i<model.getRowCount();i++){
            if(model.getValueAt(i, 0)==data[0]){
                for(int j=1;j<model.getColumnCount();j++){
                    model.setValueAt(data[j], i, j);
                }
            }
        }
    }

    public void deleteRow(Object idx){
        DefaultTableModel model=(DefaultTableModel)this.getModel();
        for(int i=0;i<model.getRowCount();i++){
            if(model.getValueAt(i, 0)==idx){
                model.removeRow(i);
            }
        }
    }

    public void highlightRow(Object idx){
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
    }
}
