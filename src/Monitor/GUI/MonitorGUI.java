/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Monitor.GUI;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Common.Entities.EComputationPayload;
import Common.Entities.EServiceNode;
import Common.Enums.EStatus;
import Monitor.Entities.EMonitor;

/**
 *
 * @author doctortux
 */
public class MonitorGUI extends javax.swing.JFrame {

    private EMonitor monitor;

    private final Map<Integer, PanelMonitor> lbPanels= new HashMap<Integer, PanelMonitor>();
    private final Map<Integer, PanelMonitor> servicesPanels= new HashMap<Integer, PanelMonitor>();
    private final Map<Integer, PanelMonitor> clientPanels= new HashMap<Integer, PanelMonitor>();

    private final ArrayList<Integer> lbTabs = new ArrayList<>();
    private final ArrayList<Integer> serviceTabs = new ArrayList<>();
    private final ArrayList<Integer> clientTabs = new ArrayList<>();

    /**
     * Creates new form MonitorGUI
     */
    public MonitorGUI(EMonitor monitor) {
        initComponents();
        this.monitor=monitor;
        this.setVisible(true);
        this.setTitle("Monitor");
    }

    public void addService(EServiceNode node){
        PanelMonitor p = new PanelMonitor();
        switch(node.getServiceName()){
            case "LoadBalancer":
                lbPanels.put(node.getID(), p);
                PLoadBalancer.addTab("LB"+node.getID(), p);
                lbTabs.add(node.getID());
                break;
            case "Computation":
                servicesPanels.put(node.getID(),p);
                PService.addTab("Pi"+node.getID(), p);
                serviceTabs.add(node.getID());
                break;
            case "Client":
                clientPanels.put(node.getID(),p);
                PClient.addTab("Client"+node.getID(), p);
                clientTabs.add(node.getID());
                break;
        }
    }

    public void removeService(EServiceNode node){
        switch(node.getServiceName()){
            case "LoadBalancer":
                lbPanels.remove(node.getID());
                PLoadBalancer.removeTabAt(lbTabs.indexOf(node.getID()));
                lbTabs.remove(node.getID());
                break;
            case "Computation":
                servicesPanels.remove(node.getID());
                PService.removeTabAt(serviceTabs.indexOf(node.getID()));
                serviceTabs.remove(node.getID());
                break;
            case "Client":
                clientPanels.remove(node.getID());
                PClient.removeTabAt(clientTabs.indexOf(node.getID()));
                clientTabs.remove(node.getID());
                break;
        }
    }

    public void addRequest(EServiceNode node, EComputationPayload payload){
        Object[] data;
        switch(payload.getCode()){
            case 2:
                data=new Object[]{payload.getRequestID(),payload.getClientID(),payload.getServerID(),payload.getIteractions(),payload.getPI(),payload.getDeadline(),payload.getCode()};
                break;
            case 3:
                data=new Object[]{payload.getRequestID(),payload.getClientID(),payload.getServerID(),payload.getIteractions(),-1,payload.getDeadline(),payload.getCode()};
                break;
            default:
                data=new Object[]{payload.getRequestID(),payload.getClientID(),"-",payload.getIteractions(),"-",payload.getDeadline(),payload.getCode()};
                break;
        }
        switch(node.getServiceName()){
            case "LoadBalancer":
                lbPanels.get(node.getID()).addRequest(data);
                break;
            case "Computation":
                servicesPanels.get(node.getID()).addRequest(data);
                break;
            case "Client":
                clientPanels.get(node.getID()).addRequest(data);
                break;
        }
    }

    public void updateRequest(EServiceNode node, EComputationPayload payload){
        Object[] data;
        switch(payload.getCode()){
            default:
                data=new Object[]{payload.getRequestID(),payload.getClientID(),"-",payload.getIteractions(),"-",payload.getDeadline(),payload.getCode()};
                break;
            case 2:
                data=new Object[]{payload.getRequestID(),payload.getClientID(),payload.getServerID(),payload.getIteractions(),payload.getPI(),payload.getDeadline(),payload.getCode()};
                break;
            case 3:
                data=new Object[]{payload.getRequestID(),payload.getClientID(),payload.getServerID(),payload.getIteractions(),-1,payload.getDeadline(),payload.getCode()};
                break;
        }
        switch(node.getServiceName()){
            case "LoadBalancer":
                lbPanels.get(node.getID()).updateRequest(data);
                break;
            case "Computation":
                servicesPanels.get(node.getID()).updateRequest(data);
                break;
            case "Client":
                clientPanels.get(node.getID()).updateRequest(data);
                break;
        }
    }

