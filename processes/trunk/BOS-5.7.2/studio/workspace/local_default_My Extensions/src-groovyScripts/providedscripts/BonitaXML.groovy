/*METADATA
Object evaluateXPathOnVariable(Object documentOrXML, String xpath)=Returns the result of evaluation of the given xpath on the given org.w3c.dom.Document or XML String. Result can be a string or a org.w3c.Node
*/
package providedscripts ;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result 
import javax.xml.transform.Source 
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory 
import javax.xml.transform.dom.DOMSource 
import javax.xml.transform.stream.StreamResult 
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class BonitaXML {
	
	public static Object evaluateXPathOnVariable(Object documentOrXML, String xpath) {
		Document doc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream contentStream;
		if (documentOrXML instanceof String) {
			contentStream = new ByteArrayInputStream(((String)documentOrXML).getBytes());
		} else if (documentOrXML instanceof Document) {
			// The Document may be namespace-aware whereas the XPath is not
			// namespace-aware. Then we need to reparse it with a non-namespace
			// aware builder
			// TODO: optimize it.
			// If you are a Bonita user, and have a suggestion, please come to our forum ;)
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Source xmlSource = new DOMSource(documentOrXML);
			Result outputTarget = new StreamResult(outputStream);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty OutputKeys.OMIT_XML_DECLARATION, "yes"
			transformer.transform(xmlSource, outputTarget);
			byte[] bytes = outputStream.toByteArray();
			contentStream = new ByteArrayInputStream(bytes);
		}
		doc = builder.parse(contentStream);
		contentStream.close();
		XPath xpathEval = XPathFactory.newInstance().newXPath();
		if (isTextExpected(xpath)) {
			return xpathEval.evaluate(xpath, doc);
		} else {
			return xpathEval.evaluate(xpath, doc, XPathConstants.NODE);
		}
	}
	
	/**
	 * @param xpath
	 * @return
	 */
	private static boolean isTextExpected(String xpath) {
		String[] segments = xpath.split("/");
		String lastSegment = segments[segments.length - 1];
		return lastSegment.equals("text()") || lastSegment.startsWith("@");
	}
}

