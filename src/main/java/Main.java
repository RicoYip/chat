import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Main extends JFrame {

    private JTextArea sendTextArea = new JTextArea("");
    private JTextArea receivTextArea = new JTextArea("");
    private JTextArea listTextArea = new JTextArea("");
    private JButton sendBtn = new JButton("发送");
    private JButton exitBtn = new JButton("退出");
    private JComboBox usersCombox = new JComboBox(); //下拉框
    private String uid;
    private Socket socket;
    private BufferedWriter write;
    private BufferedReader reader;
    private Integer msgId = 0;//每条消息的ID
    private java.util.List<String> users = new ArrayList<>();//在线名单




    public Main(String uid) {

        this.uid = uid;
        //连接Socket服务
        try {
            socket = new Socket("127.0.0.1", 8989);
            this.write = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(Main.this.getRootPane(),"无法连接服务器");
            System.exit(0);
        }
        //设置标题栏
        this.setTitle("聊天室        当前用户ID：" + uid);


        //发送当前登陆ID
        Utils.writeTo(write, "login::" + uid);

        this.setSize(500, 500);
        //接受消息框
        JPanel receive = new JPanel();
        receive.setLayout(new BorderLayout());
        receivTextArea.setLineWrap(true);
        receivTextArea.setWrapStyleWord(true);
        receivTextArea.setFont(new Font("Micosoft Yahei",Font.BOLD,20));
        receivTextArea.setEnabled(false);

        receive.add(new JScrollPane(receivTextArea),"Center");


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
        listPanel.setPreferredSize(new Dimension(100,0));//高度自动调节的
        listTextArea.setFont(new Font("Micosoft Yahei",Font.BOLD,20));
        listTextArea.setEnabled(false);
        listPanel.add(new JScrollPane(listTextArea),"Center");

        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = sendTextArea.getText();
                int c = msgId++;
                //群聊
                if (qJR.isSelected()) {
                    if (Utils.isNullAndEmpty(text)) {
                        JOptionPane.showMessageDialog(Main.this.getRootPane(), "消息不能为空！");
                        return;
                    }
                    try {
                        System.out.println("已发送:" + c);
                        Utils.writeTo(write, "sendmsg:" + c + ":" + text);
//                        String[] strings = reader.readLine().split(":");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        System.out.println("发送失败！！");
                    }
                }
                //单聊
                if (dJR.isSelected()) {
                    String ouid = (String) usersCombox.getSelectedItem();//得到选中的用户ID
                    Utils.writeTo(write,"sendmsg-s:"+ouid+":" + c + ":" + text);
                    //在本方也添加消息
                    receivTextArea.append(uid+":"+text+"\n");

                }
                //设置消息框光标调到末尾
                Utils.setTextareaPointToEnd(receivTextArea);
            }
        });
        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.writeTo(write, "exit:" + uid);
                JOptionPane.showMessageDialog(Main.this.getRootPane(), "退出成功");
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });

        //实时接受聊天室的内容
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("接受线程准备就绪....");
                    String msg;

                    try {
                        while ((msg = reader.readLine()) != null) {
                            String pre = Utils.getPre(msg);
                            String[] arr = Utils.strToArray(msg);
                            if(pre.equals("acklogin")) {
                                users.clear();//清空集合
                                for (int i = 1; i < arr.length; i++) {
                                    users.add(arr[i]);
                                }
                            }else if(pre.equals("exit-s")){
                                JOptionPane.showMessageDialog(null, "服务器关闭，3s后退出");
                                new Thread(()->{
                                    try {
                                        Thread.sleep(3000);
                                        System.exit(0);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }else {
                                receivTextArea.append(msg + "\n");
                                Utils.setTextareaPointToEnd(receivTextArea);
                            }
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(Main.this.getRootPane(), "和服务器断开！");
                        System.exit(0);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


        //维护在线列表的线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = 0;
                while (true){
                    //刷新下拉框
                    try {
                        Thread.sleep(1000);//每隔1s检测一次
                        if(users.size()!=size) {//在线名单有变化才会变动
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

        this.setLocationRelativeTo(null);
        this.add(sendPanel, "South");
        this.add(receive, "Center");
        this.add(listPanel, "East");
        this.setVisible(true);


    }

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Main(i+"");
        }
//        new Main("1");
////        new Main("2");
    }




}
