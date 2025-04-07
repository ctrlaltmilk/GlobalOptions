/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.ctrlaltmilk.globaloptions;

import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GlobalOptionsConfig {
    private static final List<String> DEFAULT_OPTIONS = List.of(
            "# GlobalOptions config",
            "# Lines prefixed by a '#' are comments",
            "# Other lines are options to store globally",
            "# Use a '!' to exclude options",
            "# A '*' at the end of an line will match all options starting with that line",
            "",
            "*",
            "!resourcePacks",
            "!incompatibleResourcePacks",
            "!key_key.*"
    );

    public final List<String> allowedOptions = new ArrayList<>();
    public final List<String> disallowedOptions = new ArrayList<>();

    private GlobalOptionsConfig() {}

    public static GlobalOptionsConfig load(File file) {
        GlobalOptionsConfig config = new GlobalOptionsConfig();

        if (!file.exists()) {
            try (BufferedWriter writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                for (String line : DEFAULT_OPTIONS) {
                    writer.append(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (BufferedReader reader = Files.newReader(file, StandardCharsets.UTF_8)) {
            reader.lines().forEach(line -> {
                if (line.startsWith("#") || line.isBlank()) {
                    return;
                }

                if (line.startsWith("!")) {
                    config.disallowedOptions.add(line.substring(1).strip());
                } else {
                    config.allowedOptions.add(line.strip());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return config;
    }
}
