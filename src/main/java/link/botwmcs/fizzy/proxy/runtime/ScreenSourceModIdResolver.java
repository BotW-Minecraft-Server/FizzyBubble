package link.botwmcs.fizzy.proxy.runtime;

import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ScreenSourceModIdResolver {
    private static final String MODS_TOML_PATH = "META-INF/neoforge.mods.toml";
    private static final Pattern MOD_ID_LINE = Pattern.compile("^\\s*modId\\s*=\\s*\"([a-z0-9_\\-]+)\"\\s*$");
    private static final int TOML_SEARCH_DEPTH = 8;
    private static final ConcurrentMap<Class<?>, Optional<String>> CACHE = new ConcurrentHashMap<>();

    private ScreenSourceModIdResolver() {
    }

    public static @Nullable String resolve(@Nullable Screen screen) {
        if (screen == null) {
            return null;
        }

        return CACHE.computeIfAbsent(screen.getClass(), ScreenSourceModIdResolver::resolveForClass).orElse(null);
    }

    private static Optional<String> resolveForClass(Class<?> screenClass) {
        if (screenClass.getName().startsWith("net.minecraft.")) {
            return Optional.of("minecraft");
        }

        Optional<String> fromCodeSource = resolveFromCodeSource(screenClass);
        if (fromCodeSource.isPresent()) {
            return fromCodeSource;
        }

        return resolveFromClassResource(screenClass);
    }

    private static Optional<String> resolveFromCodeSource(Class<?> screenClass) {
        try {
            if (screenClass.getProtectionDomain() == null) {
                return Optional.empty();
            }
            CodeSource codeSource = screenClass.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                return Optional.empty();
            }
            return resolveFromLocation(codeSource.getLocation());
        } catch (SecurityException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<String> resolveFromClassResource(Class<?> screenClass) {
        String resourceName = screenClass.getSimpleName() + ".class";
        URL classResource = screenClass.getResource(resourceName);
        if (classResource == null) {
            return Optional.empty();
        }
        String external = classResource.toExternalForm();
        if (!external.startsWith("jar:")) {
            return Optional.empty();
        }

        int splitIndex = external.indexOf("!/");
        if (splitIndex <= 4) {
            return Optional.empty();
        }

        String jarUri = external.substring(4, splitIndex);
        try {
            Path path = Paths.get(URI.create(jarUri));
            return readFirstModIdFromJar(path);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<String> resolveFromLocation(URL location) {
        Path path = toPath(location);
        if (path == null) {
            return Optional.empty();
        }

        if (Files.isRegularFile(path)) {
            return readFirstModIdFromJar(path);
        }
        if (Files.isDirectory(path)) {
            return findModsToml(path).flatMap(ScreenSourceModIdResolver::readFirstModIdFromFile);
        }
        return Optional.empty();
    }

    private static @Nullable Path toPath(URL url) {
        try {
            return Paths.get(url.toURI()).normalize();
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static Optional<Path> findModsToml(Path startDirectory) {
        Path current = startDirectory.normalize();
        for (int depth = 0; depth < TOML_SEARCH_DEPTH && current != null; depth++) {
            Path direct = current.resolve(MODS_TOML_PATH);
            if (Files.isRegularFile(direct)) {
                return Optional.of(direct);
            }

            Path buildResources = current.resolve("resources").resolve("main").resolve(MODS_TOML_PATH);
            if (Files.isRegularFile(buildResources)) {
                return Optional.of(buildResources);
            }

            Path srcResources = current.resolve("src").resolve("main").resolve("resources").resolve(MODS_TOML_PATH);
            if (Files.isRegularFile(srcResources)) {
                return Optional.of(srcResources);
            }

            current = current.getParent();
        }
        return Optional.empty();
    }

    private static Optional<String> readFirstModIdFromJar(Path jarPath) {
        if (jarPath == null || !Files.isRegularFile(jarPath)) {
            return Optional.empty();
        }
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            var entry = jarFile.getJarEntry(MODS_TOML_PATH);
            if (entry == null) {
                return Optional.empty();
            }
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                return readFirstModId(inputStream);
            }
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<String> readFirstModIdFromFile(Path modsTomlPath) {
        try (InputStream inputStream = Files.newInputStream(modsTomlPath)) {
            return readFirstModId(inputStream);
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<String> readFirstModId(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int commentIndex = line.indexOf('#');
                String content = (commentIndex >= 0 ? line.substring(0, commentIndex) : line).trim();
                if (content.isEmpty()) {
                    continue;
                }
                Matcher matcher = MOD_ID_LINE.matcher(content);
                if (matcher.matches()) {
                    String modId = matcher.group(1);
                    if (!modId.isBlank()) {
                        return Optional.of(modId);
                    }
                }
            }
            return Optional.empty();
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }
}
