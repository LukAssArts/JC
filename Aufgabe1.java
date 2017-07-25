
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
    Map<String, Set<String>> variations;
    int nameCount, chineseNameCount; 

    private class ConfigHandler extends DefaultHandler {

        private String value;
        private boolean insideName = false;

        public void setDocumentLocator(Locator locator) {
        }

        public void startElement(String namespaceURI, String localName,
                                 String rawName, Attributes atts) throws SAXException {
            if (rawName.equals("author") || rawName.equals("editor")) {
                insideName = true;
                nameCount++;
                value = "";
            }
        }

        public void endElement(String namespaceURI, String localName,
                               String rawName) throws SAXException {

            if (!rawName.equals("author") && !rawName.equals("editor")){
                return;
            }
            
            insideName = false;
            String[] names = value.split(" ");					//teile in vor und nachnamen
            int lastIndex = names.length-1;
            //teste, ob hinter dem Nachnamen ein Zahl-suffix steht, falls ja, setze den lastIndex auf den String des Nachnamens
            while(names[lastIndex].chars().allMatch( Character::isDigit )){
            	lastIndex--;
            }
            if (!chLastNames.contains(names[lastIndex])){	//prüfe ob der nachname ein chinesischer ist
            	return;
            }
            chineseNameCount++;
            /*
             * erstelle für jeden Namen ein "standardisierten" Namen, welcher zusammen geschrieben wird.
             * so wird z.B. A-B, A-b, A B, A b, Ab zu ab.
             * Setze danach den Nachnamen mit Leerzeichen hinter den Vornamen.
             * Mithilfe des standardisierten Namens werden spätere Vorkommen auf Gleichheit überprüft
             */
            String nameVariation = "";
            String stName = "";
            for(int i = 0; i < lastIndex; i++){	
            	if(i > 0){			//durch das Splitten werden Leerzeichen entfernt, welche jedoch für die Variation wichtig sind -> Füge neue hinzu
            		nameVariation += " " + names[i];
            	} else {
            		nameVariation = names[i];
            	}
            	stName += names[i];	//lasse sie beim standardisierten Namen weg.
            }
          //entferne Bindestriche, setze alles auf Kleinbuchstaben und füge Nachnamen hinzu
            stName = stName.replace("-", "").toLowerCase() + " " + names[lastIndex].toLowerCase(); 	
            
            //füge die Ergebnisse zur Map hinzu
            if(variations.containsKey(stName)){
            	variations.get(stName).add(nameVariation);
            } else {
            	Set<String> set = new TreeSet<String>();
            	set.add(nameVariation);
            	variations.put(stName, set);
            }
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {

            if (insideName) {
                String name = new String(ch, start, length);
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
    	int resultCount = 0;
    	int maxVariations = 0;
    	String maxVariationsName = "";
    	
    	for (Entry<String, Set<String>> entry : variations.entrySet()){
    		int size = entry.getValue().size();
    		if(size > 1){
    			System.out.println(entry.getKey() + "\t\t variations: " + entry.getValue());
    			if(size > maxVariations){
    				maxVariations = size;
    				maxVariationsName = entry.getKey();
    			}
    			resultCount++;
    		}
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append("There are ").append(resultCount).append(" chinese names with different variations\n");
    	sb.append("(one of) The name(s) with the most variations is ").append(maxVariationsName).append(variations.get(maxVariationsName)).append(" with ").append(maxVariations).append(" variations");
    	System.out.println(sb);
    }

    Aufgabe1(String dblpXmlFileName) {
    	nameCount = chineseNameCount = 0;
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
        /*
         * init map
         */
        variations = new HashMap<String, Set<String>>();
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