    public void deleteRequest(EServiceNode node, EComputationPayload payload){
        switch(node.getServiceName()){
            case "LoadBalancer":
                lbPanels.get(node.getID()).deleteRequest(payload.getRequestID());
                break;
            case "Computation":
                servicesPanels.get(node.getID()).deleteRequest(payload.getRequestID());
                break;
            case "Client":
                clientPanels.get(node.getID()).deleteRequest(payload.getRequestID());
                break;
        }
    }

    public void heartBeat(EServiceNode node, EStatus status){
        PanelMonitor p=null;
        Color c=getBackgroundColor(status);
        switch(node.getServiceName()){
            case "LoadBalancer":
                p=lbPanels.get(node.getID());
                if(c!=null){
                    PLoadBalancer.setBackgroundAt(lbTabs.indexOf(node.getID()), c);
                }
                break;
            case "Computation":
                p=servicesPanels.get(node.getID());
                if(c!=null){
                    PService.setBackgroundAt(serviceTabs.indexOf(node.getID()), c);
                }
                break;
            case "Client":
                p=clientPanels.get(node.getID());
                if(c!=null){
                    PClient.setBackgroundAt(clientTabs.indexOf(node.getID()), c);
                }
                break;
        }
        p.heartbeat(status);  

    }

    public Color getBackgroundColor(EStatus status){
        switch(status){
            case notResponding:
                return Color.ORANGE;
            case active:
                return Color.GREEN;
            case stopped:
                return Color.RED;
            default:
                return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        PConfig = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        hbWSTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        hbPeriodTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        PLoadBalancer = new javax.swing.JTabbedPane();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        PService = new javax.swing.JTabbedPane();
        PClient = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Configuration");

        jLabel3.setText("Port:");

        jLabel4.setText("Heart Beat Window Size:");

        jLabel5.setText("Heart Beat Period:");

        portTextField.setText("5000");
        hbWSTextField.setText("3");
        hbPeriodTextField.setText("1000");

        jButton1.setText("Start");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PConfigLayout = new javax.swing.GroupLayout(PConfig);
        PConfig.setLayout(PConfigLayout);
        PConfigLayout.setHorizontalGroup(
            PConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PConfigLayout.createSequentialGroup()
                .addGroup(PConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PConfigLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hbWSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hbPeriodTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PConfigLayout.createSequentialGroup()
                        .addGap(326, 326, 326)
                        .addComponent(jButton1))
                    .addGroup(PConfigLayout.createSequentialGroup()
                        .addGap(325, 325, 325)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PConfigLayout.setVerticalGroup(
            PConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PConfigLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(hbWSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(hbPeriodTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(jButton1))
        );

        jLabel2.setText("Status:");

        jTextField1.setEditable(false);
        jTextField1.setBackground(new java.awt.Color(51, 255, 51));
        jTextField1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel7.setText("Requests");

        jTabbedPane1.addTab("Load Balancers", PLoadBalancer);

        jTabbedPane1.addTab("Services", PService);
        jTabbedPane1.addTab("Clients", PClient);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(PConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 495, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                                                         

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        int port=-100, hbWS=-100, hbPeriod=-100;
        try{
            port=Integer.parseInt(portTextField.getText());
            hbWS=Integer.parseInt(hbWSTextField.getText());
            hbPeriod=Integer.parseInt(hbPeriodTextField.getText());
        } catch (Exception e) {
            System.out.println("Failed to convert value to int");
        }
        try{
            if(hbPeriod!=-100){
                monitor.startMonitor(port,hbWS,hbPeriod);
            }
        } catch(Exception e){
            System.out.println("Failed to create new EMonitor instance");
        }
        
    }                                                                               

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MonitorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MonitorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MonitorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MonitorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MonitorGUI(null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JPanel PConfig;
    private javax.swing.JTabbedPane PLoadBalancer;
    private javax.swing.JTabbedPane PService;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane PClient;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField portTextField;
    private javax.swing.JTextField hbWSTextField;
    private javax.swing.JTextField hbPeriodTextField;
    // End of variables declaration                   
}
