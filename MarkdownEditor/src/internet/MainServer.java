package internet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.loading.MLet;

import org.apache.commons.logging.Log;
import org.docx4j.fonts.microsoft.MicrosoftFonts.Font.Bold;

import com.sun.org.apache.xml.internal.resolver.helpers.Debug;

import client.Client;
import client.Room;
import util.RequestType;
import util.SignInfo;
import util.Utility;

/**
 * Markdown editor 的主服务器程序
 * @author 马啸远
 *
 */
public class MainServer {
	
	private static final int SERVER_PORT = 8080; 
	private static final int ROOM_SERVER_PORT = 8081;
	
	private static final int MAX_ROOM_NUMBER = 100;
	
	private ServerSocket mServerSocket;
	private ServerSocket mServerSocket2;
	
	private HashMap<String, Client> mClients = new HashMap<>();
	
	private int ipCount = 1000;	//虚拟ip
	
	private HashMap<String, String> mPasswords = new HashMap<>();	//管理密码
	
	private HashMap<Integer, Room> mRooms = new HashMap<>();	//管理所有的房间
	
	public MainServer() {
		setServer();
	}
	
	
	/**
	 * 建立服务器
	 */
	private void setServer() {
		new Thread(() -> {
			try {
				mServerSocket = new ServerSocket(SERVER_PORT);
				
			} catch (IOException e) {
				e.printStackTrace();
				Utility.error("本地服务器创建失败！");
			}
			
			//服务器不断接收客户端发来的连接请求，然后建立连接
			try {
				while(true) {
					//有新的客户端发来连接请求
					Socket socket = mServerSocket.accept();
					
					Client client = new Client();
					client.setSocket(socket);
					
					//新开一个线程
					new DisposeThread(client, 1).start();
				}
			} catch(Exception e) {
				e.printStackTrace();
				Utility.error("连接超时，或通路出现错误！");
			}
		}).start();
		
		new Thread(() -> {
			try {
				mServerSocket2 = new ServerSocket(ROOM_SERVER_PORT);
				
			} catch(IOException e) {
				e.printStackTrace();
				Utility.error("房间服务器创建失败！");
			}
			
			try {
				while(true) {
					//有新的客户端发来连接请求
					Socket socket = mServerSocket2.accept();
					
					Client client = new Client();
					client.setSocket2(socket);
					
					//新开一个线程
					new DisposeThread(client, 2).start();
				}
			} catch(Exception e) {
				e.printStackTrace();
				Utility.error("连接超时，或通路出现错误！");
			}
		}).start();
	}
	
	/**
	 * 服务器为每一个客户端开一个新的线程来处理它发来的请求
	 * @author 马啸远
	 *
	 */
	public class DisposeThread extends Thread {
		
		private Client mClient;
		private Socket mSocket;	//与客户端建立连接的socket
		
		private InputStream mStreamReader = null;
		private OutputStream mStreamWriter = null;
		
		private PrintWriter mPrintWriter = null;
		
		private BufferedReader mBufferedReader = null;
		
