package ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.TileObserver;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.docx4j.dml.Theme;

import client.Client;
import internet.MainServer;
import util.*;

/**
 * 定义markdown编辑器的界面
 *
 */
public class EditorGUI extends MouseAdapter implements ActionListener, 
DocumentListener, TreeSelectionListener {
	
	private Parser mParser = new Parser();
	
	private HtmlConverter mHtmlConverter = new HtmlConverter();
	
	private JFrame mFrame;
	private JTextArea mTextArea;
	private JEditorPane mEditorPane;
	
	private String mText = "";
	private String mHTML = "";
	private String mCSS = "";
	private boolean mTextChanged = true;
	
	private StyleSheet mStyleSheet;
	
	//菜单栏内容
	private JMenuBar mMenuBar;
	private JMenu mFileMenu, mCSSMenu, mClientMenu;
	private JMenuItem mOpenItem, mSaveItem, mExportHTMLItem, mExportDocItem, mExportPdfItem;
	private JMenuItem mLoginItem;
	private JMenuItem mEditCSSItem, mExternalCSSItem;
	
	//导航栏
	private DefaultTreeModel mTreeModel;
	private JTree mTree;
	private DefaultMutableTreeNode mRoot;
	
	//客户端
	private Client mClient = null;
	
	public static void main(String[] args) {
		EditorGUI editor = new EditorGUI();
		editor.show();
		
		MainServer server = new MainServer();	//开始主服务器运行
	}
	
	public EditorGUI() {
		setFrame();
		setMenu();
	}
	
	/**
	 * 更新UI
	 */
	private void update() {
		mTextChanged = true;
		mText = mTextArea.getText();
		
		try {
			mHTML = mParser.parseMarkdownToHTML(mText);
			mEditorPane.setText(mHTML);
		} catch(Exception e) {
			e.printStackTrace();
		}

		setTitles();
		
		System.out.println("updated");
	}
	
	/**
	 * 为目录更新标题
	 */
	private void setTitles() {
		Pattern pattern = Pattern.compile("<h(\\d)>(.*?)</h(\\d)>", Pattern.CANON_EQ);
		Matcher matcher = pattern.matcher(mHTML);
		
		mRoot.removeAllChildren();
		while(matcher.find()) {
			int rank = matcher.group(1).charAt(0) - '0';
			String title = matcher.group(2);
			
			DefaultMutableTreeNode target = mRoot;
			for(int i = 1; i < rank; i++) {
				target = (DefaultMutableTreeNode)target.getChildAt(target.getChildCount() - 1);
			}
//			target.add(new DefaultMutableTreeNode(title));
//			target.insert(new DefaultMutableTreeNode(title), target.getChildCount());
			
			//如果树展开了，不能用上面两种方法，必须用下面这种，利用TreeModel，不知道为什么
			mTreeModel.insertNodeInto(new DefaultMutableTreeNode(title), target, target.getChildCount());
		}
		//必须用这个，否则视图不更新。不知道为什么
		mTree.updateUI();
	}
	
	/**
	 * 创建一个{@link JTextArea}域，用来编辑markdown文本
	 */
	private void createTextArea() {
		mTextArea = new JTextArea();
		mTextArea.setLineWrap(true);
		Font font = new Font("Microsoft YaHei", Font.PLAIN, 18);
		mTextArea.setFont(font);
		mTextArea.getDocument().addDocumentListener(this);
		mTextArea.addMouseListener(this);
	}
	
	/**
	 * 创建一个{@link JEditorPane}域,用来显示HTML
	 */
	private void createEditorPane() {
		mEditorPane = new JEditorPane();
		mEditorPane.setContentType("text/html");
//		mEditorPane.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
		mEditorPane.setEditable(false);
		
		HTMLEditorKit ed = new HTMLEditorKit();
		mEditorPane.setEditorKit(ed);
		
		mStyleSheet = ed.getStyleSheet();
		mStyleSheet.addRule("body {font-family:\"Microsoft YaHei\", Monaco}");
		mStyleSheet.addRule("p {font-size: 14px}");
		
		try {
			mHTML = mParser.parseMarkdownToHTML(mText);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建一个{@link JTree}域，用来显示文本的树形目录结构
	 */
	private void createNavigation() {
		mRoot = new DefaultMutableTreeNode("目录");
		mTree = new JTree(mRoot);
		mTree.addTreeSelectionListener(this);
		
		mTreeModel = (DefaultTreeModel)mTree.getModel();
		
//		setTitles();
	}
	
	/**
	 * 创建一个{@link JFrame},作为主界面
	 */
	private void setFrame() {
		createTextArea();
		createEditorPane();
		createNavigation();
		
		mFrame = new JFrame();
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mFrame.setLayout(new GridBagLayout());

		{
			JScrollPane s1 = new JScrollPane(mTree);
			GridBagConstraints g1 = new GridBagConstraints();
			g1.gridx = 0;
			g1.gridy = 0;
			g1.weightx = 0;
			g1.weighty = 1;
			g1.ipadx = 150;
			g1.fill = GridBagConstraints.VERTICAL;
			mFrame.add(s1, g1);
		}
		
		{
			JScrollPane s2 = new JScrollPane(mTextArea);
			GridBagConstraints g2 = new GridBagConstraints();
			g2.gridx = 1;
			g2.gridy = 0;
			g2.weightx = 1;
			g2.weighty = 1;
			g2.ipadx = 250;
			g2.fill = GridBagConstraints.BOTH;
			mFrame.add(s2, g2);
		}
		
		{
			JScrollPane s3 = new JScrollPane(mEditorPane);
			GridBagConstraints g3 = new GridBagConstraints();
			g3.gridx = 2;
			g3.gridy = 0;
			g3.weightx = 1;
			g3.weighty = 1;
			g3.ipadx = 250;
			g3.fill = GridBagConstraints.BOTH;
			mFrame.add(s3, g3);
		}
		
		mFrame.setTitle("Markdown Editor 1.0");
		mFrame.setSize(800, 600);
		
		mFrame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				if(mClient != null) {
					try {
						mClient.disposeRequest(RequestType.CUT_CONNECT);
					} catch (Exception e1) {
						e1.printStackTrace();
						Utility.error("无法关闭客户端端口？");
					}
				}
			}
		});
	}
	
	private void setMenu() {
		mMenuBar = new JMenuBar();
		mFrame.setJMenuBar(mMenuBar);
		
		//文件菜单
		{
			mFileMenu = new JMenu("文件");
			mFileMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mMenuBar.add(mFileMenu);
			
			mOpenItem = new JMenuItem("打开");
			mOpenItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mOpenItem);
			mOpenItem.addActionListener(this);
			
			mSaveItem = new JMenuItem("保存");
			mSaveItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mSaveItem);
			mSaveItem.addActionListener(this);
			
			mExportHTMLItem = new JMenuItem("导出HTML");
			mExportHTMLItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mExportHTMLItem);
			mExportHTMLItem.addActionListener(this);
			
			mExportDocItem = new JMenuItem("导出docx");
			mExportDocItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mExportDocItem);
			mExportDocItem.addActionListener(this);
			
			mExportPdfItem = new JMenuItem("导出pdf");
			mExportPdfItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mExportPdfItem);
			mExportPdfItem.addActionListener(this);
		}
		
		//CSS菜单
		{
			mCSSMenu = new JMenu("CSS");
			mCSSMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mMenuBar.add(mCSSMenu);
			
			mEditCSSItem = new JMenuItem("加入CSS样式");
			mEditCSSItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mCSSMenu.add(mEditCSSItem);
			mEditCSSItem.addActionListener(this);
			
			mExternalCSSItem = new JMenuItem("导入外部CSS文件");
			mExternalCSSItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mCSSMenu.add(mExternalCSSItem);
			mExternalCSSItem.addActionListener(this);
		}
		
		//登录和注册菜单
		{
			mClientMenu = new JMenu("登录/注册");
			mClientMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mMenuBar.add(mClientMenu);
			
			mLoginItem = new JMenuItem("登录/注册");
			mLoginItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mClientMenu.add(mLoginItem);
			mLoginItem.addActionListener(this);
		}
	}
	
	/**
	 * 显示该界面
	 */
	public void show() {
		mFrame.setVisible(true);
	}
	
	
	//监听事件
	@Override
	public void actionPerformed(ActionEvent event) {
		Object item = event.getSource();
		
		//打开markdown文件
	    if(item == mOpenItem) {
	    	String tmp = Utility.getContentFromExternalFile();
	    	if(tmp != null) {
	    		mText = tmp;
		    	mTextArea.setText(mText);
	    	}
	    }
	    
	    //保存
	    else if(item == mSaveItem) {
	    	if(mTextChanged) {
	    		if(Utility.saveContent(mText, "md"))
	    			mTextChanged = false;
	    	}
	    }
		
		//导出HTML
	    else if(item == mExportHTMLItem) {
	    	Utility.saveContent(Utility.getStyledHTML(mHTML, mCSS), "html");
		}
	    
	    //导出docx
	    else if(item == mExportDocItem) {
	    	try {
				mHtmlConverter.saveHtmlToDocx(Utility.getStyledHTML(mHTML, mCSS));
			} catch (Exception e) {
//				e.printStackTrace();
				System.out.println("保存docx失败！");
			}
	    }
	    
	    //导出pdf
	    //有一些问题，暂时取消
	    else if(item == mExportPdfItem) {
//	    	try {
//				mHtmlConverter.saveHtmlToPdf(Utility.getStyledHTML(mHTML, mCSS));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("导出pdf失败！");
//			}
	    }
	    
	    //添加CSS
	    else if(item == mEditCSSItem) {
	    	String css = JOptionPane.showInputDialog(null, "输入你要添加的CSS样式");
	    	mStyleSheet.addRule(css);
	    	mEditorPane.setText(mHTML);
	    	mCSS += css + "\n";
	    }
	    
	    //导入外部CSS
	    else if(item == mExternalCSSItem) {
	    	String rule = Utility.getContentFromExternalFile();
	    	if(rule != null) {
		    	mStyleSheet.addRule(rule);
		    	mEditorPane.setText(mHTML);
		    	mCSS += rule + "\n";
	    	}
	    }
	    
	    //登录或注册
	    else if(item == mLoginItem) {
	    	if(mClient != null) {
	    		Utility.info("你已经登录！");
	    	}
	    	else login();
	    }
	    
	    //创建房间
	    else if(item == mCreateRoomItem) {
	    	if(mClient.getRoomID() != -1) {
	    		Utility.info("你已在房间内！");
	    	}
	    	else createRoom();
	    }
	    
	    //加入房间
	    else if(item == mJoinRoomItem) {
	    	if(mClient.getRoomID() != -1) {
	    		Utility.info("你已在房间内！");
	    	}
	    	else joinRoom();
	    }
	    
	    
	}
	
	/*
	/* 这样可以大大提升效率。不是每次输入或删除都更新UI，先sleep 1000ms，在此过程中如果用户输入又更新了，
	/* 那么又会产生新的线程，新线程会试图去阻塞旧的更新UI线程，所以旧的就作废了，
	/* 不会执行更新的方法，这样就大大减少了浪费，提高了效率。
	/* 利用invokeLater()可以在其他线程中对UI进行更改，它会把一个更新UI的任务放到EDT（事件派发线程）中，
	/* 这样就实现了在其他线程更新UI。也可以用invokeAndWait()。
	 * 最后的效果就是：如果用户一直快速地输入，那么一直都不会更新，停止输入超过1s，才会调用更新UI的方法。
	 * */
	private Thread lastThread = null;		//存储上一个要更新UI的线程，供下一个阻塞
	private void updateUIEfficiently() {
		new Thread(() -> {
			Thread last = lastThread;
			lastThread = Thread.currentThread();
			//if(isUpdating) return;
			//isUpdating = true;
			
			try {
				//阻塞上一个更新的线程
				if(last != null) {
					last.interrupt();
				}
				Thread.sleep(1000);
			} catch(InterruptedException exc) {
				return;
			}

			if(Thread.currentThread().isInterrupted()) return;
			SwingUtilities.invokeLater(() -> {update();});
			
			if(mIsHost) {
				String updation = mTextArea.getText();
				try {
					mClient.disposeRequest(RequestType.UPLOAD_UPDATION, updation);
				} catch (Exception e) {
					e.printStackTrace();
					Utility.error("与服务器端连接出现错误！");
				}
			}
			
			//isUpdating = false;
		}).start();
	}
	
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		updateUIEfficiently();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateUIEfficiently();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {

	}
	
	//检测光标位置
	@Override
	public void mouseClicked(MouseEvent e) {
		Object item = e.getSource();
		
		if(item == mTextArea) {
			int position = mTextArea.getCaretPosition();
//			System.out.println(position);
//			int max = mEditorPane.getText().length();
//			if(position >= max)
//				mEditorPane.setCaretPosition(max);
//			else
//				mEditorPane.setCaretPosition(position);
		}
			
	}

	//导航栏树选中的事件
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode) 
				mTree.getLastSelectedPathComponent();//返回最后选定的节点  
		
		String title = selectedNode.toString();
		int level = selectedNode.getLevel();
		System.out.println(level);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < level; i++)
			sb.append("#");
		sb.append(title);
		
		int pos = mText.indexOf(sb.toString());
