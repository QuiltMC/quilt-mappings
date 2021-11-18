package quilt.internal.decompile.quiltflower;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Manifest;

public class QuiltflowerResultSaver implements IResultSaver {
    private final Path outputPath;

    public QuiltflowerResultSaver(Path outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void saveFolder(String path) {
        Path folder = outputPath.resolve(path);
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory " + folder, e);
            }
        }
    }

    @Override
    public void copyFile(String source, String path, String entryName) {
    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        Path file = outputPath.resolve(path).resolve(entryName);
        try {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }

            Files.writeString(file, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write class file " + file, e);
        }
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest) {
    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName) {
    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entry) {
    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        saveClassFile(path, qualifiedName, entryName, content, null);
    }

    @Override
    public void closeArchive(String path, String archiveName) {
    }
}
