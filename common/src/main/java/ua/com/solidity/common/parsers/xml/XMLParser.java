package ua.com.solidity.common.parsers.xml;

import com.ctc.wstx.api.WstxInputProperties;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.FilteredTextInputStream;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.data.JsonDataObject;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

@Slf4j
public class XMLParser extends CustomParser {
    private FilteredTextInputStream mainStream;
    private XMLStreamReader reader;
    private final XMLInputFactory factory = XMLInputFactory.newInstance();
    private final XmlMapper mapper = Utils.getSortedXmlMapper();
    private JsonNode lastNode = null;
    private final XMLParams params;

    public XMLParser(XMLParams params) {
        this.params = params;
        factory.setProperty(WstxInputProperties.P_ALLOW_XML11_ESCAPED_CHARS_IN_XML10, true);
    }

    @Override
    protected boolean doOpen() {
        mainStream = new FilteredTextInputStream(stream,8192);
        try {
            reader = factory.createXMLStreamReader(mainStream);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void lookupNode() throws IOException {
        try {
            lastNode = mapper.readValue(reader, JsonNode.class);
        } catch (JsonParseException e) {
            JsonLocation location = e.getLocation();
            log.error("\nposition: (row: {}, col: {}, charOffset: {}, byteOffset: {})\n{}",
                    location.getLineNr(), location.getColumnNr(), location.getCharOffset(), location.getByteOffset(),
                    mainStream.getInfoNearLocation((long) location.getLineNr() - 1,(long) location.getColumnNr() - 1,
                            0, reader.getEncoding()), e);
            errorReporting(mainStream.getErrorReport(location, reader.getEncoding()));
        }
    }

    private boolean readNode() {
        try {
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (params.isReady()) {
                            lookupNode();
                            return true;
                        } else {
                            params.push(reader.getName().toString());
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        params.pop();
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            log.error("Parsing error.", e);
        }
        return false;
    }

    @Override
    public DataObject internalDataObject() {
        return JsonDataObject.create(null, lastNode);
    }

    @Override
    public boolean doNext() {
        return readNode();
    }
}
