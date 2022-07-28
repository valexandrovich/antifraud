package ua.com.solidity.common.parsers.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import ua.com.solidity.common.ConvertEncodingInputStream;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.FilteredTextInputStream;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.data.JsonDataObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;


@CustomLog
public class JSONParser extends CustomParser {
    private static final String BASE_ENCODING = "UTF-8";
    private static final int QUEUE_FIELD = 0;
    private static final int QUEUE_ARRAY = 1;
    private static final int QUEUE_OBJECT = 2;

    private FilteredTextInputStream mainStream;
    private JsonParser parser;
    private JsonNode lastNode = null;
    private JsonLocation lastLocation = null;
    private final JSONParams params;
    private final Deque<Integer> stack = new LinkedList<>();

    public JSONParser(JSONParams params) {
        this.params = params;
    }

    @Override
    protected boolean doOpen() {
        Charset charset = params.getCharset();
        ConvertEncodingInputStream baseStream = new ConvertEncodingInputStream(stream, charset, StandardCharsets.UTF_8);
        mainStream = new FilteredTextInputStream(baseStream,8192);
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(mainStream);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean doLookupNextValue() {
        JsonLocation location = parser.getCurrentLocation();
        if (parser.hasCurrentToken() && parser.currentToken() != JsonToken.END_ARRAY) {
            try {
                lastNode = Utils.getSortedMapper().readTree(parser);
                lastLocation = location;
                return true;
            } catch (JsonProcessingException e) {
                location = e.getLocation();
                log.debug("Parsing error: position: (row: {}, col: {}, charOffset: {}, byteOffset: {})\n{}",
                        location.getLineNr(), location.getColumnNr(), location.getCharOffset(), location.getByteOffset(),
                        mainStream.getInfoNearLocation((long) location.getLineNr() - 1,(long) location.getColumnNr() - 1,
                                0, BASE_ENCODING), e);
                errorReporting(mainStream.getErrorReport(location, BASE_ENCODING));
            } catch (Exception e) {
                log.error("JSONParser lookup value error.", e);
                errorReporting(mainStream.getErrorReport(location, BASE_ENCODING));
            }
        }
        return false;
    }

    private void stackPopEndItem(int item) {
        if (!stack.isEmpty() && stack.peekLast() == item) {
            stack.removeLast();
            if (item == QUEUE_FIELD) {
                params.pop();
            } else if (!stack.isEmpty() && stack.peekLast() == QUEUE_FIELD) {
                params.pop();
                stack.removeLast();
            }
        }
    }

    private boolean readNode() {
        try {
            while (parser.nextToken() != null) {
                if (params.isReady() && !stack.isEmpty() && stack.peekLast() == QUEUE_ARRAY && parser.hasCurrentToken() && doLookupNextValue()) {
                    return true;
                }
                switch (parser.currentToken()) {
                    case FIELD_NAME:
                        stack.addLast(QUEUE_FIELD);
                        params.push(parser.currentName());
                        break;

                    case START_ARRAY:
                        stack.addLast(QUEUE_ARRAY);
                        if (params.isReady()) {
                            parser.nextToken();
                            return doLookupNextValue();
                        }
                        break;
                    case END_ARRAY:
                        stackPopEndItem(QUEUE_ARRAY);
                        break;

                    case START_OBJECT:
                        stack.addLast(QUEUE_OBJECT);
                        break;

                    case END_OBJECT:
                        stackPopEndItem(QUEUE_OBJECT);
                        break;

                    case NOT_AVAILABLE:
                        break;

                    default:    // in all other
                        stackPopEndItem(QUEUE_FIELD);
                        break;
                }
            }
        } catch (Exception e) {
            log.error("Parsing error.", e);
        }
        return false;
    }

    @Override
    protected DataObject internalDataObject() {
        return JsonDataObject.create(null, lastNode, lastLocation.getLineNr(), lastLocation.getColumnNr(), lastLocation.getByteOffset(), lastLocation.getCharOffset());
    }

    @Override
    protected boolean doNext() {
        return readNode();
    }
}
