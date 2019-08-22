import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Register extends JFrame {

    private JButton button = new JButton("确定");
    private boolean isTestEnv = true;
    private JFrame login;

    public Register(final JFrame login){

        this.login = login;

        this.setSize(400,500);

        JPanel titlePanel = new JPanel();
        JLabel titleLable = new JLabel("注册");
        titleLable.setFont(Utils.getBigFont());
        titlePanel.add(titleLable);


        final JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(null);

        Utils.countStart = 0;//坐标清0
        bodyPanel.add(Utils.getNormalLabel("用户名"));
        bodyPanel.add(Utils.getNormalLabel("密码"));
        bodyPanel.add(Utils.getNormalLabel("确认密码"));
        bodyPanel.add(Utils.getNormalLabel("性别"));
        bodyPanel.add(Utils.getNormalLabel("年龄"));
        bodyPanel.add(Utils.getNormalLabel("电子邮件"));
        Utils.countStart = 0;
        bodyPanel.add(Utils.getNormalTF());
        bodyPanel.add(Utils.getNormalTF());
        bodyPanel.add(Utils.getNormalTF());
        bodyPanel.add(Utils.getNormalTF());
        bodyPanel.add(Utils.getNormalTF());
        bodyPanel.add(Utils.getNormalTF());

        JPanel btnPanel = new JPanel();
        btnPanel.add(button);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.add(titlePanel,"North");
        this.add(bodyPanel,"Center");
        this.add(btnPanel,"South");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                java.util.List<String> vs = new ArrayList<>();
                for(int i=0;i< bodyPanel.getComponentCount();i++){
                    if (bodyPanel.getComponent(i) instanceof JTextField){
                        JTextField textField = (JTextField) bodyPanel.getComponent(i);
                        //检测数据合法性
                        if(!isTestEnv&&Utils.isNullAndEmpty(textField.getText())){
                            JOptionPane.showMessageDialog(bodyPanel,"所有项目必须都要填写");
                            return;
                        }
                        vs.add(textField.getText());
                    }
                }
                if(!isTestEnv) {
                    if (!vs.get(1).equals(vs.get(2))) {
                        JOptionPane.showMessageDialog(bodyPanel,"密码不相同");
                        return;
                    }
                    String pattern = "^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";
                    boolean isMatch = Pattern.matches(pattern, vs.get(5));
                    if(!isMatch){
                        System.out.println(vs.get(5));
                        JOptionPane.showMessageDialog(bodyPanel,"邮箱不合法");
                        return;
                    }
                    int age = Integer.parseInt(vs.get(4));
                    if(age<11||age>99){
                        JOptionPane.showMessageDialog(bodyPanel,"年龄为大于10，小于100");
                        return;
                    }
                }

                Socket socket = Utils.getServerSocket();
                BufferedWriter bw = Utils.getBW(socket);
                StringBuilder sb = new StringBuilder();
                for(String s : vs){
                    sb.append(":");
                    sb.append(s);
                }
                String msg = "reg" + sb.toString();
                Utils.writeTo(bw,"reg" + sb.toString());
                System.out.println(msg);

                try {
                    String[] split = Utils.getBR(socket).readLine().split(":");
                    if(split[0].equals("ackreg")){
                        if(split[1].equals("1")){
                            JOptionPane.showMessageDialog(Register.this.getRootPane(),"注册成功");
                        }else if(split[1].equals("3")){
                            JOptionPane.showMessageDialog(Register.this.getRootPane(),"用户名重复");
                        }else if(split[1].equals("2")){
                            JOptionPane.showMessageDialog(Register.this.getRootPane(),"注册失败");
                        }
                    }
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
//
//                Connection connection = null;
//                try {
//                    connection = JDBCUtils.getConnection();
//                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER WHERE NAME = ?");
//                    statement.setString(1,vs.get(0));
//                    ResultSet resultSet = statement.executeQuery();
//                    if (resultSet.next()){
//                        JOptionPane.showMessageDialog(Register.this.getRootPane(),"用户名重复");
//                        return;
//                    }
//                    statement = connection.prepareStatement("INSERT INTO USER VALUES(NULL,?,?,?,?,?)");
//                    statement.setString(1,vs.get(0));
//                    statement.setString(2,vs.get(1));
//                    statement.setString(3,vs.get(2));
//                    statement.setString(4,vs.get(3));
//                    statement.setString(5,vs.get(4));
//                    int i = statement.executeUpdate();
//                    if(i>0){
//                        JOptionPane.showMessageDialog(bodyPanel, "注册成功", "提示",JOptionPane.DEFAULT_OPTION);
//                        Register.this.setVisible(false);
//                        login.setVisible(true);
//                    }else{
//                        JOptionPane.showMessageDialog(bodyPanel, "注册失败", "提示",JOptionPane.DEFAULT_OPTION);
//                    }
//                } catch (SQLException e1) {
//                    e1.printStackTrace();
//                }finally {
//                    JDBCUtils.closeConnection(connection);
//                }
            }
        });

        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    public static void main(String[] args) {
        new Register(new Login());
    }
}
