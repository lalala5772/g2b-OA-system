package com.allforland.automation.common;

/** Extension -> MIME type for company file downloads. Downloads are always sent as attachments, so this only affects the icon/app association after saving, not rendering. */
public class MimeTypes {

	private MimeTypes() {
	}

	public static String byExtension(String extension) {
		if (extension == null) {
			return "application/octet-stream";
		}
		return switch (extension.toLowerCase()) {
			case "pdf" -> "application/pdf";
			case "doc" -> "application/msword";
			case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			case "xls" -> "application/vnd.ms-excel";
			case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			case "hwp" -> "application/x-hwp";
			case "hwpx" -> "application/haansofthwpx";
			default -> "application/octet-stream";
		};
	}
}
