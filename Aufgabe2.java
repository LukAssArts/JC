
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Aufgabe2 {

	Map<String, Set<String>> variations;
	Set<String> names;

	private class ConfigHandler extends DefaultHandler {

		private boolean insideJournalTitel = false;
		private boolean insideArticleTitle = false;
		private boolean insideArticle = false;
		private boolean insideJournal = false;

		private String keyValue;
		private String conferenceOrArticleName;

		public void setDocumentLocator(Locator locator) {
		}

		public void startElement(String namespaceURI, String localName, String rawName, Attributes atts)
				throws SAXException {
			if (insideArticle) {
				if (rawName.equals("journal")) {
					conferenceOrArticleName = "";
					insideJournalTitel = true;
				}
			} else if (insideJournal) {
				if (rawName.equals("title")) {
					conferenceOrArticleName = "";
					insideArticleTitle = true;
				}
			} else {
				if (rawName.equals("article")) {
					insideArticle = true;
					keyValue = atts.getValue("key").split("/")[1];
				} else if (rawName.equals("proceedings")) {
					insideJournal = true;
					keyValue = atts.getValue("key").split("/")[1];
				}

			}
		}

		public void endElement(String namespaceURI, String localName, String rawName) throws SAXException {

			if (rawName.equals("article")) {
				insideArticle = false;
				addTitleToMap(keyValue, conferenceOrArticleName);
			} else if (rawName.equals("proceedings")) {
				insideJournal = false;
				addTitleToMap(keyValue, conferenceOrArticleName);
			} else if (rawName.equals("journal")) {
				insideJournalTitel = false;
			} else if (rawName.equals("title")) {
				insideArticleTitle = false;
			}

		}

		// füge die Ergebnisse zur Map hinzu
		private void addTitleToMap(String key, String title) {
			if (variations.containsKey(key)) {
				variations.get(key).add(conferenceOrArticleName);
			} else {
				Set<String> set = new HashSet<String>();
				set.add(conferenceOrArticleName);
				variations.put(key, set);
			}

		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (insideJournalTitel || insideArticleTitle) {
				String name = new String(ch, start, length);
				conferenceOrArticleName += name;
			}

		}

		private void Message(String mode, SAXParseException exception) {
			System.out.println(mode + " Line: " + exception.getLineNumber() + " URI: " + exception.getSystemId() + "\n"
					+ " Message: " + exception.getMessage());
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

		for (Entry<String, Set<String>> entry : variations.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1) {
				System.out.println(entry.getKey() + "\t\t variations: " + entry.getValue() + "\r\n");
				if (size > maxVariations) {
					maxVariations = size;
					maxVariationsName = entry.getKey();
				}
				resultCount++;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("\nThere are ").append(resultCount).append(" journals/proceeding names with different variations\n");
		sb.append("\nThe name with the most variations is ").append(maxVariationsName)
				.append(variations.get(maxVariationsName)).append(" with ").append(maxVariations).append(" variations");
		System.out.println(sb);
	}

	Aufgabe2(String dblpXmlFileName) {

		variations = new HashMap<String, Set<String>>();
//		System.out.println(Paths.get("").toAbsolutePath());

		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();
			ConfigHandler handler = new ConfigHandler();
			parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", true);
			if (dblpXmlFileName.endsWith(".gz"))
				parser.parse(new GZIPInputStream(new FileInputStream(dblpXmlFileName)), handler);
			else
				parser.parse(new File(dblpXmlFileName), handler);
		} catch (IOException e) {
			System.out.println("Error reading URI: " + e.getMessage());
		} catch (SAXException e) {
			System.out.println("Error in parsing: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println("Error in XML parser configuration: " + e.getMessage());
		}
		print_result();
	}

	public static void main(String[] args) {
		System.setProperty("entityExpansionLimit", "2500000");
		if (args.length < 1) {
			System.out.println("Usage: java Parser [input]");
			System.exit(0);
		}

		new Aufgabe2(args[0]);
	}
}
