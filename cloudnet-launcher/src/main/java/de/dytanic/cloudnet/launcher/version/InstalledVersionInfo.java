package de.dytanic.cloudnet.launcher.version;


import java.nio.file.Path;

public class InstalledVersionInfo implements VersionInfo {

    protected Path targetDirectory;

    protected String gitHubRepository;

    protected String appVersion;

    protected long releaseTimestamp = -1;

    public InstalledVersionInfo(Path targetDirectory, String gitHubRepository) {
        this.targetDirectory = targetDirectory;
        this.gitHubRepository = gitHubRepository;

        String versionSpecification = targetDirectory.getFileName().toString();

        String[] versionParts = versionSpecification.split("-");

        if (versionParts.length > 1) {
            this.appVersion = versionParts[0] + "-" + versionParts[1];

            try {
                this.releaseTimestamp = versionParts.length > 2 ? Long.parseLong(versionParts[2]) : -1;
            } catch (NumberFormatException ignored) {
                this.releaseTimestamp = -1;
            }
        }

    }

    @Override
    public String getRepositoryVersion() {
        return null;
    }

    @Override
    public String getCurrentVersion() {
        return this.appVersion;
    }

    @Override
    public String getGitHubRepository() {
        return this.gitHubRepository;
    }

    @Override
    public long getReleaseTimestamp() {
        return this.releaseTimestamp;
    }

    @Override
    public Path getTargetDirectory() {
        return this.targetDirectory;
    }

}
