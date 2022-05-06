package ua.com.solidity.statuslogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import org.junit.Test;
import ua.com.solidity.db.entities.StatusLogger;

import java.time.LocalDateTime;

public class DateTest {

	@Test
	public void whenSerializingJava8DateWithCustomSerializerThenCorrect()
			throws JsonProcessingException {

		LocalDateTime date = LocalDateTime.of(2014, 12, 20, 2, 30,30);
		StatusLogger entity = new StatusLogger();
		entity.setStarted(date);
		entity.setFinished(date);
		ObjectMapper mapper = new ObjectMapper();
		String result = mapper.writeValueAsString(entity);
		assertThat(result, containsString("2014.12.20 02:30:30"));
	}

	@Test
	public void whenDeserializingJava8DateWithCustomSerializerThenCorrect() throws JsonProcessingException {

		String date = "{\"progress\":1239, \"unit\":\"rows\", \"name\":\"Manual import from test.xlsx\", \"userName\":\"V.A.Bieloienko\", \"started\":\"2022.05.04 09:12:38\", \"finished\":\"2022.05.04 09:52:38\", \"status\":\"Imported: 1234, Failed: 5\"}";
		ObjectMapper mapper = new ObjectMapper();
		StatusLogger statusLogger = mapper.readValue(date, StatusLogger.class);
		assertThat(statusLogger.getStarted().toString(), containsString("2022-05-04T09:12:38"));
	}
}
