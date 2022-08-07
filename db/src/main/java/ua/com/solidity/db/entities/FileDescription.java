package ua.com.solidity.db.entities;

import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "file_description")
@Entity
public class FileDescription {

    @Id
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "description")
    private String description;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created")
    private Date created = new Date(System.currentTimeMillis());

    @Column(name = "wrong_column", length = 1500)
    private String wrongColumn;

    @Column(name = "validated")
    private boolean validated;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", referencedColumnName = "id")
    private ManualFileType type;
}
