package ua.com.solidity.db.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.SneakyThrows;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class CustomDateDeserializer extends StdDeserializer<LocalDateTime> {

	private SimpleDateFormat formatter =
			new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	public CustomDateDeserializer() {
		this(null);
	}

	public CustomDateDeserializer(Class<?> vc) {
		super(vc);
	}

	@SneakyThrows
	@Override
	public LocalDateTime deserialize(JsonParser jsonparser, DeserializationContext context)
			throws IOException {
		String date = jsonparser.getText();
			return convertToLocalDateViaInstant(formatter.parse(date));
	}

	public LocalDateTime convertToLocalDateViaInstant(Date dateToConvert) {
		return dateToConvert.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}
}
