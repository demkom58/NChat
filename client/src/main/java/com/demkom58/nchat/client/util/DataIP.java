package com.demkom58.nchat.client.util;

import com.demkom58.nchat.common.Environment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataIP {
    private static final Logger LOGGER = LoggerFactory.getLogger("DataIP");
    private static final File SERVER_LIST_FILE = new File(Environment.DATA_DIRECTORY, "srv.list");

    public static void saveIP(String ip) {
        final File file = createFileIfNotExist(SERVER_LIST_FILE);

        try {
            List<String> ips = Files.readAllLines(Paths.get(file.getPath()), StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            boolean exist = ips.contains(ip);

            for (String fip : ips) {
                writer.write(fip);
                writer.newLine();
            }

            if (!exist) writer.write(ip);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveIPList(List<String> ips) {
        try {
            final File file = createFileIfNotExist(SERVER_LIST_FILE);
            final FileWriter writer = new FileWriter(file);

            for (String str : ips)
                writer.write(str + "\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadIPList() {
        try {
            File file = createFileIfNotExist(SERVER_LIST_FILE);
            List<String> ips = new ArrayList<>();

            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) ips.add(scanner.nextLine());

            return ips;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static @NotNull File createFileIfNotExist(@NotNull final File file) {

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
