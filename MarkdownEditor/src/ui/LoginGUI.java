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
    JButton login = new JButton("登录");  
    JButton register = new JButton("注册");  
    JLabel  name = new JLabel("用户名：");  
    JLabel password = new JLabel("密码：");   
    JTextField JName = new JTextField(10); //明文账号输入  
    JPasswordField JPassword = new JPasswordField(10); // 非明文密码输入；  
      
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
        jp.setLayout(new GridLayout(3,2));  //3行2列的面板jp（网格布局）  
          
        name.setHorizontalAlignment(SwingConstants.RIGHT);  //设置该组件的对齐方式为向右对齐  
        password.setHorizontalAlignment(SwingConstants.RIGHT);  
          
        jp.add(name);   //将内容加到面板jp上  
        jp.add(JName);    
        jp.add(password);  
        jp.add(JPassword);    
        jp.add(login);  
        jp.add(register);  
          
        login.addActionListener(this); //登录增加事件监听  
        register.addActionListener(this);   //退出增加事件监听  
          
        this.add(jp,BorderLayout.CENTER);   //将整块面板定义在中间  
          
        this.setTitle("登录");  
        this.pack();
        this.setLocation(500,300);  //设置初始位置   
    }
    
    //事件处理
    public void actionPerformed(ActionEvent e) 
    {  
    	Object source = e.getSource();
    	
    	//登录
        if(source == register) {
        	try {
        		//登录成功，就唤醒主界面线程
        		if( attemptRegister() ) {
        			mCallThread.interrupt();
        			setVisible(false);
        			
        			Utility.info("注册成功！");
        		};
        	} catch(Exception exc) {
        		Utility.error(exc.getMessage());
        	}
        }
        //登录
        else {
        	try {
        		//登录成功，就唤醒主界面线程
        		if( attemptLogin() ) {
        			mCallThread.interrupt();
        			setVisible(false);
        			
        			Utility.info("登录成功！");
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
     * 检查用户名或密码格式是否正确
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
     * 尝试登录
     * @return
     * @throws Exception
     */
    private boolean attemptLogin() throws Exception {
    	String name = JName.getText();
    	String password = new String(JPassword.getPassword());
    	
    	//用户名或密码格式有误，不能包含 #
    	if(!isValid(name) || !isValid(password)) {
    		throw new Exception(SignInfo.INVALID_VALUE.toString());
    	}
    	
    	//试图连接服务器
    	mClient = new Client();
    		
    	try {
    		mClient.connectServer();
    	} catch(Exception e) {
    		e.printStackTrace();
    		throw new Exception("连接服务器失败！");
    	}
    	
    	mClient.disposeRequest(RequestType.LOGIN, name, password);
    	
    	return true;
    }
    
    /**
     * 尝试注册
     * @return
     * @throws Exception
     */
    private boolean attemptRegister() throws Exception {
    	String name = JName.getText();
    	String password = new String(JPassword.getPassword());
    	
    	//用户名或密码格式有误，不能包含 #
    	if(!isValid(name) || !isValid(password)) {
    		throw new Exception(SignInfo.INVALID_VALUE.toString());
    	}
    	
    	//试图连接服务器
    	mClient = new Client();
    		
    	try {
    		mClient.connectServer();
    	} catch(Exception e) {
    		e.printStackTrace();
    		throw new Exception("连接服务器失败！");
    	}
    	
    	String request = RequestType.REGISTER.ordinal() + "#" + name + password;
    	mClient.disposeRequest(RequestType.REGISTER, name, password);
    	
    	return true;
    }
}  










