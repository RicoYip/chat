import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class Utils {

    public static int countStart = 0;

    public static Font getBigFont(){
        return new Font("宋体",Font.BOLD,25);
    }

    public static JLabel getNormalLabel(String name){
        JLabel jl = new JLabel(name);
        countStart+=35;
        jl.setBounds(10,countStart,60,30);
        return jl;
    }

    public static JTextField getNormalTF(){
        JTextField jf = new JTextField();
        countStart+=35;
        jf.setBounds(80,countStart,300,30);
        return jf;
    }

    public static boolean isNullAndEmpty(String str){
        return null==str || "".equals(str);
    }

    public static void writeTo(BufferedWriter bw, String msg){
        try {
            bw.write(""+msg+"\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给所有客户端发送消息
     * @param map
     * @param msg
     */
    public static void writeToAll(Map<String,Socket> map, String msg){
        try {
            for(String strings :map.keySet()){
                writeTo(getBW(map.get(strings)),msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 给指定客户端发送消息
     * @param map
     * @param msg
     */
    public static void writeTo(Map<String,Socket> map,String ouid,String msg){
        try {
            for(String strings :map.keySet()){
                if(strings.equals(ouid)){
                    writeTo(getBW(map.get(strings)),msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Socket getServerSocket(){
        try {
            return new Socket("127.0.0.1",8989);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTime(){
        return new SimpleDateFormat("yyyy-MM-dd MM:dd:ss").format(new Date());
    }

    public static BufferedWriter getBW(Socket socket){
        try {
            return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedReader getBR(Socket socket){
        try {
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到信息的前缀
     * @return
     */
    public static String getPre(String string){
        return string.split(":")[0];
    }

    /**
     * 将消息字符串转成数组
     * @param string
     * @return
     */
    public static String[] strToArray(String string){
        return string.split(":");
    }

    public static void setTextareaPointToEnd(JTextArea jTextArea){
        jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
    }

}
