package Model;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBase {

    private Map<String, String> users;

    private static final int USER_LOGIN = 1;
    private static final int USER_PASSWORD = 3;

    private ClientBase(){
        users = new ConcurrentHashMap<>();
        try {
            parseClients();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static volatile ClientBase instance;

    public static ClientBase getInstance() {
        ClientBase localInstance = instance;
        if (localInstance == null) {
            localInstance = instance;
            if (localInstance == null) {
                instance = localInstance = new ClientBase();
            }
        }
        return localInstance;
    }

    private void parseClients() throws ParserConfigurationException, SAXException{
        try{
            File xmlFile = new File("src/Model/Users.xml");
            if (!xmlFile.isFile())
                return;
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            Node rootElement = doc.getDocumentElement();
            NodeList usersList = rootElement.getChildNodes();

            for(int count = 0; count < usersList.getLength(); count++){
                if (usersList.item(count).getNodeType() != Node.TEXT_NODE) {
                    Node user = usersList.item(count);

                    users.put(user.getChildNodes().item(USER_LOGIN).getTextContent(),
                            user.getChildNodes().item(USER_PASSWORD).getTextContent());

                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public Map<String, String> getUsers() {
        return users;
    }
}
