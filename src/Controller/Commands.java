package Controller;

import Model.ClientBase;
import View.Window;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class Commands {

    private String userName;
    private String workingDirectory;
    private ClientBase clientBase;

    private ServerSocket dataServerSocket;

    private BufferedWriter out;

    private boolean ASCII = true;

    private Window window;

    Commands(BufferedWriter out, Window window){
        clientBase = ClientBase.getInstance();
        workingDirectory = "/Users/steven/Documents/study/ppvis/FTPS/src/";

        this.out = out;

        this.window = window;
    }

    public void commandUSER(String usrName){
        userName = usrName;
        if(clientBase.getUsers().get(usrName) != null) {
            try {
                out.write("331 Login correct\r\n");
                out.flush();
                window.getCommTextArea().append("331 Login correct\n");
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            try {
                out.write("530 Login incorrect\r\n");
                out.flush();
                window.getCommTextArea().append("530 Login incorrect\n");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void commandPASS(String password){
        if (clientBase.getUsers().get(userName).equals(password)){
            try {
                out.write("230 Password correct\r\n");
                out.flush();
                window.getCommTextArea().append("230 Password correct\n");
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            try {
                out.write("332 Password incorrect\r\n");
                out.flush();
                window.getCommTextArea().append("332 Password incorrect\n");
            }catch (IOException e){
                e.printStackTrace();
            }
        } 
    }
    
    public void commandSYST(){
        try {
            out.write("215 " + System.getProperty("os.name") + "\r\n");
            out.flush();
            window.getCommTextArea().append("215 " + System.getProperty("os.name") + "\n");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void commandPWD(){
        try {
            out.write("250 " + workingDirectory + "\r\n");
            out.flush();
            window.getCommTextArea().append("250 " + workingDirectory + "\n");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void commandNotSupported(){
        try{
            out.write("202 Command not supported\r\n");
            out.flush();
            window.getCommTextArea().append("202 Command not supported\n");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void commandPASV() {
        try {
            dataServerSocket = new ServerSocket(5115, 10, Inet4Address.getLocalHost());
        }catch(IOException e){
            e.printStackTrace();
        }

        int i;
        int j;
        String s = Integer.toBinaryString(dataServerSocket.getLocalPort());
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < 16 - s.length(); ++index) {
            builder.append("0");
        }
        builder.append(s);

        i = Integer.parseInt(builder.toString().substring(0, 8), 2);
        j = Integer.parseInt(builder.toString().substring(8), 2);
        try {
            out.write("227 Entering Passive Mode (172,20,10,9," + i + "," + j + ")" + "\r\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commandEPSV(){
        try {
            dataServerSocket = new ServerSocket(5115);
            out.write("229 Entering EPSV (|||5115|)\r\n");
            window.getCommTextArea().append("229 Entering EPSV (|||5115|)\n");
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void commandLIST(){
        File directory = new File(workingDirectory);
        String[] files = directory.list();

        try {
            Socket socket = dataServerSocket.accept();
            PrintWriter outWriter = null;
            if (ASCII) {
                outWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            }else {
                window.getCommTextArea().append("504 Incorrect type\n");
                out.write("504 Incorrect type\r\n");
                out.close();
                return;
            }
            out.write("150 Opening data chanel for directory listing\r\n");
            window.getCommTextArea().append("150 Opening data chanel for directory listing\n");
            assert files != null;
            for(String fileName: files){
                File file = new File(workingDirectory + fileName);

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd YYYY");

                if(file.isDirectory()) {
                    outWriter.write("d "  + " " + sdf.format(file.lastModified()) + " " + file.length() +
                            " . . . " + fileName + "\r\n");
                    window.getCommTextArea().append("d " + " " + sdf.format(file.lastModified()) + " " + file.length() +
                            " . . . " + fileName + "\r\n");
                } else {
                    outWriter.write("- " + sdf.format(file.lastModified()) + " " + file.length() +
                            " . . . " + fileName + "\r\n");
                    window.getCommTextArea().append("- " + sdf.format(file.lastModified()) +  " " + file.length() +
                            " . . . " + fileName + "\r\n");
                }
            }
            out.write("226 Successfully transferred\r\n");
            window.getCommTextArea().append("226 Successfully transferred\n");

            out.flush();
            outWriter.flush();
            outWriter.close();

            socket.close();
            dataServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commandTYPE(String type){
        try{
            ASCII = type.equals("A");
            out.write("220 Command OK\r\n");
            out.flush();
            window.getCommTextArea().append("200 Command OK\n");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void commandQUIT(ServerSocket serverSocket){
        try{
            out.write("220 Good bye\r\n");
            out.flush();
            window.getCommTextArea().append("220 Good bye\n");
            serverSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void commandRETR(String filename){
        try{
            Socket socket = dataServerSocket.accept();
            if (checkASCII()) return;
            out.write("125 Opening data chanel\r\n");
            out.flush();
            window.getCommTextArea().append("125 Opening data chanel\n");
            File file = new File( workingDirectory + "/" + filename);
            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
            output.close();
            fileInputStream.close();
            socket.close();
            dataServerSocket.close();
            out.write("150 Successfully transfered" + filename + "\r\n");
            window.getCommTextArea().append("150 Successfully transfered\n");
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commandSTOR(String filename){
        try{
            if (checkASCII()) return;
            Socket socket = dataServerSocket.accept();
            out.write("150 Opening data chanel\r\n");
            out.flush();
            window.getCommTextArea().append("150 Opening data chanel\n");
            File file = new File(filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedInputStream output = new BufferedInputStream(socket.getInputStream());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = output.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            out.write("150 Successfully transfered\r\n");
            window.getCommTextArea().append("150 Successfully transfered\n");

            out.flush();
            output.close();
            fileOutputStream.close();
            socket.close();
            dataServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkASCII() throws IOException {
        if(ASCII){
            window.getCommTextArea().append("504 Incorrect type\n");
            out.write("504 Incorrect type\r\n");
            out.close();
            return true;
        }
        return false;
    }

    public void commandCWD(String dirName){
        try{
            out.write("250 " + workingDirectory + dirName + "\r\n");
            window.getCommTextArea().append("250 " + workingDirectory + dirName + "\n");
            setWorkingDirectory(dirName);
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory += workingDirectory + '/';
    }
}
