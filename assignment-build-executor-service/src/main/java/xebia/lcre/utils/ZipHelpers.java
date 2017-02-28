package xebia.lcre.utils;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public abstract class ZipHelpers {

    public static Path downloadZipTo(InputStream in, String filename, String dest) throws IOException {
        File assignmentZipFile = Paths.get(dest, filename).toFile();
        FileUtils.copyInputStreamToFile(in, assignmentZipFile);
        return assignmentZipFile.toPath();
    }

    public static Path unzipTo(Path srcZipPath, Path extractionDir) {
        ZipUtil.unpack(srcZipPath.toFile(), extractionDir.toFile());
        return extractionDir;
    }

    public static Path zip(Path zipPath, File rootDir, File... filesToZip) {
        File zip = zipPath.toFile();
        ZipUtil.pack(rootDir, zip);
        ZipUtil.addEntries(
                zip,
                Arrays.stream(filesToZip).map(f -> new FileSource(f.getName(), f)).toArray(ZipEntrySource[]::new)
        );
        return zipPath;
    }
}


