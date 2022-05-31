/*
 * Copyright 2019 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.devcms.redisfront.ui.theme;

import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Karl Tauber
 */
public class ThemesManager {
    public static final List<ThemeInfo> bundledThemes = new ArrayList<>();
    public static final List<ThemeInfo> moreThemes = new ArrayList<>();

    static {
        loadBundledThemes();
        loadThemesFromDirectory();
    }

    @SuppressWarnings("unchecked")
    static void loadBundledThemes() {

        Map<String, Object> json;
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(ThemesManager.class.getResourceAsStream("themes.json")), StandardCharsets.UTF_8)) {
            json = (Map<String, Object>) Json.parse(reader);
        } catch (IOException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
            return;
        }

        for (Map.Entry<String, Object> e : json.entrySet()) {
            String resourceName = e.getKey();
            Map<String, String> value = (Map<String, String>) e.getValue();
            String name = value.get("name");
            boolean dark = Boolean.parseBoolean(value.get("dark"));
            String license = value.get("license");
            String licenseFile = value.get("licenseFile");
            String sourceCodeUrl = value.get("sourceCodeUrl");
            String sourceCodePath = value.get("sourceCodePath");
            bundledThemes.add(new ThemeInfo(name, resourceName, dark, license, licenseFile, sourceCodeUrl, sourceCodePath, null, null));
        }
    }

    static void loadThemesFromDirectory() {

        File directory = new File("").getAbsoluteFile();
        File[] themeFiles = directory.listFiles((dir, name) -> name.endsWith(".theme.json") || name.endsWith(".properties"));

        if (themeFiles == null)
            return;

        for (File file : themeFiles) {
            String fileName = file.getName();
            String name = fileName.endsWith(".properties")
                    ? StringUtils.removeTrailing(fileName, ".properties")
                    : StringUtils.removeTrailing(fileName, ".theme.json");
            moreThemes.add(new ThemeInfo(name, null, false, null, null, null, null, file, null));
        }
    }
}
