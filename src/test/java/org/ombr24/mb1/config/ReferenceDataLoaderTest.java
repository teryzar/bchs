package org.ombr24.mb1.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ombr24.mb1.exception.FileOperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для ReferenceDataLoader
 */
class ReferenceDataLoaderTest {

    private final ReferenceDataLoader loader = new ReferenceDataLoader();

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadAllReferenceData() throws IOException {
        // Given
        Path dataDir = tempDir.resolve("data");
        Path configDir = tempDir.resolve("config");
        Files.createDirectories(dataDir);
        Files.createDirectories(configDir);

        Path positions70 = configDir.resolve("positions70.txt");
        Path positions100 = configDir.resolve("positions100.txt");
        Path commanders = configDir.resolve("commanders.txt");

        Files.write(positions70, List.of("70", "71", "72"));
        Files.write(positions100, List.of("100", "101", "102"));
        Files.write(commanders, List.of("1 мр:Командир1", "2 мр:Командир2"));

        AppConfig config = new AppConfig(
            dataDir.toString(),
            tempDir.resolve("output").toString(),
            configDir.toString()
        );

        // When
        ReferenceData data = loader.load(config);

        // Then
        assertThat(data.getPositions70()).containsExactly("70", "71", "72");
        assertThat(data.getPositions100()).containsExactly("100", "101", "102");
        assertThat(data.getCommanderPositions())
            .hasSize(2)
            .containsEntry("1 мр", "Командир1")
            .containsEntry("2 мр", "Командир2");
    }

    @Test
    void shouldIgnoreEmptyLines() throws IOException {
        // Given
        Path configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);

        Path positions70 = configDir.resolve("positions70.txt");
        Path positions100 = configDir.resolve("positions100.txt");
        Path commanders = configDir.resolve("commanders.txt");
        
        Files.write(positions70, List.of("70", "", "71", "  ", "72"));
        Files.write(positions100, List.of("100"));
        Files.write(commanders, List.of("1 мр:Командир"));

        AppConfig config = new AppConfig(
            tempDir.resolve("data").toString(),
            tempDir.resolve("output").toString(),
            configDir.toString()
        );

        // When
        ReferenceData data = loader.load(config);

        // Then
        assertThat(data.getPositions70())
            .hasSize(3)
            .containsExactly("70", "71", "72");
    }

    @Test
    void shouldTrimWhitespace() throws IOException {
        // Given
        Path configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);

        Path positions70 = configDir.resolve("positions70.txt");
        Path positions100 = configDir.resolve("positions100.txt");
        Path commanders = configDir.resolve("commanders.txt");
        
        Files.write(positions70, List.of("70"));
        Files.write(positions100, List.of("100"));
        Files.write(commanders, List.of("  1 мр  :  Командир1  "));

        AppConfig config = new AppConfig(
            tempDir.resolve("data").toString(),
            tempDir.resolve("output").toString(),
            configDir.toString()
        );

        // When
        ReferenceData data = loader.load(config);

        // Then
        assertThat(data.getCommanderPositions())
            .containsEntry("1 мр", "Командир1");
    }

    @Test
    void shouldHandleDuplicateKeys() throws IOException {
        // Given
        Path configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);

        Path positions70 = configDir.resolve("positions70.txt");
        Path positions100 = configDir.resolve("positions100.txt");
        Path commanders = configDir.resolve("commanders.txt");
        
        Files.write(positions70, List.of("70"));
        Files.write(positions100, List.of("100"));
        Files.write(commanders, List.of("1 мр:Командир1", "1 мр:Командир2"));

        AppConfig config = new AppConfig(
            tempDir.resolve("data").toString(),
            tempDir.resolve("output").toString(),
            configDir.toString()
        );

        // When
        ReferenceData data = loader.load(config);

        // Then - має зберегтись перше значення
        assertThat(data.getCommanderPositions().get("1 мр")).isEqualTo("Командир1");
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        // Given
        AppConfig config = new AppConfig(
            tempDir.resolve("nonexistent").toString(),
            tempDir.resolve("output").toString(),
            tempDir.resolve("nonexistent-config").toString()
        );

        // When/Then
        assertThatThrownBy(() -> loader.load(config))
            .isInstanceOf(FileOperationException.class)
            .hasMessageContaining("не знайдено");
    }

    @Test
    void shouldThrowExceptionOnInvalidMapFormat() throws IOException {
        // Given
        Path configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);

        Path positions70 = configDir.resolve("positions70.txt");
        Path positions100 = configDir.resolve("positions100.txt");
        Path commanders = configDir.resolve("commanders.txt");
        
        Files.write(positions70, List.of("70"));
        Files.write(positions100, List.of("100"));
        Files.write(commanders, List.of("InvalidLineWithoutColon"));

        AppConfig config = new AppConfig(
            tempDir.resolve("data").toString(),
            tempDir.resolve("output").toString(),
            configDir.toString()
        );

        // When
        ReferenceData data = loader.load(config);

        // Then - рядок без двокрапки має бути проігнорований
        assertThat(data.getCommanderPositions()).isEmpty();
    }

    @Test
    void shouldHandleEmptyFiles() throws IOException {
        // Given
        Path configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);

        Path positions70 = configDir.resolve("positions70.txt");
        Path positions100 = configDir.resolve("positions100.txt");
        Path commanders = configDir.resolve("commanders.txt");

        Files.write(positions70, List.of());
        Files.write(positions100, List.of());
        Files.write(commanders, List.of());

        AppConfig config = new AppConfig(
            tempDir.resolve("data").toString(),
            tempDir.resolve("output").toString(),
            configDir.toString()
        );

        // When
        ReferenceData data = loader.load(config);

        // Then
        assertThat(data.getPositions70()).isEmpty();
        assertThat(data.getPositions100()).isEmpty();
        assertThat(data.getCommanderPositions()).isEmpty();
    }
}
