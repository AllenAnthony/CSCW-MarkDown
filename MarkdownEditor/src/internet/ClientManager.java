package internet;

import java.util.ArrayList;

import client.Client;

/**
 * 用户管理，服务器端用于管理所有连接的用户
 * @author 马啸远
 *
 */
public class ClientManager {
	//将所有的客户端都添加到这个mClients中进行统一管理
	private ArrayList<Client> mClients = new ArrayList<>();

	public ClientManager() {}
	
	/**
	 * 添加新连接的用户
	 * @param client
	 */
	synchronized void add(Client client) {
		mClients.add(client);
	}

	/**
	 * 用户断开连接
	 * @param client
	 */
	synchronized void delete(Client client) {
		mClients.remove(client);
	}

	synchronized int size() {
		return mClients.size();
	}

	synchronized Client get(int n) {
		return (Client) mClients.get(n);
	}
}












