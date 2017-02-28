package xebia.lcre.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class FileUtils {

    public static File createFile(Path parent, String cmdId, String filename) throws IOException {
        File outputFile = parent.resolve(String.format("%s-%s", cmdId, filename)).toFile();
        if (!outputFile.createNewFile()) {
            throw new RuntimeException(String.format("Unable to create file %s", outputFile));
        }
        return outputFile;
    }
}
