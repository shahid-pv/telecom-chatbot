package com.skytel.support.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** Public, read-only access to the support PDFs bundled with the application. */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
	private static final String DOCUMENT_PATTERN = "classpath:/docs/*.pdf";
	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	/** Lists the names and browser URLs of the available support PDFs. */
	@GetMapping
	public List<DocumentInfo> list() throws IOException {
		return Arrays.stream(resolver.getResources(DOCUMENT_PATTERN))
				.map(resource -> new DocumentInfo(Objects.requireNonNullElse(resource.getFilename(), "document.pdf"),
						"/api/documents/" + resource.getFilename()))
				.sorted(Comparator.comparing(DocumentInfo::name))
				.toList();
	}

	/** Serves a bundled PDF inline so browsers can display it in the document dialog. */
	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> read(@PathVariable String filename) throws IOException {
		Resource resource = Arrays.stream(resolver.getResources(DOCUMENT_PATTERN))
				.filter(candidate -> filename.equals(candidate.getFilename()))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.inline().filename(filename).build().toString())
				.body(resource);
	}

	public record DocumentInfo(String name, String url) {
	}
}
