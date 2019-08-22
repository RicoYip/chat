import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    public static void main(String[] args) {

        HashMap<String, Socket> map = new HashMap<>();
        try {
            ServerSocket ss = new ServerSocket(8989);
            while (true) {
                System.out.println("等待客户端连接....");
                Socket accept = ss.accept();

                //每一个客户端维持一个线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String uid = "-1";
                            System.out.println("服务端线程已就绪...");
                            BufferedReader br = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                            String str = null;
                            while ((str = br.readLine()) != null) {
                                String[] strings = str.split(":");
                                //消息类型：参数：数据
                                str = strings[0];//查看消息前缀
                                if ("sendmsg".equals(str)) {
                                    int msgid = Integer.parseInt(strings[1]);
                                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(accept.getOutputStream()));
//                                    Utils.writeTo(writer, "ack:" + msgid);
                                    Utils.writeTo(writer, ""+uid+" : " + strings[2] + "已发送");
                                }else if("login".equals(str)){
                                    //登陆请求
                                    uid = strings[2];
                                    map.put(strings[2],accept);
                                    System.out.println(strings[2]+"===登陆到服务器");
                                }else if("exit".equals(str)){
                                    uid = strings[1];
                                    map.remove(uid);
                                    System.out.println(strings[1]+"===退出服务器");
                                    accept.close();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
