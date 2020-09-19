package com.demkom58.nchat.client.repository;

import com.demkom58.nchat.common.Environment;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Repository
public class ServersRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger("DataIP");
    private final File serverListFile = new File(Environment.DATA_DIRECTORY, "srv.list");

    @SneakyThrows
    public void saveServer(String ip) {
        final File file = createFileIfNotExist(serverListFile);
        List<String> ips = Files.readAllLines(Paths.get(file.getPath()), StandardCharsets.UTF_8);
        if (ips.contains(ip))
            return;

        try (final BufferedWriter writer
                     = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(ip);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAllServers(List<String> ips) {
        final File file = createFileIfNotExist(serverListFile);

        try (final FileWriter writer = new FileWriter(file)) {
            for (String str : ips)
                writer.write(str + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public List<String> loadAllServer() {
        File file = createFileIfNotExist(serverListFile);
        return Files.readAllLines(Paths.get(file.getPath()), StandardCharsets.UTF_8);
    }

    private @NotNull File createFileIfNotExist(@NotNull final File file) {

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Can't create file in path " + file.getAbsolutePath(), e);
                return file;
            }
        }

        return file;

    }
}
