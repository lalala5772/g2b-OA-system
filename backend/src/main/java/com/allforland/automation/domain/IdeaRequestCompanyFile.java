package com.allforland.automation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Audit/replay snapshot of which COMPANY_FILES were referenced when an IdeaRequest ran — not user-selectable. */
@Entity
@Table(name = "idea_request_company_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdeaRequestCompanyFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_request_id", nullable = false)
	private IdeaRequest ideaRequest;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_file_id", nullable = false)
	private CompanyFile companyFile;

	public IdeaRequestCompanyFile(IdeaRequest ideaRequest, CompanyFile companyFile) {
		this.ideaRequest = ideaRequest;
		this.companyFile = companyFile;
	}
}
