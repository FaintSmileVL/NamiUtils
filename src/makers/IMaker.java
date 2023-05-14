package makers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    default void writeExceptionFile(File item, String content, Charset charset, String newPath, String innerPath) {
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
