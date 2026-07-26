package com.allforland.automation.dto;

/** One placeholder field in a DocumentTemplate. auto=true means it's filled from the company repository, not user input. */
public record DocumentFieldSchema(String key, String label, boolean auto) {
}
