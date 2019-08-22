import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login extends JFrame{

    private JTextField nameField = new JTextField();
    private JPasswordField passField = new JPasswordField();
    private JTextField ipField = new JTextField();
    private JButton loginButton = new JButton("登陆");
    private JButton rigsterButton = new JButton("注册");
    private  static int loginCount = 0;

    public Login(){

        this.setTitle("用户登陆");
        this.setSize(400,300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel bodyPanel  = new JPanel();
        bodyPanel.setLayout(null);
        JLabel namenLable = new JLabel("用户 名：");
        JLabel passLable = new JLabel("密   码：");
        JLabel ipLable = new JLabel("服务器：");
        namenLable.setBounds(15,10,60,30);
        passLable.setBounds(15,60,60,30);
        passLable.setBounds(15,60,60,30);
        ipLable.setBounds(15,110,60,30);
        bodyPanel.add(namenLable);
        bodyPanel.add(passLable);
        bodyPanel.add(ipLable);
        nameField.setBounds(80,10,250,30);
        passField.setBounds(80,60,250,30);
        ipField.setBounds(80,110,250,30);
        bodyPanel.add(nameField);
        bodyPanel.add(passField);
        bodyPanel.add(ipField);

        JPanel titlePanel = new JPanel();

        JLabel label = new JLabel("聊天室登陆");
        label.setFont(Utils.getBigFont());
        titlePanel.add(label);

        JPanel btnPanel = new JPanel();
        btnPanel.add(loginButton);
        btnPanel.add(rigsterButton);

        this.add(titlePanel,"North");
        this.add(bodyPanel,"Center");
        this.add(btnPanel,"South");

        addListener();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void addListener() {
        rigsterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Login.this.setVisible(false);
                Register register = new Register(Login.this);
            }
        });
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String pass = new String(passField.getPassword());
                String ip = ipField.getText();

                if(Utils.isNullAndEmpty(ip)||Utils.isNullAndEmpty(pass)||Utils.isNullAndEmpty(name)){
                    JOptionPane.showMessageDialog(Login.this.getRootPane(),"所有项目必须都要填写");
                    return;
                }

                if(pass.length()<6||pass.length()>10){
                    JOptionPane.showMessageDialog(Login.this.getRootPane(),"密码长度6-10");
                    return;
                }

                //联网判断
                Connection connection = null;
                try {
                    connection = JDBCUtils.getConnection();
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER WHERE NAME=? AND pass=?");
                    statement.setString(1,name);
                    statement.setString(2,pass);
                    ResultSet set = statement.executeQuery();
                    if (set.next()){
                        JOptionPane.showMessageDialog(Login.this.getRootPane(),"登陆成功");
                        Login.this.setVisible(false);
                        new Main(name);
                    }else{
                        JOptionPane.showMessageDialog(Login.this.getRootPane(),"用户名或者密码错误");
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }finally {
                    JDBCUtils.closeConnection(connection);
                }
            }
        });

    }


    public static void main(String[] args) {
        Login login = new Login();
    }


}
