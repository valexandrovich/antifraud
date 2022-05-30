package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

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
}
