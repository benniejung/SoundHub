package com.yebin.sideproject.domain.track;

import com.yebin.sideproject.domain.track.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SongEmbeddingRunner implements CommandLineRunner {
    private final SongRepository songRepository;
    private final VectorStore vectorStore;

    @Override
    public void run(String... args) {
        songRepository.findByEmbeddedFalse().forEach(song -> {
            String text = """
                    제목: %s
                    아티스트: %s
                    가사: %s
                    응원법: %s
                    """.formatted(song.getTitle(), song.getArtist(), song.getLyrics(), song.getCheerVersion());

            Document doc = new Document(text, Map.of("type", "song", "songId", song.getId()));
            vectorStore.add(List.of(doc));
            song.markEmbedded();
            songRepository.save(song);
        });
    }
}

