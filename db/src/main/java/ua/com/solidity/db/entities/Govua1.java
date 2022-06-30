package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "govua_1")
public class Govua1 {
	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "revision")
	private UUID revision;

	@Column(name="portion_id")
	private UUID portionId;

	@Column(name = "record_date")
	private LocalDate recordDate;

	@Column(name = "record_no")
	private String recordNo;

	@Column(name = "record_type", length = 1024)
	private String recordType;

	@Column(name = "name")
	private String name;

	@Column(name = "edrpou", length = 128)
	private String edrpou;

	@Column(name = "court_name", length = 1024)
	private String courtName;

	@Column(name = "case_number")
	private String caseNumber;

	@Column(name = "start_date_auc")
	private LocalDate startDateAuc;

	@Column(name = "end_date_auc")
	private LocalDate endDateAuc;

	@Column(name = "end_registration_date")
	private LocalDate endRegistrationDate;
}