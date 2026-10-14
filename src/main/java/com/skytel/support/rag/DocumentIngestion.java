package com.skytel.support.rag;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.*;
import java.util.*;

@Configuration
public class DocumentIngestion {
	private static final Logger log = LoggerFactory.getLogger(DocumentIngestion.class);

	@Bean
	ApplicationRunner ingest(VectorStore store, JdbcTemplate jdbc) {
		return args -> {
			Integer count;
			try {
				count = jdbc.queryForObject("select count(*) from vector_store", Integer.class);
			} catch (Exception ex) {
				log.warn("Document ingestion skipped: vector_store is unavailable: {}", ex.getClass().getSimpleName());
				return;
			}
			if (count != null && count > 0)
				return;
			var resolver = new PathMatchingResourcePatternResolver();
			var splitter = new TokenTextSplitter(700, 350, 5, 10000, true, List.of('.', '?', '!', '\n'));
			List<Document> docs = new ArrayList<>();
			for (Resource r : resolver.getResources("classpath:/docs/*")) {
				try {
					String filename = Objects.requireNonNullElse(r.getFilename(), "document");
					String text;
					if (filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
						text = new org.springframework.ai.reader.pdf.PagePdfDocumentReader(r).get().stream()
								.map(Document::getText).reduce("", (a, b) -> a + "\n" + b);
					} else if (filename.toLowerCase(Locale.ROOT).endsWith(".md")) {
						text = new String(r.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
					} else
						continue;
					docs.addAll(splitter.apply(List.of(new Document(text, Map.of("source", filename)))));
				} catch (Exception e) {
					log.warn("Could not read support document {}: {}", r.getFilename(), e.getClass().getSimpleName());
				}
			}
			for (int i = 0; i < docs.size(); i += 8) {
				boolean saved = false;
				for (int attempt = 0; attempt < 3 && !saved; attempt++) {
					try {
						store.add(docs.subList(i, Math.min(i + 8, docs.size())));
						saved = true;
					} catch (Exception e) {
						if (attempt == 2)
							log.warn("RAG ingestion failed for a batch after retries; app remains available: {}",
									e.getClass().getSimpleName());
						else
							try {
								Thread.sleep(500L * (1L << attempt));
							} catch (InterruptedException interrupted) {
								Thread.currentThread().interrupt();
								return;
							}
					}
				}
				if (!saved)
					break;
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			log.info("SkyTel document ingestion complete: {} chunks attempted", docs.size());
		};
	}
}
