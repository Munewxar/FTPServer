package View;

import Controller.Server;

import javax.swing.*;
import java.awt.*;

public class Window {

    private JTextArea commTextArea;
    private static Server server;

    private Window(){
        server = new Server(this);
    }

    private JFrame buildWindow(){
        JFrame serverFrame = new JFrame("FTPS Altamirano");
        commTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(commTextArea);
        scrollPane.setBackground(Color.BLACK);
        commTextArea.setBackground(Color.BLACK);
        commTextArea.setForeground(Color.GREEN);
        serverFrame.setPreferredSize(new Dimension(480, 360));
        serverFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        serverFrame.setLocationRelativeTo(null);
        scrollPane.setAutoscrolls(true);
        scrollPane.setEnabled(true);
        serverFrame.add(scrollPane);
        serverFrame.pack();
        return serverFrame;
    }

    public JTextArea getCommTextArea() {
        return commTextArea;
    }

    public static void main(String[] args) {
        JFrame frame = new Window().buildWindow();
        frame.setVisible(true);
        server.runServer();
    }
}
