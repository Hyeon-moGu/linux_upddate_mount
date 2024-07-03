package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LinuxOfflineUpdater {

    public static void main(String[] args) {
        String configFile = args.length > 0 ? args[0] : "config.txt";

        Properties props = loadProperties(configFile);
        String isoPath = props.getProperty("ISO_PATH");
        String mountPath = props.getProperty("MOUNT_PATH");
        String repoFilePath = props.getProperty("REPO_FILE_PATH");

        if (isoPath == null || mountPath == null || repoFilePath == null) {
            System.out.println("Invalid settings.");
            return;
        }

        try {
            executeCommand("sudo mount -o loop " + isoPath + " " + mountPath);

            Path repoDir = Paths.get(repoFilePath);
            Path backupDir = repoDir.resolve("backup");
            if (!Files.exists(backupDir)) {
                Files.createDirectory(backupDir);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(repoDir, "*.repo")) {
                for (Path entry : stream) {
                    Files.move(entry, backupDir.resolve(entry.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            String repoContent = "[local-media-baseos]\n" +
                    "name=Rocky Linux 9.4 Local Media - BaseOS\n" +
                    "baseurl=file://" + mountPath + "/BaseOS\n" +
                    "enabled=1\n" +
                    "gpgcheck=0\n" +
                    "gpgkey=file://" + mountPath + "/RPM-GPG-KEY-rockyofficial\n" +
                    "\n" +
                    "[local-media-appstream]\n" +
                    "name=Rocky Linux 9.4 Local Media - AppStream\n" +
                    "baseurl=file://" + mountPath + "/AppStream\n" +
                    "enabled=1\n" +
                    "gpgcheck=0\n" +
                    "gpgkey=file://" + mountPath + "/RPM-GPG-KEY-rockyofficial";

            Files.write(repoDir.resolve("local-media.repo"), repoContent.getBytes());

            executeCommand("sudo dnf clean all");
            executeCommand("sudo dnf --disablerepo='*' --enablerepo='local-media-baseos' --enablerepo='local-media-appstream' makecache");
            executeCommand("sudo dnf --disablerepo='*' --enablerepo='local-media-baseos' --enablerepo='local-media-appstream' upgrade");

            executeCommand("sudo umount " + mountPath);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir, "*.repo")) {
                for (Path entry : stream) {
                    Files.move(entry, repoDir.resolve(entry.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            System.out.println("Update completed successfully.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get(fileName))) {
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return props;
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        process.waitFor();
    }
}
