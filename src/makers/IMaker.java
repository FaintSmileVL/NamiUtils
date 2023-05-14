package makers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * @author Nami
 * @date 14.05.2023
 * @time 11:52
 */
public interface IMaker {
    default void createDir(Path parent) {
        Path parentDir = Paths.get(parent.toUri()).getParent();
        try {
            if (!Files.exists(parentDir)) {
                createDir(parentDir);
            }
            if (!parent.toString().endsWith(".java")) {
                Files.createDirectory(parent);
            } else {
                Files.createFile(parent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void writeExceptionFile(String content, Charset charset, String exceptionPath) {
        try {
            Path path2 = FileSystems.getDefault().getPath(exceptionPath);
            if (!Files.exists(path2)) {
                Path parentDir = Paths.get(path2.toUri()).getParent();
                try {
                    if (!Files.exists(parentDir)) {
                        createDir(parentDir);
                    }
                    if (!path2.toString().endsWith(".txt")) {
                        Files.createDirectory(path2);
                    } else {
                        Files.createFile(path2);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Files.write(path2, content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void writeNormalFile(File item, String content, Charset charset, String newPath, String innerPath) {
        try {
            Path path2 = FileSystems.getDefault().getPath(item.getAbsolutePath().replace(innerPath, newPath));
            if (!Files.exists(path2)) {
                createDir(path2);
            }
            Files.write(path2, content.getBytes(charset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