//		mTextArea.setCaretPosition(pos);
		mTextArea.setSelectionStart(pos);
		mTextArea.setSelectionEnd(pos);
//		mEditorPane.setCaretPosition(pos);
	}
	
	
	/******************************萌萌哒分界线************************************/
	
	
	private JMenu mRoomMenu;
	private JMenuItem mCreateRoomItem, mJoinRoomItem, mExitRoomItem;
	private void setRoomMenu() {
		mRoomMenu = new JMenu("房间");
		mRoomMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mMenuBar.add(mRoomMenu);
		
		mCreateRoomItem = new JMenuItem("创建房间");
		mCreateRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mRoomMenu.add(mCreateRoomItem);
		mCreateRoomItem.addActionListener(this);
		
		mJoinRoomItem = new JMenuItem("加入房间");
		mJoinRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mRoomMenu.add(mJoinRoomItem);
		mJoinRoomItem.addActionListener(this);
		
		mExitRoomItem = new JMenuItem("退出当前房间");
		mExitRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mRoomMenu.add(mExitRoomItem);
		mExitRoomItem.addActionListener(this);
		
		mFrame.repaint();
		mFrame.revalidate();
	}
	
	/**
	 * 登录/注册
	 */
	private void login() {
		new Thread(() -> {
    		LoginGUI loginGUI = new LoginGUI(EditorGUI.this, Thread.currentThread());
	    	loginGUI.setVisible(true);
	    	
	    	try {
	    		//设置一个比较大的时间，登录不太可能超过这个时间，超过了就放弃登录了
				Thread.sleep(1000000000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				mClient = loginGUI.getClient();
				
				SwingUtilities.invokeLater(() -> {
					mFrame.setTitle("欢迎你： " + mClient.getName());
					setRoomMenu();
				});
			}
	    	
    	}).start();
	}
	
	
	private boolean mIsHost;
	
	/**
	 * 当加入一个房间后，就要开始一直监测服务器发来的信息
	 */
	private void startUpdateMonitor() {
		new Thread(() -> {
			try {
				while(true) {
					String updation = mClient.startMonitor_getUpdation();
					SwingUtilities.invokeLater(() -> {
						mTextArea.setText(updation);
					});
				}
			} catch(Exception e) {
				e.printStackTrace();
				Utility.error("与服务器连接中断！");
				return;
			}
		}).start();
	}
	
	/**
	 * 创建房间
	 */
	private void createRoom() {
		new Thread(() -> {
			try {
				mClient.disposeRequest(RequestType.CREATE_ROOM);
			} catch (Exception e) {
				e.printStackTrace();
				Utility.error(e.getMessage());
				return;
			}
			
			Utility.info("房间创建成功!你已创建房间： " + mClient.getRoomID());
			mIsHost = true;
			SwingUtilities.invokeLater(() -> {
				mFrame.setTitle(mFrame.getTitle() + "(你已在房间： " + mClient.getRoomID() + ")");
			});
			
			startUpdateMonitor();
		}).start();
	}
	
	/**
	 * 加入房间
	 */
	private void joinRoom() {
		new Thread(() -> {
			String idString = JOptionPane.showInputDialog("请输入你要加入的房间id：");
			
			try {
				mClient.disposeRequest(RequestType.JOIN_ROOM, idString);
			} catch(Exception e) {
				e.printStackTrace();
				Utility.error(e.getMessage());
				return;
			}
			
			Utility.info("房间加入成功！你已加入房间： " + mClient.getRoomID());
			mIsHost = false;
			SwingUtilities.invokeLater(() -> {
				mFrame.setTitle(mFrame.getTitle() + "(你已在房间： " + mClient.getRoomID() + ")");
				mTextArea.setEditable(false);
			});
			
			startUpdateMonitor();
		}).start();
	}
	
	
}









