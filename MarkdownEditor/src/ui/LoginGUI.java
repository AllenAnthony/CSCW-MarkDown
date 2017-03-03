package ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
  
import javax.swing.JButton;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
import javax.swing.JOptionPane;  
import javax.swing.JPanel;  
import javax.swing.JPasswordField;  
import javax.swing.JTextField;  
import javax.swing.SwingConstants;

import client.Client;
import util.RequestType;
import util.SignInfo;
import util.Utility;

public class LoginGUI extends JFrame implements ActionListener  
{  
    JButton login = new JButton("��¼");  
    JButton register = new JButton("ע��");  
    JLabel  name = new JLabel("�û�����");  
    JLabel password = new JLabel("���룺");   
    JTextField JName = new JTextField(10); //�����˺�����  
    JPasswordField JPassword = new JPasswordField(10); // �������������룻  
      
    private Client mClient = null;
    
    private EditorGUI mContext;
    private Thread mCallThread;
    
    public LoginGUI(EditorGUI context, Thread callThread) {  
    	setGUI();
    	mContext = context;
    	mCallThread = callThread;
    }  
    
    private void setGUI() {
    	login.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
    	register.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
    	name.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
    	password.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
    	JName.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
    	JPassword.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
    	
        JPanel jp = new JPanel();  
        jp.setLayout(new GridLayout(3,2));  //3��2�е����jp�����񲼾֣�  
          
        name.setHorizontalAlignment(SwingConstants.RIGHT);  //���ø�����Ķ��뷽ʽΪ���Ҷ���  
        password.setHorizontalAlignment(SwingConstants.RIGHT);  
          
        jp.add(name);   //�����ݼӵ����jp��  
        jp.add(JName);    
        jp.add(password);  
        jp.add(JPassword);    
        jp.add(login);  
        jp.add(register);  
          
        login.addActionListener(this); //��¼�����¼�����  
        register.addActionListener(this);   //�˳������¼�����  
          
        this.add(jp,BorderLayout.CENTER);   //��������嶨�����м�  
          
        this.setTitle("��¼");  
        this.pack();
        this.setLocation(500,300);  //���ó�ʼλ��   
    }
    
    //�¼�����
    public void actionPerformed(ActionEvent e) 
    {  
    	Object source = e.getSource();
    	
    	//��¼
        if(source == register) {
        	try {
        		//��¼�ɹ����ͻ����������߳�
        		if( attemptRegister() ) {
        			mCallThread.interrupt();
        			setVisible(false);
        			
        			Utility.info("ע��ɹ���");
        		};
        	} catch(Exception exc) {
        		Utility.error(exc.getMessage());
        	}
        }
        //��¼
        else {
        	try {
        		//��¼�ɹ����ͻ����������߳�
        		if( attemptLogin() ) {
        			mCallThread.interrupt();
        			setVisible(false);
        			
        			Utility.info("��¼�ɹ���");
        		};
        	} catch(Exception exc) {
        		Utility.error(exc.getMessage());
        	}
        }
    }  
    
    public Client getClient() {
    	return mClient;
    }
    
    /**
     * ����û����������ʽ�Ƿ���ȷ
     * @param name
     * @return
     */
    private boolean isValid(String name) {
    	if(name.equals("") || name.equals("") 
    			|| name.contains("*") || name.contains("*")) {
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * ���Ե�¼
     * @return
     * @throws Exception
     */
    private boolean attemptLogin() throws Exception {
    	String name = JName.getText();
    	String password = new String(JPassword.getPassword());
    	
    	//�û����������ʽ���󣬲��ܰ��� #
    	if(!isValid(name) || !isValid(password)) {
    		throw new Exception(SignInfo.INVALID_VALUE.toString());
    	}
    	
    	//��ͼ���ӷ�����
    	mClient = new Client();
    		
    	try {
    		mClient.connectServer();
    	} catch(Exception e) {
    		e.printStackTrace();
    		throw new Exception("���ӷ�����ʧ�ܣ�");
    	}
    	
    	mClient.disposeRequest(RequestType.LOGIN, name, password);
    	
    	return true;
    }
    
    /**
     * ����ע��
     * @return
     * @throws Exception
     */
    private boolean attemptRegister() throws Exception {
    	String name = JName.getText();
    	String password = new String(JPassword.getPassword());
    	
    	//�û����������ʽ���󣬲��ܰ��� #
    	if(!isValid(name) || !isValid(password)) {
    		throw new Exception(SignInfo.INVALID_VALUE.toString());
    	}
    	
    	//��ͼ���ӷ�����
    	mClient = new Client();
    		
    	try {
    		mClient.connectServer();
    	} catch(Exception e) {
    		e.printStackTrace();
    		throw new Exception("���ӷ�����ʧ�ܣ�");
    	}
    	
    	String request = RequestType.REGISTER.ordinal() + "#" + name + password;
    	mClient.disposeRequest(RequestType.REGISTER, name, password);
    	
    	return true;
    }
}  










