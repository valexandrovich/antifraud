package ua.com.solidity.dwh.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ArContragentID  implements Serializable {
	private static final long serialVersionUID = -393633213751066009L;
	@Column(name = "SITEID", nullable = false)
	private Long siteId;
	@Column(name = "ARCDATE",nullable = false , columnDefinition = "DATE")
	private LocalDate arcDate;
	@Column(name = "ID", nullable = false)
	private Long id;// Код контрагента

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		ArContragentID that = (ArContragentID) o;
		return siteId != null && Objects.equals(siteId, that.siteId)
				&& arcDate != null && Objects.equals(arcDate, that.arcDate)
				&& id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, arcDate, id);
	}
}
