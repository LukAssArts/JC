
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Aufgabe1 {
    Set<String> chLastNames;
    Set<String> chFirstNames;

    private class ConfigHandler extends DefaultHandler {

        private String value;
        private boolean insideName = false;
        private boolean isChName = false;

        public void setDocumentLocator(Locator locator) {
        }

        public void startElement(String namespaceURI, String localName,
                                 String rawName, Attributes atts) throws SAXException {
            if (rawName.equals("author") || rawName.equals("editor")) {
                insideName = true;
                value = "";
            }
        }

        public void endElement(String namespaceURI, String localName,
                               String rawName) throws SAXException {

            if (!rawName.equals("author") && !rawName.equals("editor"))
                return;
            insideName = false;
            if(isChName) {
                System.out.println("chinese Name: " + value);
                StringTokenizer st = new StringTokenizer(value, " ");
                char c = st.nextToken().charAt(0);
                while (st.hasMoreTokens()) {
                    if (st.nextToken().charAt(0) != c + 1)
                        return;
                    c++;
                }
                chFirstNames.add(value);
                isChName = false;
            }
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {

            if (insideName) {
                String name = new String(ch, start, length);
                if(chLastNames.contains(name)){
                    isChName = true;
                }
                value += name;
            }

        }

        private void Message(String mode, SAXParseException exception) {
            System.out.println(mode + " Line: " + exception.getLineNumber()
                    + " URI: " + exception.getSystemId() + "\n" + " Message: "
                    + exception.getMessage());
        }

        public void warning(SAXParseException exception) throws SAXException {

            Message("**Parsing Warning**\n", exception);
            throw new SAXException("Warning encountered");
        }

        public void error(SAXParseException exception) throws SAXException {

            Message("**Parsing Error**\n", exception);
            throw new SAXException("Error encountered");
        }

        public void fatalError(SAXParseException exception) throws SAXException {

            Message("**Parsing Fatal Error**\n", exception);
            throw new SAXException("Fatal Error encountered");
        }
    }

    void print_result() {
        for (String name: chFirstNames) {
            System.out.println(name);
        }
    }

    Aufgabe1(String dblpXmlFileName) {
        String[] chArray = { "Wang", "Chen", "Li", "Chang", "Liu", "Yang", "Huang", "Wu",
                "Lin", "Chou", "Yeh", "Chao", "Lu", "Hsu", "Sun", "Chu", "Kao",
                "Ma", "Liang", "Kuo", "He", "Cheng", "Hu", "Tsai", "Tseng", "Wong",
                "She", "Teng", "Shen", "Hsieh", "Tang", "Hsu", "Lo", "Yuan", "Feng",
                "Sung", "Su", "Tsao", "Lu", "Mai", "Tung", "Yu", "Han", "Jen",
                "Chiang", "Ku", "Chung", "Fang", "Tu", "Ting", "Yao", "Pan",
                "Chiang", "Tan", "Chiu", "Hsiao", "Chin", "Chia", "Tien", "Tsui", "Cheng"};
        /*
         * Init Set from array
         */
        chLastNames = new TreeSet<String>();
        for(int i = 0; i < chArray.length; i++) {
            chLastNames.add(chArray[i]);
        }

        chFirstNames = new TreeSet<String>();
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            ConfigHandler handler = new ConfigHandler();
            parser.getXMLReader().setFeature(
                    "http://xml.org/sax/features/validation", true);
            if (dblpXmlFileName.endsWith(".gz"))
                parser.parse(new GZIPInputStream(new FileInputStream(dblpXmlFileName)), handler);
            else
                parser.parse(new File(dblpXmlFileName), handler);
        } catch (IOException e) {
            System.out.println("Error reading URI: " + e.getMessage());
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println("Error in XML parser configuration: "
                    + e.getMessage());
        }
        print_result();
    }

    public static void main(String[] args) {
        System.setProperty("entityExpansionLimit", "2500000");
        if (args.length < 1) {
            System.out.println("Usage: java Parser [input]");
            System.exit(0);
        }

        new Aufgabe1(args[0]);
    }
}