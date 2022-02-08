package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Table(name = "role_map")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoleMap {

    @Id
    @NotBlank(message = "Should be not blank")
    @Column(name = "dn")
    private String dn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
}
