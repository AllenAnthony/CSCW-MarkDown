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
 * ����markdown�༭���Ľ���
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
	
	//�˵�������
	private JMenuBar mMenuBar;
	private JMenu mFileMenu, mCSSMenu, mClientMenu;
	private JMenuItem mOpenItem, mSaveItem, mExportHTMLItem, mExportDocItem, mExportPdfItem;
	private JMenuItem mLoginItem;
	private JMenuItem mEditCSSItem, mExternalCSSItem;
	
	//������
	private DefaultTreeModel mTreeModel;
	private JTree mTree;
	private DefaultMutableTreeNode mRoot;
	
	//�ͻ���
	private Client mClient = null;
	
	public static void main(String[] args) {
		EditorGUI editor = new EditorGUI();
		editor.show();
		
		MainServer server = new MainServer();	//��ʼ������������
	}
	
	public EditorGUI() {
		setFrame();
		setMenu();
	}
	
	/**
	 * ����UI
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
	 * ΪĿ¼���±���
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
			
			//�����չ���ˣ��������������ַ������������������֣�����TreeModel����֪��Ϊʲô
			mTreeModel.insertNodeInto(new DefaultMutableTreeNode(title), target, target.getChildCount());
		}
		//�����������������ͼ�����¡���֪��Ϊʲô
		mTree.updateUI();
	}
	
	/**
	 * ����һ��{@link JTextArea}�������༭markdown�ı�
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
	 * ����һ��{@link JEditorPane}��,������ʾHTML
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
	 * ����һ��{@link JTree}��������ʾ�ı�������Ŀ¼�ṹ
	 */
	private void createNavigation() {
		mRoot = new DefaultMutableTreeNode("Ŀ¼");
		mTree = new JTree(mRoot);
		mTree.addTreeSelectionListener(this);
		
		mTreeModel = (DefaultTreeModel)mTree.getModel();
		
//		setTitles();
	}
	
	/**
	 * ����һ��{@link JFrame},��Ϊ������
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
						Utility.error("�޷��رտͻ��˶˿ڣ�");
					}
				}
			}
		});
	}
	
	private void setMenu() {
		mMenuBar = new JMenuBar();
		mFrame.setJMenuBar(mMenuBar);
		
		//�ļ��˵�
		{
			mFileMenu = new JMenu("�ļ�");
			mFileMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mMenuBar.add(mFileMenu);
			
			mOpenItem = new JMenuItem("��");
			mOpenItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mOpenItem);
			mOpenItem.addActionListener(this);
			
			mSaveItem = new JMenuItem("����");
			mSaveItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mSaveItem);
			mSaveItem.addActionListener(this);
			
			mExportHTMLItem = new JMenuItem("����HTML");
			mExportHTMLItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mExportHTMLItem);
			mExportHTMLItem.addActionListener(this);
			
			mExportDocItem = new JMenuItem("����docx");
			mExportDocItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mExportDocItem);
			mExportDocItem.addActionListener(this);
			
			mExportPdfItem = new JMenuItem("����pdf");
			mExportPdfItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mFileMenu.add(mExportPdfItem);
			mExportPdfItem.addActionListener(this);
		}
		
		//CSS�˵�
		{
			mCSSMenu = new JMenu("CSS");
			mCSSMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mMenuBar.add(mCSSMenu);
			
			mEditCSSItem = new JMenuItem("����CSS��ʽ");
			mEditCSSItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mCSSMenu.add(mEditCSSItem);
			mEditCSSItem.addActionListener(this);
			
			mExternalCSSItem = new JMenuItem("�����ⲿCSS�ļ�");
			mExternalCSSItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mCSSMenu.add(mExternalCSSItem);
			mExternalCSSItem.addActionListener(this);
		}
		
		//��¼��ע��˵�
		{
			mClientMenu = new JMenu("��¼/ע��");
			mClientMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mMenuBar.add(mClientMenu);
			
			mLoginItem = new JMenuItem("��¼/ע��");
			mLoginItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
			mClientMenu.add(mLoginItem);
			mLoginItem.addActionListener(this);
		}
	}
	
	/**
	 * ��ʾ�ý���
	 */
	public void show() {
		mFrame.setVisible(true);
	}
	
	
	//�����¼�
	@Override
	public void actionPerformed(ActionEvent event) {
		Object item = event.getSource();
		
		//��markdown�ļ�
	    if(item == mOpenItem) {
	    	String tmp = Utility.getContentFromExternalFile();
	    	if(tmp != null) {
	    		mText = tmp;
		    	mTextArea.setText(mText);
	    	}
	    }
	    
	    //����
	    else if(item == mSaveItem) {
	    	if(mTextChanged) {
	    		if(Utility.saveContent(mText, "md"))
	    			mTextChanged = false;
	    	}
	    }
		
		//����HTML
	    else if(item == mExportHTMLItem) {
	    	Utility.saveContent(Utility.getStyledHTML(mHTML, mCSS), "html");
		}
	    
	    //����docx
	    else if(item == mExportDocItem) {
	    	try {
				mHtmlConverter.saveHtmlToDocx(Utility.getStyledHTML(mHTML, mCSS));
			} catch (Exception e) {
//				e.printStackTrace();
				System.out.println("����docxʧ�ܣ�");
			}
	    }
	    
	    //����pdf
	    //��һЩ���⣬��ʱȡ��
	    else if(item == mExportPdfItem) {
//	    	try {
//				mHtmlConverter.saveHtmlToPdf(Utility.getStyledHTML(mHTML, mCSS));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("����pdfʧ�ܣ�");
//			}
	    }
	    
	    //���CSS
	    else if(item == mEditCSSItem) {
	    	String css = JOptionPane.showInputDialog(null, "������Ҫ��ӵ�CSS��ʽ");
	    	mStyleSheet.addRule(css);
	    	mEditorPane.setText(mHTML);
	    	mCSS += css + "\n";
	    }
	    
	    //�����ⲿCSS
	    else if(item == mExternalCSSItem) {
	    	String rule = Utility.getContentFromExternalFile();
	    	if(rule != null) {
		    	mStyleSheet.addRule(rule);
		    	mEditorPane.setText(mHTML);
		    	mCSS += rule + "\n";
	    	}
	    }
	    
	    //��¼��ע��
	    else if(item == mLoginItem) {
	    	if(mClient != null) {
	    		Utility.info("���Ѿ���¼��");
	    	}
	    	else login();
	    }
	    
	    //��������
	    else if(item == mCreateRoomItem) {
	    	if(mClient.getRoomID() != -1) {
	    		Utility.info("�����ڷ����ڣ�");
	    	}
	    	else createRoom();
	    }
	    
	    //���뷿��
	    else if(item == mJoinRoomItem) {
	    	if(mClient.getRoomID() != -1) {
	    		Utility.info("�����ڷ����ڣ�");
	    	}
	    	else joinRoom();
	    }
	    
	    
	}
	
	/*
	/* �������Դ������Ч�ʡ�����ÿ�������ɾ��������UI����sleep 1000ms���ڴ˹���������û������ָ����ˣ�
	/* ��ô�ֻ�����µ��̣߳����̻߳���ͼȥ�����ɵĸ���UI�̣߳����Ծɵľ������ˣ�
	/* ����ִ�и��µķ����������ʹ��������˷ѣ������Ч�ʡ�
	/* ����invokeLater()�����������߳��ж�UI���и��ģ������һ������UI������ŵ�EDT���¼��ɷ��̣߳��У�
	/* ������ʵ�����������̸߳���UI��Ҳ������invokeAndWait()��
	 * ����Ч�����ǣ�����û�һֱ���ٵ����룬��ôһֱ��������£�ֹͣ���볬��1s���Ż���ø���UI�ķ�����
	 * */
	private Thread lastThread = null;		//�洢��һ��Ҫ����UI���̣߳�����һ������
	private void updateUIEfficiently() {
		new Thread(() -> {
			Thread last = lastThread;
			lastThread = Thread.currentThread();
			//if(isUpdating) return;
			//isUpdating = true;
			
			try {
				//������һ�����µ��߳�
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
					Utility.error("������������ӳ��ִ���");
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
	
	//�����λ��
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

	//��������ѡ�е��¼�
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode) 
				mTree.getLastSelectedPathComponent();//�������ѡ���Ľڵ�  
		
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
	
	
	/******************************�����շֽ���************************************/
	
	
	private JMenu mRoomMenu;
	private JMenuItem mCreateRoomItem, mJoinRoomItem, mExitRoomItem;
	private void setRoomMenu() {
		mRoomMenu = new JMenu("����");
		mRoomMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mMenuBar.add(mRoomMenu);
		
		mCreateRoomItem = new JMenuItem("��������");
		mCreateRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mRoomMenu.add(mCreateRoomItem);
		mCreateRoomItem.addActionListener(this);
		
		mJoinRoomItem = new JMenuItem("���뷿��");
		mJoinRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mRoomMenu.add(mJoinRoomItem);
		mJoinRoomItem.addActionListener(this);
		
		mExitRoomItem = new JMenuItem("�˳���ǰ����");
		mExitRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		mRoomMenu.add(mExitRoomItem);
		mExitRoomItem.addActionListener(this);
		
		mFrame.repaint();
		mFrame.revalidate();
	}
	
	/**
	 * ��¼/ע��
	 */
	private void login() {
		new Thread(() -> {
    		LoginGUI loginGUI = new LoginGUI(EditorGUI.this, Thread.currentThread());
	    	loginGUI.setVisible(true);
	    	
	    	try {
	    		//����һ���Ƚϴ��ʱ�䣬��¼��̫���ܳ������ʱ�䣬�����˾ͷ�����¼��
				Thread.sleep(1000000000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				mClient = loginGUI.getClient();
				
				SwingUtilities.invokeLater(() -> {
					mFrame.setTitle("��ӭ�㣺 " + mClient.getName());
					setRoomMenu();
				});
			}
	    	
    	}).start();
	}
	
	
	private boolean mIsHost;
	
	/**
	 * ������һ������󣬾�Ҫ��ʼһֱ����������������Ϣ
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
				Utility.error("������������жϣ�");
				return;
			}
		}).start();
	}
	
	/**
	 * ��������
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
			
			Utility.info("���䴴���ɹ�!���Ѵ������䣺 " + mClient.getRoomID());
			mIsHost = true;
			SwingUtilities.invokeLater(() -> {
				mFrame.setTitle(mFrame.getTitle() + "(�����ڷ��䣺 " + mClient.getRoomID() + ")");
			});
			
			startUpdateMonitor();
		}).start();
	}
	
	/**
	 * ���뷿��
	 */
	private void joinRoom() {
		new Thread(() -> {
			String idString = JOptionPane.showInputDialog("��������Ҫ����ķ���id��");
			
			try {
				mClient.disposeRequest(RequestType.JOIN_ROOM, idString);
			} catch(Exception e) {
				e.printStackTrace();
				Utility.error(e.getMessage());
				return;
			}
			
			Utility.info("�������ɹ������Ѽ��뷿�䣺 " + mClient.getRoomID());
			mIsHost = false;
			SwingUtilities.invokeLater(() -> {
				mFrame.setTitle(mFrame.getTitle() + "(�����ڷ��䣺 " + mClient.getRoomID() + ")");
				mTextArea.setEditable(false);
			});
			
			startUpdateMonitor();
		}).start();
	}
	
	
}









