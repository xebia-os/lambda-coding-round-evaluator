package xebia.lcre.pkg.creators;

import java.nio.file.Path;

public class BuildPackage {
    private final String name;
    private final Path codeZipPath;
    private final String candidateId;

    public BuildPackage(String name, Path codeZipPath, String candidateId) {
        this.name = name;
        this.codeZipPath = codeZipPath;
        this.candidateId = candidateId;
    }

    public String getName() {
        return name;
    }

    public Path getCodeZipPath() {
        return codeZipPath;
    }

    public String getCandidateId() {
        return candidateId;
    }

    @Override
    public String toString() {
        return "BuildPackage{" +
                "name='" + name + '\'' +
                ", codeZipPath=" + codeZipPath +
                ", candidateId='" + candidateId + '\'' +
                '}';
    }
}
