package ua.com.solidity.statuslogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ua.com.solidity.db.entities.StatusLogger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ua.com.solidity.statuslogger.listener.RabbitMQListener.trimStatus;

public class TrimTest {
    @Test
    public void shouldHandleLargeStatus() throws JsonProcessingException {
        String message = "{\"progress\":1239, \"unit\":\"rows\", \"name\":\"Manual import from test.xlsx\", \"userName\":\"V.A.Bieloienko\", \"started\":\"2022.05.04 09:12:38\", \"finished\":\"2022.05.04 09:52:38\", \"status\":\"ERROR: Too large string passed in status field where only 255 chars allowed according to database setup. You may count characters in this message - it has exactly 261 bytes, so the message parser will take only first 255 letters and you will not see these words\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        StatusLogger statusLogger;
        statusLogger = objectMapper.readerFor(StatusLogger.class).readValue(message);
        trimStatus(statusLogger);
        assertThat(statusLogger.getStatus(), hasLength(255));
    }

    @Test
    public void shouldHandleNullStatus() throws JsonProcessingException {
        String message = "{\"progress\":1239, \"unit\":\"rows\", \"name\":\"Manual import from test.xlsx\", \"userName\":\"V.A.Bieloienko\", \"started\":\"2022.05.04 09:12:38\", \"finished\":\"2022.05.04 09:52:38\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        StatusLogger statusLogger;
        statusLogger = objectMapper.readerFor(StatusLogger.class).readValue(message);
        trimStatus(statusLogger);
        assertThat(statusLogger.getStatus(), is(nullValue()));
    }
}
