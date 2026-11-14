import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class ImageStorage {
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
    private final Path rootDirectory;

    public ImageStorage(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public Path store(ImageMessage message) throws IOException {
        Path busDirectory = rootDirectory.resolve(message.getBusID());
        Files.createDirectories(busDirectory);
        String baseName = message.getTag() + "-" +
                FILE_TIMESTAMP.format(message.getTimestamp());
        Path imagePath = busDirectory.resolve(baseName + ".jpg");
        int suffix = 1;
        while (Files.exists(imagePath)) {
            imagePath = busDirectory.resolve(baseName + "-" + suffix++ + ".jpg");
        }
        Files.write(imagePath, message.getImage());
        return imagePath;
    }
}