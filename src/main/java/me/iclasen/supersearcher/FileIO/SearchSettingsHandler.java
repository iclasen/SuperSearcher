package me.iclasen.supersearcher.FileIO;

import me.iclasen.supersearcher.Searcher.DirectorySearcher.DirectorySearcher;
import me.iclasen.supersearcher.Searcher.DirectorySearcher.SearchRestrictor;
import me.iclasen.supersearcher.Searcher.Data.SearchCriteria;
import me.iclasen.supersearcher.Searcher.Data.SearchSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SearchSettingsHandler {
    private final static boolean DEFAULT_IS_REGEX_VALUE = false;
    private final static boolean DEFAULT_IGNORE_CASE_VALUE = false;
    private final static int DEFAULT_NUMBER_OF_THREADS = 8;

    // Handles the building of the SearchSettings
    // This includes building a DirectorySearcher, the SearchCriteria, and determining the max threads allowed
    public static SearchSettings getSearchSettings(String fileName) {
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       DirectorySearcher directorySearcher;
       ArrayList<SearchCriteria> searchCriteria;
       Integer maxThreads;

       try {
           factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
           DocumentBuilder builder = factory.newDocumentBuilder();
           Document document = builder.parse(new File(fileName));

           // Retrieve the maximum number of threads
           maxThreads = getThreadLimit(document);

           // Retrieve the directories to search
           directorySearcher = new DirectorySearcher(getDirectories(document), maxThreads, getSearchRestrictor(document));

           // Retrieve the search criteria
           searchCriteria = getSearchCriteria(document);

           //result = new SearchSettings();
       } catch (ParserConfigurationException | SAXException | IOException e) {
           throw new RuntimeException(e);
       }
       return new SearchSettings(searchCriteria, directorySearcher, maxThreads);
    }

    // Retrieves the directories to search
    private static ArrayList<String> getDirectories(Document document) {
       ArrayList<String> result = new ArrayList<>();
       NodeList directoryList = document.getElementsByTagName("directory");
       Node node;
       Element element;

       for (int index = 0; index < directoryList.getLength(); index++) {
           node = directoryList.item(index);
           if(node.getNodeType() == Node.ELEMENT_NODE) {
               element = (Element) node;
               result.add(element.getTextContent());
           }
       }

       return result;
    }

    // Retrieves the thread limit if it exists, if no value for it is found, uses a default value instead
    private static int getThreadLimit(Document document) {
        NodeList nodeList = document.getElementsByTagName("threads");
        int result;

        if(nodeList.getLength() > 0) {
            result = Integer.parseInt(nodeList.item(0).getTextContent());
        } else {
            result = DEFAULT_NUMBER_OF_THREADS;
        }

        return result;
    }

    // Retrieves all the SearchCriteria
    private static ArrayList<SearchCriteria> getSearchCriteria(Document document) {
        ArrayList<SearchCriteria> result = new ArrayList<>();
        NodeList directoryList = document.getElementsByTagName("criteria");
        NodeList nodeList;
        Node node;
        Element element;
        boolean isRegex;
        boolean ignoreCase;

        // Loops through each <criteria></criteria> tag
        for (int criteriaIndex = 0; criteriaIndex < directoryList.getLength(); criteriaIndex++) {
            node = directoryList.item(criteriaIndex);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                element = (Element) node;

                // checks the criteria for an is_regex tag, if it does not exist, use the default
                nodeList = element.getElementsByTagName("is_regex");
                if(nodeList.getLength() > 0) {
                    isRegex = nodeList.item(0).getTextContent().equalsIgnoreCase("true");
                } else {
                    isRegex = DEFAULT_IS_REGEX_VALUE;
                }

                // checks the criteria for an ignore_case tag, if it does not exist, use the default
                nodeList = element.getElementsByTagName("ignore_case");
                if(nodeList.getLength() > 0) {
                    ignoreCase = nodeList.item(0).getTextContent().equalsIgnoreCase("true");
                } else {
                    ignoreCase = DEFAULT_IGNORE_CASE_VALUE;
                }

                // Loops through each term in a criteria, uses the is_regex and ignore_case values from above
                for(int termIndex = 0; termIndex < element.getElementsByTagName("term").getLength(); termIndex++) {
                    result.add(new SearchCriteria(element.getElementsByTagName("term").item(termIndex).getTextContent(),
                        0,
                        isRegex,
                        ignoreCase));
                }
            }
        }

        return result;
    }

    // Builds and returns and SearchRestrictor
    private static SearchRestrictor getSearchRestrictor(Document document) {
        return new SearchRestrictor(getNameList(document, "allowed_names"),
                getNameList(document, "blocked_names"),
            getExtensionList(document, "allowed_extensions"),
            getExtensionList(document, "blocked_extensions"));
    }

    // Gets a list of elements with the tag "name"
    private static ArrayList<String> getNameList(Document document, String elementCategory) {
        return getList(document, elementCategory, "name");
    }

    // Gets a list of elements with the tag "extension"
    private static ArrayList<String> getExtensionList(Document document, String elementCategory) {
        ArrayList<String> result = new ArrayList<>();
        String extension;

        // TODO: check and see how this handles a blank value
        for (String string: getList(document, elementCategory, "extension")) {
            if(string.length() != 0 && !(string.charAt(0) == '.')) {
                extension = "." + string;
            } else {
                extension = string;
            }
            result.add(extension);
        }

        return result;
    }

    // Gets a list of elements based on the given values, allows any number of the elements with "elementName" in the results
    private static ArrayList<String> getList(Document document, String elementCategory, String elementName) {
        ArrayList<String> result = new ArrayList<>();
        NodeList list = document.getElementsByTagName(elementCategory);
        Node node;
        Element element;


        for (int index = 0; index < list.getLength(); index++) {
            node = list.item(index);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                element = (Element) node;
                for(int elementNameIndex = 0; elementNameIndex < element.getElementsByTagName(elementName).getLength(); elementNameIndex++) {
                    // gets all the elements with "elementName"
                    result.add(element.getElementsByTagName(elementName).item(elementNameIndex).getTextContent());
                }
            }
        }

        return result;
    }

    // private constructor keeps the class from being instantiated.
    private SearchSettingsHandler() {

    }
}
