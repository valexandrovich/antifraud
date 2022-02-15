package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataExtension;
import java.util.UUID;

public class RevisionExtension extends DataExtension {
    public final UUID revision;

    public RevisionExtension(UUID revision) {
        super();
        this.revision = revision;
    }
}
