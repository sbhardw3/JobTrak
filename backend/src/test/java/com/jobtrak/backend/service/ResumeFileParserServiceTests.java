package com.jobtrak.backend.service;

import com.jobtrak.backend.dto.ResumeUploadResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeFileParserServiceTests {

	private final ResumeFileParserService service = new ResumeFileParserService();

	@Test
	void parseTxtExtractsTextAndTitle() {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"ShivenResume.txt",
				"text/plain",
				"Java Spring React PostgreSQL".getBytes()
		);

		ResumeUploadResponse response = service.parse(file);

		assertThat(response.title()).isEqualTo("ShivenResume");
		assertThat(response.content()).isEqualTo("Java Spring React PostgreSQL");
	}

	@Test
	void parsePdfExtractsReadableText() throws IOException {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"ShivenResume.pdf",
				"application/pdf",
				createPdf("Spring Boot resume text")
		);

		ResumeUploadResponse response = service.parse(file);

		assertThat(response.title()).isEqualTo("ShivenResume");
		assertThat(response.content()).contains("Spring Boot resume text");
	}

	@Test
	void parseRejectsUnsupportedFileTypes() {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"resume.png",
				"image/png",
				"not a resume".getBytes()
		);

		assertThatThrownBy(() -> service.parse(file))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Unsupported resume file type");
	}

	private byte[] createPdf(String text) throws IOException {
		try (
				PDDocument document = new PDDocument();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
		) {
			PDPage page = new PDPage();
			document.addPage(page);

			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				contentStream.newLineAtOffset(72, 720);
				contentStream.showText(text);
				contentStream.endText();
			}

			document.save(outputStream);
			return outputStream.toByteArray();
		}
	}
}
