package com.shubh;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;


/**
 * CLass MqCommunicationUtilApplication
 * */
@SpringBootApplication
public class MqCommunicationUtilApplication extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private MQAccessGateway mqAccessGateway = null;
	private MQQueueManager queueManager = null;
	
	public MqCommunicationUtilApplication() {
        initUI();
    }

	public static void main(String[] args) {
		 ConfigurableApplicationContext ctx = new SpringApplicationBuilder(MqCommunicationUtilApplication.class)
	                .headless(false).run(args);

		EventQueue.invokeLater(() -> {
			MqCommunicationUtilApplication ex = ctx.getBean(MqCommunicationUtilApplication.class);
			ex.setVisible(true);
		});
	}

	private void initUI() {
		createLayout();
	}

	private void createLayout(JComponent... arg) {
		Container pane = getContentPane();
		GridLayout gridLayout = new GridLayout(4,2, 10, 10);
		pane.setLayout(gridLayout);
		JLabel lblIP = new JLabel("MQ Server IP*");
		lblIP.setSize(100, 20);
		JLabel lblPort = new JLabel("MQ Server Port*");
		lblPort.setSize(100, 20);
		JLabel lblChannel = new JLabel("MQ Server Channel*");
		lblChannel.setSize(100, 20);
		JTextField txtIP = new JTextField(); 
		txtIP.setSize(300, 20);
		JTextField txtPort = new JTextField(); 
		txtPort.setSize(300, 20);
		JTextField txtChannel = new JTextField(); 
		txtChannel.setSize(300, 20);
		JLabel txtChannel1 = new JLabel();
		txtChannel1.setSize(100, 20);
		JButton btn = new JButton("Connect");
		btn.setSize(300, 20);
		pane.add(lblIP);
		pane.add(txtIP);
		pane.add(lblPort);
		pane.add(txtPort);
		pane.add(lblChannel);
		pane.add(txtChannel);
		pane.add(txtChannel1);
		pane.add(btn);
		
		setTitle("IBM MQ Util");
		setSize(400, 160);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
				
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String ip = txtIP.getText();
					int port = Integer.parseInt(txtPort.getText());
					String channel = txtChannel.getText();
					queueManager = getMQConnection(pane, ip, port, channel);
					btn.setEnabled(false);
					txtIP.setEnabled(false);
					txtPort.setEnabled(false);
					txtChannel.setEnabled(false);
					getOrPutMessage(btn, txtIP, txtPort, txtChannel);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(pane, "Invalid Port Number", "Error", JOptionPane.ERROR_MESSAGE);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
	}
	
	private MQQueueManager getMQConnection(Component parent, String ip, int port, String channel) throws Exception {
		mqAccessGateway = new MQAccessGatewayImpl(ip, port, channel);
		try {
			MQQueueManager manager = mqAccessGateway.getMQQueueManager("");
			return manager;
		} catch (MQException e) {
			if(e.reasonCode == 2009) 
				JOptionPane.showMessageDialog(parent, "Unable to connect MQ(MQ Reason code 2009)", "Error", JOptionPane.ERROR_MESSAGE);
			else if(e.reasonCode == 2035)
				JOptionPane.showMessageDialog(parent, "Authentication Error(MQ Reason code 2035)", "Error", JOptionPane.ERROR_MESSAGE);
			else if(e.reasonCode == 2035)
				JOptionPane.showMessageDialog(parent, "Host Not Available Error(MQ Reason code 2538)", "Error", JOptionPane.ERROR_MESSAGE);
			else 
				JOptionPane.showMessageDialog(parent, "Unknown Error(MQ Reason code " + e.reasonCode + ")", "Error", JOptionPane.ERROR_MESSAGE);
			throw new Exception();
		}
	}
	
	private void getOrPutMessage(JButton btn, JTextField txtIP, JTextField txtPort, JTextField txtChannel) {
		JDialog dialog = new JDialog();
		dialog.setSize(400, 160);
		dialog.setLocationRelativeTo(null);
		dialog.setTitle("IBM MQ Util");
		
		JButton btnPut = new JButton("Put Message");
		JButton btnGet = new JButton("Get Message");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 85, 10, 10));
		buttonPane.add(Box.createHorizontalBox());
		buttonPane.add(btnPut);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(btnGet);
		
		dialog.add(buttonPane, BorderLayout.CENTER);
		dialog.setVisible(true);
		
		btnPut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createDailog("PUT");
			}
		});
		
		btnGet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createDailog("GET");
			}
		});
		
	}
	
	private void createDailog(String action) {
		JDialog dialogPut = new JDialog();
		dialogPut.setSize(400, 600);
		dialogPut.setLocationRelativeTo(null);
		if("PUT".equals(action))
			dialogPut.setTitle("Put Message");
		else
			dialogPut.setTitle("Get Message");
		
		JLabel lblQueueName = new JLabel("Queue Name*");
		JTextField txtQueueName = new JTextField();
		JPanel queuePane = new JPanel();
		queuePane.setLayout(new BoxLayout(queuePane, BoxLayout.LINE_AXIS));
		queuePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		queuePane.add(Box.createHorizontalBox());
		queuePane.add(lblQueueName);
		queuePane.add(Box.createRigidArea(new Dimension(10, 0)));
		queuePane.add(txtQueueName);
		
		JTextArea txtMsg = new JTextArea();
		txtMsg.setSize(400, 400);
		if("PUT".equals(action)) {
			txtMsg.setToolTipText("Enter Message");
		}
		
		JButton putMsg = new JButton();
		putMsg.setAlignmentX(RIGHT_ALIGNMENT);
		if("PUT".equals(action))
			putMsg.setText("Put Message");
		else
			putMsg.setText("Get Message");
		
		
		JPanel buttonPane1 = new JPanel();
		buttonPane1.setLayout(new BoxLayout(buttonPane1, BoxLayout.PAGE_AXIS));
		buttonPane1.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane1.add(Box.createHorizontalBox());
		buttonPane1.add(txtMsg);
		
		dialogPut.add(queuePane, BorderLayout.PAGE_START);
		dialogPut.add(buttonPane1);
		dialogPut.add(putMsg, BorderLayout.PAGE_END);
		dialogPut.setVisible(true);
		
		putMsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if("PUT".equals(action)) {
					String message = txtMsg.getText();
					String queueName = txtQueueName.getText();
					if(queueName == null || queueName.isEmpty()) {
						JOptionPane.showMessageDialog(dialogPut, "Please enter queue name", "Error", JOptionPane.ERROR_MESSAGE);
					}if(message == null || message.isEmpty())
						JOptionPane.showMessageDialog(dialogPut, "Please enter a message", "Error", JOptionPane.ERROR_MESSAGE);
					else {
						try {
							mqAccessGateway.putMessage(queueManager, queueName, message);
							JOptionPane.showMessageDialog(dialogPut, "Message inserted successfully", "Success", JOptionPane.PLAIN_MESSAGE);
						} catch (MQException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(dialogPut, "Unable to put message(MQ Reason Code " + e1.reasonCode +")", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				} else {
					String queueName = txtQueueName.getText();
					if(queueName == null || queueName.isEmpty()) {
						JOptionPane.showMessageDialog(dialogPut, "Please enter queue name", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						try {
							String msg = mqAccessGateway.getMessage(queueManager, queueName);
							txtMsg.setText(msg);
							JOptionPane.showMessageDialog(dialogPut, "Message retrieved successfully", "Success", JOptionPane.PLAIN_MESSAGE);
						} catch (MQException e1) {
							e1.printStackTrace();
							if(e1.reasonCode == 2033)
								JOptionPane.showMessageDialog(dialogPut, "No message found", "Success", JOptionPane.PLAIN_MESSAGE);
							else
								JOptionPane.showMessageDialog(dialogPut, "Unable to get message(MQ Reason Code " + e1.reasonCode +")", "Error", JOptionPane.ERROR_MESSAGE);
						} catch (Exception e2) {
						}
					}
				}
			}
		});
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(mqAccessGateway != null && queueManager != null)
			mqAccessGateway.closeMQQueueManager(queueManager);
	}
}
