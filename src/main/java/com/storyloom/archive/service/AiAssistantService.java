package com.storyloom.archive.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiAssistantService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AiAssistantService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> getGlobalRecommendationStream(String userPrompt) {
        // Fetch up to 4 potential matching document fragments from the repository
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userPrompt)
                        .topK(4)
                        .build()
        );

        if (similarDocs.isEmpty()) {
            return Flux.just("Our archive is currently empty! Please ask an administrator to upload data files.");
        }

        String context = similarDocs.stream()
                .map(doc -> "Title: " + doc.getMetadata().get("title") + "\nExcerpt: " + doc.getText())
                .collect(Collectors.joining("\n\n---\n\n"));

        // Leveraging Llama 3.1's structured reasoning with clear contextual limitations
        String systemPrompt = """
            You are the expert digital archivist for the StoryLoom platform. Your goal is to guide users through our specific collections.
            
            CRITICAL SYSTEM BOUNDARIES:
            1. You have access ONLY to the books explicitly provided in the [LOCAL VECTOR DATABASE CONTEXT] section below.
            2. If the user asks for a category, genre, or concept (e.g., 'mythology', 'adventure') that is not closely represented within the [LOCAL VECTOR DATABASE CONTEXT], do not invent information. Instead, explicitly state that the platform archive does not currently carry matches for that specific category.
            3. Absolutely NEVER mention outside real-world popular book series (such as Percy Jackson, Lord of the Rings, etc.) unless they are physically written out in the text block below.
            4. Keep answers clean, objective, and accurate to the metadata.
            
            [LOCAL VECTOR DATABASE CONTEXT]:
            %s
            """.formatted(context);

        return chatClient.prompt().system(systemPrompt).user(userPrompt).stream().content();
    }

    public Flux<String> getReaderAssistanceStream(Long bookId, String bookTitle, String userPrompt, String currentTextOnScreen) {
        var b = new FilterExpressionBuilder();
        List<Document> bookSpecificDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userPrompt)
                        .topK(3)
                        .filterExpression(b.eq("bookId", bookId).build())
                        .build()
        );

        String context = bookSpecificDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Upgraded system prompt for the inline ebook sidebar copilot
        String systemPrompt = """
            You are a real-time Reading Companion integrated directly inside the eBook viewer for '%s'.
            The reader is interacting with a specific passage and looking for internal textual analysis.
            
            INLINE COMPLIANCE POLICIES:
            - Rely exclusively on the provided [CURRENT PASSAGE VIA UI] and the surrounding [COMPLEMENTARY CHAPTER CONTEXT].
            - Do not inject structural data, plot points, or external lore from outside this specific book file.
            - Answer questions regarding character tracking, vocabulary translation, and narrative summaries concisely.
            
            [CURRENT PASSAGE VIA UI]:
            "%s"
            
            [COMPLEMENTARY CHAPTER CONTEXT]:
            %s
            """.formatted(bookTitle, currentTextOnScreen, context);

        return chatClient.prompt().system(systemPrompt).user(userPrompt).stream().content();
    }
}