/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.util.xml;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * convenience methods to make reading org.w3c.dom models easier.
 * 
 * @author Tom Baeyens
 */
public abstract class XmlUtil {

	static final Logger LOG = Logger.getLogger(XmlUtil.class.getName());

  public static List<Element> elements(Element element, String tagName) {
    List<Element> elements = null;
    NodeList nodeList = element.getChildNodes();
    if (nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node child = nodeList.item(i);
        if (Element.class.isAssignableFrom(child.getClass())) {
          Element childElement = (Element) child;
          String childTagName = getTagLocalName(childElement);
          if (childTagName.equals(tagName)) {
            if (elements == null) {
              elements = new ArrayList<Element>();
            }
            elements.add(childElement);
          }
        }
      }
    }
    return elements;
  }

  public static List<Element> elements(Element element,
      Set<String> allowedTagNames) {
    List<Element> elements = null;
    NodeList nodeList = element.getChildNodes();
    if (nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node child = nodeList.item(i);
        if (Element.class.isAssignableFrom(child.getClass())) {
          Element childElement = (Element) child;
          String childTagName = getTagLocalName(childElement);
          if (allowedTagNames.contains(childTagName)) {
            if (elements == null) {
              elements = new ArrayList<Element>();
            }
            elements.add(childElement);
          }
        }
      }
    }
    return elements;
  }

  public static Element element(Element element, String tagName) {
    Element childElement = null;
    if (element != null) {
      NodeList nodeList = element.getChildNodes();
      for (int i = 0; ((i < nodeList.getLength()) && (childElement == null)); i++) {
        Node child = nodeList.item(i);
        if ((Element.class.isAssignableFrom(child.getClass()))
            && (getTagLocalName((Element) child)).equals(tagName)) {
          childElement = (Element) child;
        }
      }
    }
    return childElement;
  }

  public static List<Element> elements(Element element) {
    List<Element> elements = null;
    if (element != null) {
      NodeList nodeList = element.getChildNodes();
      if ((nodeList != null) && (nodeList.getLength() > 0)) {
        elements = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node node = nodeList.item(i);
          if (node instanceof Element) {
            elements.add((Element) node);
          }
        }
      }
    }
    return elements;
  }

  public static List<Element> elements(Element father, String ns,
      String localName) {
    List<Element> matchingElements = new ArrayList<Element>();
    NodeList nl = father.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element && n.getLocalName() != null
          && n.getLocalName().equals(localName) && n.getNamespaceURI() != null
          && n.getNamespaceURI().equals(ns)) {
        matchingElements.add((Element) n);
      }
    }
    return matchingElements;
  }

  public static List<Element> elementsQName(Element element,
      Set<QName> allowedTagNames) {
    List<Element> elements = null;
    NodeList nodeList = element.getChildNodes();
    if (nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node child = nodeList.item(i);
        if (Element.class.isAssignableFrom(child.getClass())) {
          Element childElement = (Element) child;
          QName childElementQName = new QName(childElement.getNamespaceURI(),
              childElement.getLocalName());
          if (allowedTagNames.contains(childElementQName)) {
            if (elements == null) {
              elements = new ArrayList<Element>();
            }
            elements.add(childElement);
          }
        }
      }
    }
    return elements;
  }

  public static Element element(Element element) {
    Element onlyChild = null;
    if (element != null) {
      List<Element> elements = elements(element);
      if (elements != null) {
        onlyChild = (Element) elements.get(0);
      }
    }
    return onlyChild;
  }

  public static String toString(Element element) {
    if (element == null) {
      return "null";
    }

    Source source = new DOMSource(element);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    Result result = new StreamResult(printWriter);

    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.transform(source, result);
    } catch (Exception e) {
    	if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("couldn't transform dom element into string representation");
    	}
      return "<" + element.getTagName() + " ... >...</" + element.getTagName()
          + ">";
    }

    printWriter.close();

    return stringWriter.toString();
  }

  public static String getContentText(Element element) {
    StringBuffer buffer = new StringBuffer();
    NodeList nodeList = element.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node instanceof CharacterData) {
        CharacterData characterData = (CharacterData) node;
        buffer.append(characterData.getData());
      }
    }
    return buffer.toString();
  }

  public static boolean isTextOnly(Element element) {
    boolean isTextOnly = true;
    NodeList nodeList = element.getChildNodes();
    for (int i = 0; ((i < nodeList.getLength()) && (isTextOnly)); i++) {
      if (Element.class.isAssignableFrom(nodeList.item(i).getClass())) {
        isTextOnly = false;
      }
    }
    return isTextOnly;
  }

  public static List<Attr> attributes(Element element) {
    NamedNodeMap attributeMap = element.getAttributes();
    if ((attributeMap == null) || (attributeMap.getLength() == 0)) {
      return null;
    }

    List<Attr> attributes = new ArrayList<Attr>();
    for (int i = 0; i < attributeMap.getLength(); i++) {
      attributes.add((Attr) attributeMap.item(i));
    }

    return attributes;
  }

  public static String getTagLocalName(Element element) {
    if (element == null) {
      return null;
    }
    String localName = element.getLocalName();
    if (localName != null) {
      return localName;
    }
    return element.getTagName();
  }

  /** the attribute value or null if the attribute is not present */
  public static String attribute(Element element, String attributeName) {
    if (element.hasAttribute(attributeName)) {
      return element.getAttribute(attributeName);
    } else {
      return null;
    }
  }

  /**
   * convenience method to combine extraction of a string attribute value.
   * 
   * If the attribute exists, it is returned. If the attribute is not present,
   * the defaultValue is returned. The attribute is not present and it is
   * required, a problem will be added to the parse.
   */
  public static String attribute(Element element, String attributeName,
      boolean required, Parse parse) {
    if (element.hasAttribute(attributeName)) {
      String value = element.getAttribute(attributeName);
      if (required && "".equals(value)) {
        parse.addProblem("attribute <" + XmlUtil.getTagLocalName(element) + " "
            + attributeName + "=\"\" is empty");
      }
      return value;
    }

    if (required) {
      parse.addProblem("attribute <" + XmlUtil.getTagLocalName(element) + " "
          + attributeName + "=\"...\" is required");
    }

    return null;
  }

  /** parse an attribute as an integer. */
  public static Integer attributeInteger(Element element, String attributeName,
      boolean required, Parse parse) {
    String valueText = attribute(element, attributeName, required, parse);

    if (valueText != null) {
      try {
        return Integer.parseInt(valueText);
      } catch (NumberFormatException e) {
        parse.addProblem(errorMessageAttribute(element, attributeName,
            valueText, "value not parsable as integer"));
      }
    }

    return null;
  }

  /** parse an attribute as an boolean. */
  public static Boolean attributeBoolean(Element element, String attributeName,
      boolean required, Parse parse) {
    return attributeBoolean(element, attributeName, required, parse, null);
  }

  /** parse an attribute as an boolean. */
  public static Boolean attributeBoolean(Element element, String attributeName,
      boolean required, Parse parse, Boolean defaultValue) {
    String valueText = attribute(element, attributeName, required, parse);
    if (valueText != null) {
      Boolean value = parseBooleanValue(valueText);
      if (value == null) {
        parse.addProblem(errorMessageAttribute(element, attributeName, 
            valueText, "value not in {true, enabled, on, false, disabled, off}"));
      }
      return value;
    }
    return defaultValue;
  }

  public static Boolean parseBooleanValue(String valueText) {
    if (valueText != null) {
      // if we have to check for value true
      if (("true".equals(valueText)) || ("enabled".equals(valueText))
          || ("on".equals(valueText))) {
        return Boolean.TRUE;

      } else if (("false".equals(valueText)) || ("disabled".equals(valueText))
          || ("off".equals(valueText))) {
        return Boolean.FALSE;
      }
    }

    return null;
  }

  public static String errorMessageAttribute(Element element,
      String attributeName, String attributeValue, String message) {
    return "attribute <" + XmlUtil.getTagLocalName(element) + " "
        + attributeName + "=\"" + attributeValue + "\" " + message;
  }

  public static List<String> parseList(Element element, String singularTagName) {
    // a null value for text represents a wildcard
    String text = XmlUtil.attribute(element, singularTagName + "s");
    // so next we'll convert a '*' into the text null value, which indicates a
    // wildcard
    if ("*".equals(text)) {
      text = null;
    }
    if (element.hasAttribute(singularTagName)) {
      String eventText = element.getAttribute(singularTagName);
      text = (text == null ? eventText : text + "," + eventText);
    }
    List<String> eventNames = parseCommaSeparatedList(text);
    return eventNames;
  }

  /**
   * parses comma or space separated list. A null return value means a wildcard.
   * 
   * @return List of tokens or null if the commaSeparatedListText is null, '*',
   *         or empty
   */
  public static List<String> parseCommaSeparatedList(
      String commaSeparatedListText) {
    List<String> entries = null;
    if (commaSeparatedListText != null) {
      if (!"*".equals(commaSeparatedListText)) {
        StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListText,
            ", ");
        while (tokenizer.hasMoreTokens()) {
          if (entries == null) {
            entries = new ArrayList<String>();
          }
          entries.add(tokenizer.nextToken());
        }
      }
    }
    return entries;
  }

  public static class NamespaceValue {

    public String prefix;
    public String localPart;

    public NamespaceValue(String prefix, String localPart) {
      this.prefix = prefix;
      this.localPart = localPart;
    }
  }

  public static NamespaceValue attributeNamespaceValue(Element element,
      String attributeName) {
    NamespaceValue namespaceValue = null;
    String text = attribute(element, attributeName);
    if (text != null) {
      int colonIndex = text.indexOf(':');
      if (colonIndex == -1) {
        namespaceValue = new NamespaceValue(null, text);
      } else {
        String prefix = text.substring(0, colonIndex);
        String localPart = null;
        if (text.length() > colonIndex + 1) {
          localPart = text.substring(colonIndex + 1);
        }
        namespaceValue = new NamespaceValue(prefix, localPart);
      }
    }
    return namespaceValue;
  }

  public static QName attributeQName(Element element, String attributeName) {
    QName qname = null;

    NamespaceValue namespaceValue = attributeNamespaceValue(element, attributeName);
    String text = attribute(element, attributeName);
    if (namespaceValue != null) {
      if (namespaceValue.prefix == null) {
        qname = new QName(text);
      } else {
        String uri = element.lookupNamespaceURI(namespaceValue.prefix);
        if (uri == null) {
        	String message = ExceptionManager.getInstance().getFullMessage("bp_XU_1", text);
          throw new BonitaRuntimeException(message);
        } else if (namespaceValue.localPart == null) {
        	String message = ExceptionManager.getInstance().getFullMessage("bp_XU_2", text);
        	throw new BonitaRuntimeException(message);
        } else {
          qname = new QName(uri, namespaceValue.localPart, namespaceValue.prefix);
        }
      }
    }
    return qname;
  }

  public static QName getQNameFromString(Element element, String qnameAsString) {
    if (qnameAsString == null || element == null) {
      return null;
    }
    int colonIndex = qnameAsString.indexOf(':');
    String prefix = qnameAsString.substring(0, colonIndex);
    String localName = qnameAsString.substring(colonIndex + 1);
    String ns = getNamespaceURI(element, prefix);
    return new QName(ns, localName, prefix);
  }

  public static String getNamespaceURI(final org.w3c.dom.Node n,
      final String prefix) {
    Node prefixDeclaration = n.getAttributes().getNamedItem("xmlns:" + prefix);
    if (prefixDeclaration != null) {
      // we have found the good NameSpace
      return prefixDeclaration.getNodeValue();
    }
    // we have found the good NameSpace
    // we look for the NameSpace in the parent Node
    return getNamespaceURI(n.getParentNode(), prefix);
  }
 
}
