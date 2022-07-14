package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.common.ActionObject;
import ua.com.solidity.common.DBUtils;
import ua.com.solidity.common.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

@Getter
@Setter
@CustomLog
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class DownloaderCustomDropRevisionAction extends ActionObject {
    private String revisionField = "revision";
    private String revisionGroupField = "revision_group";
    private String[] tables = null;
    private String[] tablesWithGroup = null;

    @JsonIgnore
    protected Connection connection = null;
    @JsonIgnore
    private boolean connectionRequested = false;
    @JsonIgnore
    protected UUID currentRevision;

    protected final boolean connectionNeeded() {
        if (connection != null) return true;
        if (connectionRequested) return false;
        try {
            connection = DBUtils.createConnection("spring.datasource", null);
        } catch (Exception e) {
            log.error("Connection to database not established.", e);
            return false;
        }
        return true;
    }

    protected void connectionRelease() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // nothing
            }
            connection = null;
        }
    }

    private void dropRevision(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement("select 1 from import_revision_remove(?);")) {
            statement.setObject(1, uuid);
            statement.execute();
        } catch (Exception e) {
            log.error("Remove revision error.", e);
        }
    }

    private void dropTableRows(String table) {
        try (PreparedStatement statement = connection.prepareStatement(Utils.messageFormat("delete from \"{}\" where \"{}\" = ?;", table, revisionField))) {
            statement.setObject(1, currentRevision);
            statement.execute();
        } catch (Exception e) {
            log.error("Can't remove data from table {}.", table, e);
        }
    }

    private void dropTableWithGroupRows(String table) {
        try (PreparedStatement statement = connection.prepareStatement(
                Utils.messageFormat("delete from {} t using import_revision_group g where t.{} = g.id and g.revision = ?;",
                        table, revisionGroupField))) {
            statement.setObject(1, currentRevision);
            statement.execute();
        } catch (Exception e) {
            log.error("Can't remove data from table {}.", table, e);
        }
    }

    protected abstract boolean downloaderActionExecute();

    private void doExecuteDropRevision() {
        if (tables != null) {
            for (var table : tables) {
                dropTableRows(table);
            }
        }

        if (tablesWithGroup != null) {
            for (var table: tablesWithGroup) {
                dropTableWithGroupRows(table);
            }
        }

        dropRevision(currentRevision);
    }

    @Override
    protected boolean doExecute() {
        if (connectionNeeded()) {
            try {
                if (downloaderActionExecute() && currentRevision != null) {
                    doExecuteDropRevision();
                }
                return true;
            } finally {
                connectionRelease();
            }
        }
        return false;
    }
}
