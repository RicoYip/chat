
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Admin extends JFrame {

    private JTextArea sendTextArea = new JTextArea("");
    private JTextArea receivTextArea = new JTextArea("");
    private JTextArea listTextArea = new JTextArea("");
    private JButton sendBtn = new JButton("发送");
    private JButton exitBtn = new JButton("退出");
    private String uid;
    private ServerSocket ss;
    private BufferedWriter write;
    private BufferedReader reader;
    private Integer msgId = 0;
    private HashMap<String, Socket> map = new HashMap<>();
    private java.util.List<String> users = new ArrayList<>();//在线名单
    //下拉框
    private JComboBox usersCombox = new JComboBox();

    public Admin() {

        try {
            ss = new ServerSocket(8989);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Admin.this.getRootPane(), "创建服务器失败");
            System.exit(0);
        }
        this.setSize(800, 500);
        this.setTitle("聊天室          用户：管理员");
        //在线人
        //接受消息框
        JPanel receive = new JPanel();
        receive.setLayout(new BorderLayout());
        receivTextArea.setLineWrap(true);
        receivTextArea.setWrapStyleWord(true);
        receivTextArea.setFont(new Font("Micosoft Yahei", Font.BOLD, 20));
        receivTextArea.setEnabled(false);
        receive.add(new JScrollPane(receivTextArea), "Center");


        //发送的Panel
        JPanel sendPanel = new JPanel();
        JPanel opPanel = new JPanel();
        opPanel.setLayout(new BorderLayout());

        //单选框
        JRadioButton qJR = new JRadioButton("群聊");
        qJR.setSelected(true);
        JRadioButton dJR = new JRadioButton("单聊");
        JPanel chosePanel = new JPanel();
        chosePanel.add(qJR);
        chosePanel.add(dJR);
        ButtonGroup bg = new ButtonGroup();
        bg.add(qJR);
        bg.add(dJR);

        opPanel.add(usersCombox, "North");
        //退出发送2个按钮面板
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BorderLayout());
        btnPanel.add(sendBtn, "Center");
        btnPanel.add(exitBtn, "South");

        opPanel.add(btnPanel, "Center");
        opPanel.add(chosePanel, "South");

        sendPanel.setLayout(new BorderLayout());
        sendTextArea.setPreferredSize(new Dimension(300, 200));
        sendTextArea.setFont(Utils.getBigFont());
        sendTextArea.setLineWrap(true);//自动换行

        sendPanel.add(sendTextArea, "Center");
        sendPanel.add(opPanel, "East");

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setPreferredSize(new Dimension(100, 0));//高度自动调节的
        listTextArea.setFont(new Font("Micosoft Yahei", Font.BOLD, 20));
        listTextArea.setEnabled(false);
        listPanel.add(new JScrollPane(listTextArea), "Center");

        sendBtn.addActionListener((event) -> {
            String text = sendTextArea.getText();
            int c = msgId++;
            //群聊
            if (qJR.isSelected()) {
                if (Utils.isNullAndEmpty(text)) {
                    JOptionPane.showMessageDialog(Admin.this.getRootPane(), "消息不能为空！");
                    return;
                }
                try {
                    System.out.println("已发送:" + c);
                    Utils.writeToAll(map, "admin:管理员广播消息：" + text);
                    receivTextArea.append("管理员 --> 广播:" + text + "\n");
//                        String[] strings = reader.readLine().split(":");
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.out.println("发送失败！！");
                }
            }
            //单聊
            if (dJR.isSelected()) {
                String ouid = (String) usersCombox.getSelectedItem();//得到选中的用户ID
                Utils.writeTo(map, ouid, "admin-s:管理员和你私聊：" + text);
                //在本方也添加消息
                receivTextArea.append("管理员 --> " + ouid + ":" + text + "\n");

            }
        });
        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.writeToAll(map,"exit-s:服务器已关闭请退出");
                System.exit(0);
            }
        });

        new Thread(() -> {
            while (true) {
                try {
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
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(accept.getOutputStream()));
                                String str = null;
                                while ((str = br.readLine()) != null) {
                                    String[] strings = str.split(":");
                                    //消息类型：参数：数据
                                    str = strings[0];//查看消息前缀
                                    if ("sendmsg".equals(str)) {
                                        int msgid = Integer.parseInt(strings[1]);
//                                          Utils.writeTo(writer, "ack:" + msgid);
                                        Utils.writeToAll(map, uid + " : " + strings[2]);
                                        receivTextArea.append(msgid + " 用户->：" + strings[2] + "群聊 \n");
                                    } else if ("sendmsg-s".equals(str)) {
                                        String ouid = strings[1];//目标ID
                                        Utils.writeTo(map, ouid, uid + " : " + strings[3]);
                                        receivTextArea.append(uid + " 用户->：" + strings[3] + "单聊 \n");
                                    } else if ("login".equals(str)) {
                                        //登陆请求
                                        uid = strings[2];
                                        map.put(strings[2], accept);
                                        System.out.println(strings[2] + "===登陆到服务器");
                                        receivTextArea.append(strings[2] + " 登陆到服务器 时间：" + Utils.getTime() + "\n");
                                        //推送到所有的客户端
                                        StringBuilder sb = new StringBuilder();
                                        for (String s : map.keySet()) {
                                            sb.append(":");
                                            sb.append(s);
                                        }
                                        Utils.writeToAll(map, "acklogin" + sb.toString());
                                    } else if ("exit".equals(str)) {
                                        uid = strings[1];
                                        map.remove(uid);
                                        System.out.println(strings[1] + "===退出服务器");
                                        receivTextArea.append(strings[1] + " 退出服务器 时间：" + Utils.getTime() + "\n");
                                        accept.close();
                                        break;
                                    } else if ("reg".equals(str)) { //注册
                                        if (strings.length < 6) {
                                            Utils.writeTo(writer, "ackreg:2");
                                            return;
                                        }
                                        String name = strings[1];
                                        String pass = strings[2];//确认密码不需要3角标
                                        String pass2 = strings[3];//确认密码不需要3角标
                                        String sex = strings[4];
                                        String age = strings[5];
                                        String email = strings[6];
                                        String pattern = "^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";
                                        if (!pass.equals(pass2)) {
                                            Utils.writeTo(writer, "ackreg:2");
                                            return;
                                        }
                                        boolean isMatch = Pattern.matches(pattern, email);
                                        if (!isMatch) {
                                            Utils.writeTo(writer, "ackreg:2");
                                            return;
                                        }
                                        int a = Integer.parseInt(age);
                                        if (a < 11 || a > 99) {
                                            Utils.writeTo(writer, "ackreg:2");
                                            return;
                                        }
                                        Connection connection = null;
                                        try {
                                            connection = JDBCUtils.getConnection();
                                            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER WHERE NAME = ?");
                                            statement.setString(1, name);
                                            ResultSet resultSet = statement.executeQuery();

                                            if (resultSet.next()) {
                                                Utils.writeTo(writer, "ackreg:3");//代表重复
                                                return;
                                            }
                                            statement = connection.prepareStatement("INSERT INTO USER VALUES(NULL,?,?,?,?,?)");
                                            statement.setString(1, name);
                                            statement.setString(2, pass);
                                            statement.setString(3, sex);
                                            statement.setString(4, age);
                                            statement.setString(5, email);
                                            int i = statement.executeUpdate();
                                            if (i > 0) {
                                                Utils.writeTo(writer, "ackreg:1");//代表成功
                                                receivTextArea.append(name + " 已注册 注册时间" + Utils.getTime() + "\n");
                                            } else {
                                                Utils.writeTo(writer, "ackreg:2");
                                            }
                                            accept.close();
                                            break;
                                        } catch (SQLException e1) {
                                            e1.printStackTrace();
                                        } finally {
                                            JDBCUtils.closeConnection(connection);
                                        }


                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        this.setLocationRelativeTo(null);
        this.add(sendPanel, "South");
        this.add(receive, "Center");
        this.add(listPanel, "East");
        this.setVisible(true);

        //维护在线列表的线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = 0;
                while (true) {
                    //刷新下拉框
                    try {
                        users = new ArrayList<>(map.keySet());
                        Thread.sleep(1000);//每隔1s检测一次
                        if (users.size() != size) {//在线名单有变化才会变动
                            listTextArea.setText("");//清空在线列表
                            usersCombox.removeAllItems();//清空下拉框
                            for (String uname : users) {
                                listTextArea.append(uname + "\n");
                                usersCombox.addItem(uname);
                            }
                            size = users.size();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public static void main(String[] args) {
        new Admin();
    }
}
