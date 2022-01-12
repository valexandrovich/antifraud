package ua.com.solidity.db.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.db.repositories.ImportRevisionGroupErrorRepository;

import javax.persistence.*;
import java.util.UUID;

@Slf4j
@Table(name = "import_revision_group_errors")
@Entity(name = "import_revision_group_errors")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImportRevisionGroupError extends CustomEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision_group", nullable = false)
    private UUID revisionGroup;

    @Column(name = "line")
    private Long line;

    @Column(name = "col")
    private Long col;

    @Column(name = "byte_offset")
    private Long byteOffset;

    @Column(name = "char_offset")
    private Long charOffset;

    @Column(name = "info_offset")
    private Long infoOffset;

    @Lob
    @Column(name = "info")
    private String info;

    @Column(name = "clarification")
    private String clarification;

    public static void saveError(UUID importRevisionGroupId, ErrorReport errorReport) {
        ImportRevisionGroupErrorRepository repository = lookupBean(ImportRevisionGroupErrorRepository.class);
        if (repository != null) {
            ImportRevisionGroupError importRevisionGroupError = new ImportRevisionGroupError();
            importRevisionGroupError.setId(UUID.randomUUID());
            importRevisionGroupError.setRevisionGroup(importRevisionGroupId);
            importRevisionGroupError.setLine(errorReport.getLocation().getRow());
            importRevisionGroupError.setCol(errorReport.getLocation().getCol());
            importRevisionGroupError.setByteOffset(errorReport.getLocation().getByteOffset());
            importRevisionGroupError.setCharOffset(errorReport.getLocation().getCharOffset());
            importRevisionGroupError.setInfoOffset(errorReport.getLocation().getInfoOffset());
            importRevisionGroupError.setInfo(errorReport.getInfo());
            importRevisionGroupError.setClarification(errorReport.getClarification());
            repository.save(importRevisionGroupError);
        }
    }
}