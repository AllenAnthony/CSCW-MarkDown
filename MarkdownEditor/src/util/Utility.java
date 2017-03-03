package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import jdk.nashorn.internal.runtime.regexp.joni.Warnings;

/**
 * 工具类，提供一些基本的方法,不能实例化
 *
 */

public class Utility {
	//工具类不能实例化
	private Utility() {}
	
	public static final String END_MARK = "$$END$$";
	
	/**
	 * 将content内容保存到外部文件
	 * @param content
	 * @return
	 */
	public static boolean saveContent(String content, String postFix) {
		JFileChooser jf = new JFileChooser();
		jf.setSelectedFile(new File("./untitled." + postFix));
		int option = jf.showSaveDialog(new JLabel());

		if(option == JFileChooser.CANCEL_OPTION)
			return false;
		
		File file = jf.getSelectedFile();
		
		try( PrintWriter writer = new PrintWriter(file) ) {
			writer.write(content);
		} catch(IOException e) {
			System.out.println("write error");
		}
		
		return true;
	}
	
	/**
	 * 从外部文件读取文本内容
	 * @return
	 */
	public static String getContentFromExternalFile() {
		JFileChooser jf = new JFileChooser();
		jf.showOpenDialog(new JLabel());
		File file = jf.getSelectedFile();
		if(file == null) return null;
		String text = null;
		
		try(BufferedReader in = new BufferedReader(
										new InputStreamReader(
										new FileInputStream(file), "utf-8"))) {
			StringBuilder sb = new StringBuilder();
			String tmp;
			while((tmp=in.readLine()) != null) {
				sb.append(tmp);
				sb.append("\n");
			}

			text = sb.toString();
		} catch (IOException e) {
			System.out.println("read error");
		};
		return text;
	}
	
	/**
	 * 得到用CSS样式修饰的HTML
	 * @param html
	 * @param css
	 * @return 
	 */
	public static String getStyledHTML(String html, String css) {
		Pattern pattern = Pattern.compile("(<style>[\\s\\S]*)</style>");
	    Matcher matcher = pattern.matcher(html);
	    StringBuffer sb = new StringBuffer();
	    while(matcher.find()) {
	    	System.out.println("yes");
	    	matcher.appendReplacement(sb, matcher.group(1) +"\n" + css + "</style>");
	    }
	    matcher.appendTail(sb);
	    	
	    return sb.toString();
	}
	
	public static void error(String error) {
		JOptionPane.showMessageDialog(null, error, "错误！",JOptionPane.ERROR_MESSAGE);
	}
	
	public static void info(String info) {
		JOptionPane.showMessageDialog(null, info, "提示",JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void main(String[] args) {
		error("sdf");
	}
	
	
	/**
	 * 读取完整的字符串从输入流（碰到结束符）
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public static String getCompleteString(BufferedReader reader) throws Exception {
		String tmp;
		StringBuilder sb = new StringBuilder();
		while(!(tmp = reader.readLine()).equals(END_MARK)) {
			sb.append(tmp + "\n");
			System.out.println("读到的字符串： " + tmp);
		}

		tmp = sb.toString();
		if(tmp.length() == 1) return "";
		else return tmp.substring(0, tmp.length() - 1);
	}
}