		public DisposeThread(Client client, int flag) throws IOException {
			mClient = client;
			if(flag == 1) {
				mSocket = client.getSocket();
			}
			else {
				mSocket = client.getSocket2();
			}
			
			mStreamReader = mSocket.getInputStream();
			mStreamWriter = mSocket.getOutputStream();
			
			mPrintWriter = 
				new PrintWriter(
					new BufferedWriter(
						new OutputStreamWriter(
							mStreamWriter)), true);
				
			mBufferedReader = 
				new BufferedReader(
					new InputStreamReader(
							mStreamReader));
		}
		
		
		public void run() {
			try {
				String request;	//从客户端发来的请求
				boolean exit = false;
				
				while(!exit) {
					request = mBufferedReader.readLine();
					
					String[] s = request.split("#");
					RequestType type = RequestType.valueOf(s[0]);
					
					switch (type) {
					//客户端请求断开连接
					case CUT_CONNECT:
						exit = true;
						break;
						
					//请求登录
					case LOGIN:
						disposeLogin(s[1], s[2]);
						break;
						
					//请求注册
					case REGISTER:
						disposeRegister(s[1], s[2]);
						break;
						
					case CREATE_ROOM:
						disposeCreateRoom();
						break;
						
					case JOIN_ROOM:
						disposeJoinRoom(s[1]);
						break;
						
					case CONNET_ROOM_SERVER:
						disposeConnectRoom(s[1], mSocket);
						break;
						
					case UPLOAD_UPDATION:
						disposeUpdate(Utility.getCompleteString(mBufferedReader),
										Integer.parseInt(s[1]));
						break;

					default:
						break;
					}
					
				} 
				
				//退出连接时，把端口和流关闭,把客户从表中删除
				mClients.remove(mClient);
				mPrintWriter.close();
				mBufferedReader.close();
				mSocket.close();
			} 
			catch (Exception e) {
				e.printStackTrace();
				Utility.error("与客户端的连接出现了一些问题！");
				return;
			}
		}
		
		
		/**
		 * 处理登录请求
		 * @param name
		 * @param password
		 * @throws Exception
		 */
		private void disposeLogin(String name, String password) throws Exception {
			/*从数据库查找*/
			/*这里简化，用内存中的map*/
			
			String pwd = mPasswords.get(name);
			if(pwd == null) {
				mPrintWriter.println("用户不存在！");
			}
			else if(pwd.equals(password)) {
				//返回成功，并分配一个虚拟ip
				int ip = ipCount++;
				mPrintWriter.println(SignInfo.SUCCESS + "#" + ip);
				mClient.setIP(Integer.toString(ip));
				mClient.setName(name);
				
				mClients.put(name, mClient);
			}
			//密码错误
			else {
				mPrintWriter.println("密码错误！");
			}
		}
		
		
		/**
		 * 处理注册请求
		 * @param name
		 * @param password
		 * @throws Exception
		 */
		private void disposeRegister(String name, String password) throws Exception {
			if(mPasswords.get(name) != null) {
				mPrintWriter.println("用户已存在！");
			}
			else {
				mPasswords.put(name, password);
				
				int ip = ipCount++;
				mPrintWriter.println(SignInfo.SUCCESS + "#" + ip);
				mClient.setIP(Integer.toString(ip));
				mClient.setName(name);
				
				mClients.put(name, mClient);
			}
		}
		
		/**
		 * 处理创建房间请求
		 * @throws Exception
		 */
		private void disposeCreateRoom() throws Exception {
			if(mRooms.size() == MAX_ROOM_NUMBER) {
				mPrintWriter.println("很抱歉！服务器较拥挤，不能创建房间！");
			}
			else {
				int id = 0;
				//找到第一个空的位置，分配一个房间id
				for( ; id < MAX_ROOM_NUMBER; id++) {
					if(mRooms.get(id) == null)
						break;
				}
				
				Room room = new Room(id);
				room.setHost(mClient);
				mClient.setRoomID(id);
				mRooms.put(id, room);
				
				mPrintWriter.println(SignInfo.SUCCESS + "#" + id);
			}
		}
		
		
		/**
		 * 处理加入房间请求
		 * @param id 要加入的房间的id
		 * @throws Exception
		 */
		private void disposeJoinRoom(String idString) throws Exception {
			int id;
			try {
				id = Integer.parseInt(idString);
			} catch(NumberFormatException e) {
				mPrintWriter.println("房间id必须是0~99的数字！");
				return;
			}
			Room room = mRooms.get(id);
			if(room == null) {
				mPrintWriter.println("您要加入的房间不存在！");
			}
			else {
				room.add(mClient);
				mClient.setRoomID(id);
				
				mPrintWriter.println(SignInfo.SUCCESS + "#");
			}
		}
		
		
		synchronized private void disposeConnectRoom(String name, Socket socket) throws Exception {
			Client client = mClients.get(name);
			client.setSocket2(socket);
			
			client.getPrintWriter2().println(SignInfo.SUCCESS + "#");
		}
		
		/**
		 * 将更新发送到组内其他所有人
		 * @param updation
		 * @throws Exception
		 */
		private void disposeUpdate(String updation, int roomID) throws Exception {
			Room room = mRooms.get(roomID);
			
			room.updateAllMember(updation);
		}
	}

}







