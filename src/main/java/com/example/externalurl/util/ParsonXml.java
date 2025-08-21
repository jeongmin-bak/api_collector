@Slf4j
public class ParseXml {
    public static int reponseTotalCount(String xmlResponse, String tagName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;

        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));
            NodeList itemList = document.getElementsByTagName(tagName);

            if (itemList.getLength() > 0) {
                Node totalCountNode = itemList.item(0);
                return Integer.parseInt(totalCountNode.getTextContent());
            } else {
                log.error("Tag '{}' not found in the XML response", tagName);
                throw new RuntimeException(String.format("Tag '{%s}' not found in the XML response", tagName));
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> response(String xmlResponse, String tagName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;

        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));

            if (isSttsApiTblData(document)) {
                return processSttsApiTblData(document, tagName);
            }  else {
                return processDefaultResponse(document, tagName);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isSttsApiTblData(Document document) {
        return document.getDocumentElement().getTagName().equals("SttsApiTblData");
    }

    private static List<Map<String, Object>> processDefaultResponse(Document document, String tagName) {
        NodeList itemList = document.getElementsByTagName(tagName);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node itemNode = itemList.item(i);

            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                NodeList childNodes = itemElement.getChildNodes();
                Map<String, Object> data = new HashMap();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node child = childNodes.item(j);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        String columnName = child.getNodeName().toUpperCase();
                        String value = child.getTextContent().trim();
                        data.put(columnName, value.isEmpty() ? null : value);
                    }
                }
                dataList.add(data);
            }
        }
        return dataList;
    }

    private static List<Map<String, Object>> processSttsApiTblData(Document document, String tagName) {
        NodeList rowList = document.getElementsByTagName(tagName);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < rowList.getLength(); i++) {
            Node rowNode = rowList.item(i);

            if (rowNode.getNodeType() == Node.ELEMENT_NODE) {
                Element rowElement = (Element) rowNode;
                NodeList childNodes = rowElement.getChildNodes();
                Map<String, Object> rowData = new HashMap();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node child = childNodes.item(j);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        String columnName = child.getNodeName().toUpperCase();
                        String value = child.getTextContent().trim();
                        rowData.put(columnName, value.isEmpty() ? null : value);
                    }
                }
            }
        }
        return dataList;
    }
}