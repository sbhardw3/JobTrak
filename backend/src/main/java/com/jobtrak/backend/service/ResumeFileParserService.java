package com.jobtrak.backend.service;

import com.jobtrak.backend.dto.ResumeUploadResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;

@Service
public class ResumeFileParserService {

	public ResumeUploadResponse parse(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a resume file");
		}

		String filename = file.getOriginalFilename() == null ? "Uploaded resume" : file.getOriginalFilename();
		String extension = getExtension(filename);
		String content = extractText(file, extension);

		if (content.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No readable text was found in this file");
		}

		return new ResumeUploadResponse(stripExtension(filename), content.trim());
	}

	private String extractText(MultipartFile file, String extension) {
		try {
			return switch (extension) {
				case "pdf" -> extractPdf(file);
				case "docx" -> extractDocx(file);
				case "doc" -> extractDoc(file);
				case "txt", "md", "text" -> new String(file.getBytes());
				default -> throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"Unsupported resume file type. Use PDF, DOC, DOCX, TXT, or MD"
				);
			};
		} catch (IOException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume file could not be read", ex);
		}
	}

	private String extractPdf(MultipartFile file) throws IOException {
		try (PDDocument document = Loader.loadPDF(file.getBytes())) {
			return new PDFTextStripper().getText(document);
		}
	}

	private String extractDocx(MultipartFile file) throws IOException {
		try (
				XWPFDocument document = new XWPFDocument(file.getInputStream());
				XWPFWordExtractor extractor = new XWPFWordExtractor(document)
		) {
			return extractor.getText();
		}
	}

	private String extractDoc(MultipartFile file) throws IOException {
		try (
				HWPFDocument document = new HWPFDocument(file.getInputStream());
				WordExtractor extractor = new WordExtractor(document)
		) {
			return extractor.getText();
		}
	}

	private String getExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == filename.length() - 1) {
			return "";
		}
		return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}

	private String stripExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 1) {
			return filename;
		}
		return filename.substring(0, dotIndex);
	}
}
