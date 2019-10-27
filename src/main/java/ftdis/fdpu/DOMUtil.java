package ftdis.fdpu;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The DOMUtil class contains a set of support methods to encapsulate and improve
 * the efficiency of the XML processing.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class DOMUtil {
    /**
     * The method returns the value of an attribute of a DOM node.
     *
     * @param node     The DOM node that contains the attribute
     * @param attrName The name of the attribute
     * @return The value of the attribute, or null if no data is found.
     */
    public static String getAttributeValue(Node node, String attrName) throws DOMUtilException{
        String attrVal;
        try {
            //check whether node is type Element and return attribute value
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                attrVal = node.getAttributes().getNamedItem(attrName).getNodeValue();
                if(! attrVal.isEmpty())
                    return attrVal;
                else
                    throw new DOMUtilException("Attribute " + attrName + " not found!");
            }else
                throw new DOMUtilException("Node not of type ELEMENT_NODE!");
        } catch (DOMUtilException e){
            throw e;
        }
    }

    /**
     * The method returns the data of an element, e.g. "XYZ" for element "<data>XYZ</data>"
     *
     * @param node DOM node that contains the data
     * @return Data of the element, or null if no data is found.
     */
    public static String getElementValue(Node node) throws DOMUtilException{
        StringBuffer sb = new StringBuffer();
        try {
            //Check whether node is type Element and holds data, i.e. has no child nodes
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                for (int n = 0; n < node.getChildNodes().getLength(); n++) {
                    Node childNode = node.getChildNodes().item(n);
                    if (childNode.getNodeType() == Node.TEXT_NODE) {
                        sb.append(childNode.getNodeValue());
                    }
                }
                return sb.toString();
            }else
                throw new DOMUtilException("Node not of type ELEMENT_NODE!");
        } catch (DOMUtilException e){
            throw e;
        }
    }

    /**
     * The method returns a node from a node list, based on specific search criteria,
     * i.e. the node's tag name, attribute name and corresponding attribute value.
     *
     * The method has two interfaces, supporting two types of node lists.
     *
     * @param nodeList  List of DOM nodes, type NodeList, or List<Node>
     * @param tagName   The tag name of the DOM node the method is supposed to search for
     * @param attrName  The name of the attribute of the DOM node
     * @param attrValue The value of the DOM node's attribute
     * @return The node that matches the specified search criteria, or null
     * if no matching DOM node is found.
     */
    public static Node findNode(NodeList nodeList, String tagName, String attrName, String attrValue) throws DOMUtilException{
        try {
            if(nodeList.getLength() != 0){
                //loop through list of nodes and check for tagName
                for (int n = 0; n < nodeList.getLength(); n++) {
                    Node node = nodeList.item(n);
                    if (node.getNodeName().equalsIgnoreCase(tagName)) {
                        //check for attribute name and value
                        if (getAttributeValue(node, attrName).equalsIgnoreCase(attrValue)) {
                            return node;
                        }
                    }
                }
                throw new DOMUtilException("No node "+ tagName + " and attribute " + attrName + "=" + attrValue + " found!");
            }else
                throw new DOMUtilException("NodeList is empty!");
        } catch (DOMUtilException e){
            throw e;
        }
    }

    public static Node findNode(List<Node> nodeList, String tagName, String attrName, String attrValue) throws DOMUtilException {
        try {
            if(!nodeList.isEmpty()){
                //loop through list of nodes and check for tagName
                for (int n = 0; n < nodeList.size(); n++) {
                    Node node = nodeList.get(n);
                    if (node.getNodeName().equalsIgnoreCase(tagName)) {
                        //check for attribute name and value
                        if (getAttributeValue(node, attrName).equalsIgnoreCase(attrValue)) {
                            return node;
                        }
                    }
                }
                throw new DOMUtilException("No node "+ tagName + " and attribute " + attrName + "=" + attrValue + " found!");
            }else
                throw new DOMUtilException("NodeList is empty!");
        } catch (DOMUtilException e){
            throw e;
        }
    }

    public static Node findNode(NodeList nodeList, String tagName) throws DOMUtilException {
        try {
            if (nodeList.getLength() != 0) {
                //loop through list of nodes and check for tagName
                for (int n = 0; n < nodeList.getLength(); n++) {
                    Node node = nodeList.item(n);
                    if (node.getNodeName().equalsIgnoreCase(tagName)) {
                        return node;
                    }
                }
                throw new DOMUtilException("No node "+ tagName + " found!");
            }else
                throw new DOMUtilException("NodeList is empty!");
        } catch (DOMUtilException e){
            throw e;
        }
    }

    /**
     * The method returns all child nodes of a specific name, of a DOM node. This function is similar to
     * to getElementsByTagName which returns all nodes of a specific name in a document.
     *
     * @param node  DOM node that contains child nodes.
     * @return List of child nodes, or null if no data is found.
     */
    public static List<Node> getChildElementsByTagName(Node node, String tagName) throws DOMUtilException {
        List<Node> nodeList = new ArrayList<Node>();
        try{
            //Loop through child nodes and check for Element nodes
            for(Node childNode = node.getFirstChild(); childNode!=null;){
                if(childNode.getNodeType() == Node.ELEMENT_NODE & childNode.getNodeName().equalsIgnoreCase(tagName)){
                    nodeList.add(childNode);
                }
                childNode = childNode.getNextSibling();
            }
            if(! nodeList.isEmpty())
                return nodeList;
            else
                throw new DOMUtilException("No child nodes with tag name " + tagName + " found!");
        } catch (DOMUtilException e){
            throw e;
        }
    }

    /**
     * The method adds a child element to an existing node.
     *
     * @param node    DOM node to which the child element will be added
     * @param tagName Tag name of the child element
     * @return Returns the newly created child element
     */
    public static Element createChildElement(Node node, String tagName) {
            Document doc = node.getOwnerDocument();
            Element childElement = doc.createElement(tagName);
            node.appendChild(childElement);
            return childElement;
    }

    /**
     * The method adds a child element to the root node of a document
     *
     * @param doc     DOM document
     * @param tagName Tag name of the child element
     * @return Returns the newly created child element
     */
    public static Element createChildElement(Document doc, String tagName) {
            Element childElement = doc.createElement(tagName);
            doc.appendChild(childElement);
            return childElement;
    }

    /**
     * The method assigns a value, i.e. text, to an existing element
     *
     * @param element      Element to which the value will be added
     * @param elementValue Value to be added
     */
    public static void setElementValue(Element element, String elementValue) {
            Document doc = element.getOwnerDocument();
            element.appendChild(doc.createTextNode(elementValue));
    }

    /**
     * This method creates and adds a new attribute to an element.
     *
     * @param element  Element to which the attribute wil be added
     * @param attrName Name of the attribute
     * @return Returns the newly created attribute
     */
    public static Attr createAttribute(Element element, String attrName) {
            Document doc = element.getOwnerDocument();
            Attr attr = doc.createAttribute(attrName);
            element.setAttributeNode(attr);
            return attr;
    }
}