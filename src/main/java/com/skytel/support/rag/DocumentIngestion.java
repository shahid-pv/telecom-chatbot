package com.skytel.support.rag;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

/** Loads SkyTel reference files into the vector store when it is empty. */
@Configuration
public class DocumentIngestion {
	private static final Logger log = LoggerFactory.getLogger(DocumentIngestion.class);

	/** Creates the startup task that reads, splits, and stores support documents. */
	@Bean
	ApplicationRunner ingest(VectorStore store, JdbcTemplate jdbc) {
		return args -> {
			Integer count;
			try {
				count = jdbc.queryForObject("select count(*) from vector_store", Integer.class);
			} catch (Exception exception) {
				log.warn("Document ingestion skipped: vector_store is unavailable: {}",
						exception.getClass().getSimpleName());
				return;
			}
			if (count != null && count > 0) {
				return;
			}
			var resolver = new PathMatchingResourcePatternResolver();
			var splitter = TokenTextSplitter.builder().withChunkSize(700).withMinChunkSizeChars(350)
					.withMinChunkLengthToEmbed(5).withMaxNumChunks(10000).withKeepSeparator(true)
					.withPunctuationMarks(List.of('.', '?', '!', '\n')).build();
			List<Document> documents = new ArrayList<>();
			for (Resource resource : resolver.getResources("classpath:/docs/*")) {
				try {
					String filename = Objects.requireNonNullElse(resource.getFilename(), "document");
					String text;
					if (filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
						text = new PagePdfDocumentReader(resource).get().stream().map(Document::getText)
								.reduce("", (combinedText, pageText) -> combinedText + "\n" + pageText);
					} else if (filename.toLowerCase(Locale.ROOT).endsWith(".md")) {
						text = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
					} else {
						continue;
					}
					documents.addAll(splitter.apply(List.of(new Document(text, Map.of("source", filename)))));
				} catch (Exception exception) {
					log.warn("Could not read support document {}: {}", resource.getFilename(),
							exception.getClass().getSimpleName());
				}
			}
			for (int batchStartIndex = 0; batchStartIndex < documents.size(); batchStartIndex += 8) {
				boolean saved = false;
				for (int attempt = 0; attempt < 3 && !saved; attempt++) {
					try {
						store.add(documents.subList(batchStartIndex,
								Math.min(batchStartIndex + 8, documents.size())));
						saved = true;
					} catch (Exception exception) {
						if (attempt == 2) {
							log.warn("RAG ingestion failed for a batch after retries; app remains available: {}",
									exception.getClass().getSimpleName());
						} else {
							try {
								Thread.sleep(500L * (1L << attempt));
							} catch (InterruptedException interrupted) {
								Thread.currentThread().interrupt();
								return;
							}
						}
					}
				}
				if (!saved) {
					break;
				}
				try {
					Thread.sleep(250);
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			log.info("SkyTel document ingestion complete: {} chunks attempted", documents.size());
		};
	}
}
