package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@CustomLog
public class DownloaderDropLastRevisionAction extends DownloaderCustomDropRevisionAction {
    private String source = null;

    @Override
    protected boolean doValidate() {
        return source != null && !source.isBlank();
    }

    @Override
    protected boolean downloaderActionExecute() {
        try (PreparedStatement statement = connection.prepareStatement("select * from import_source_get_last_revision(?);")) {
            statement.setString(1, source);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                currentRevision = res.getObject(1, UUID.class);
            }
            res.close();
            return currentRevision != null;
        } catch (Exception e) {
            log.error("Source ({}) revision request failed.", source, e);
        }
        return false;
    }
}
