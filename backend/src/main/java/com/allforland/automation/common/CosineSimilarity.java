package com.allforland.automation.common;

import java.util.List;

public class CosineSimilarity {

	private CosineSimilarity() {
	}

	public static double of(List<Double> a, List<Double> b) {
		if (a.isEmpty() || b.isEmpty() || a.size() != b.size()) {
			return 0.0;
		}
		double dot = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < a.size(); i++) {
			dot += a.get(i) * b.get(i);
			normA += a.get(i) * a.get(i);
			normB += b.get(i) * b.get(i);
		}
		if (normA == 0.0 || normB == 0.0) {
			return 0.0;
		}
		return dot / (Math.sqrt(normA) * Math.sqrt(normB));
	}
}
