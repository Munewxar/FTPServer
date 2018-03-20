package Controller;

import View.Window;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable{

    private static final int CONTROL_PORT = 7371;

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private BufferedReader in;

    private Window window;

    private static final String INPUT_REGEXP = "[\\w.,]+";
    private static final String EMPTY_STRING = "";

    private static final String USER_NAME = "USER";
    private static final String PASSWORD = "PASS";
    private static final String PASSIVE = "PASV";
    private static final String PRINT_WORKING_DIRECTORY = "PWD";
    private static final String LIST = "LIST";
    private static final String SYSTEM = "SYST";
    private static final String EXTENDED_PASSIVE = "EPSV";
    private static final String TYPE = "TYPE";
    private static final String QUIT = "QUIT";
    private static final String STOR = "STOR";
    private static final String CWD = "CWD";
    private static final String RETR = "RETR";

    private Commands commands;

    public Server(Window window) {
        this.window = window;
    }

    public void runServer() {

        try {
            serverSocket = new ServerSocket(CONTROL_PORT);

            while (true){
                clientSocket = serverSocket.accept();
                Thread newClientThread = new Thread(this::run);
                newClientThread.run();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            out.write("220 FTPS Altamirano v 1.0\r\n");
            commands = new Commands(out, window);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            while (true) {
                if (in.ready()) {
                    String inLine = in.readLine();

                    Pattern pattern = Pattern.compile(INPUT_REGEXP);
                    Matcher matcher = pattern.matcher(inLine);
                    String command = EMPTY_STRING;
                    if (matcher.find()) {
                        command = matcher.group();
                    }
                    command = command.toUpperCase();
                    String parameter = EMPTY_STRING;
                    if (matcher.find()) {
                        parameter = matcher.group();
                    }

                    window.getCommTextArea().append(command + " " + parameter + "\n");

                    switch (command) {
                        case USER_NAME:
                            commands.commandUSER(parameter);
                            break;

                        case PASSWORD:
                            commands.commandPASS(parameter);
                            break;

                        case SYSTEM:
                            commands.commandSYST();
                            break;

                        case PRINT_WORKING_DIRECTORY:
                            commands.commandPWD();
                            break;

                        case LIST:
                            commands.commandLIST();
                            break;

                        case PASSIVE:
                            commands.commandPASV();
                            break;

                        case EXTENDED_PASSIVE:
                            commands.commandEPSV();
                            break;

                        case TYPE:
                            commands.commandTYPE(parameter);
                            break;

                        case QUIT:
                            commands.commandQUIT(serverSocket);
                            break;

                        case RETR:
                            commands.commandRETR(parameter);
                            break;

                        case STOR:
                            commands.commandSTOR(parameter);
                            break;

                        case CWD:
                            commands.commandCWD(parameter);
                            break;

                        default:
                            commands.commandNotSupported();
                            break;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
