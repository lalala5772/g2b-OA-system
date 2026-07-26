package com.allforland.automation.domain;

import jakarta.persistence.Column;
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

@Entity
@Table(name = "required_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequiredItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requirement_set_id", nullable = false)
	private RequirementSet requirementSet;

	@Column(name = "item_name", nullable = false)
	private String itemName;

	private String description;

	@Column(name = "is_matched", nullable = false)
	private boolean matched;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "matched_company_file_id")
	private CompanyFile matchedCompanyFile;

	@Column(name = "confidence_score")
	private Double confidenceScore;

	public RequiredItem(RequirementSet requirementSet, String itemName, String description) {
		this.requirementSet = requirementSet;
		this.itemName = itemName;
		this.description = description;
		this.matched = false;
	}

	public void applyMatch(CompanyFile companyFile, double confidenceScore) {
		this.matchedCompanyFile = companyFile;
		this.confidenceScore = confidenceScore;
		this.matched = true;
	}
}
