package link.botwmcs.fizzy.util;

import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EasyImagesClient implements AutoCloseable {
    public static final String DEFAULT_UPLOAD_PATH = "/api/index.php";

    private final String baseUrl; // 例如：http://127.0.0.1
    private final String uploadPath; // 例如：/api/index.php
    private final Supplier<String> tokenSupplier;

    private final OkHttpClient client;

    /**
     * 简便构造：默认路径与超时、最多重试 2 次（仅对 IO 异常与 5xx）。
     */
    public EasyImagesClient(String baseUrl, Supplier<String> tokenSupplier) {
        this(baseUrl, DEFAULT_UPLOAD_PATH, tokenSupplier, Duration.ofSeconds(5), Duration.ofSeconds(30), 2);
    }

    /**
     * 完整构造。
     * @param baseUrl 基址，如 http://127.0.0.1（不以 / 结尾）
     * @param uploadPath 上传路径，默认 /api/index.php
     * @param tokenSupplier 提供 token 的函数（可从配置、文件或缓存获取）
     * @param connectTimeout 连接超时
     * @param callTimeout 整体调用超时（含重试）
     * @param maxRetries 最大重试次数（仅对 IOException 与 5xx；4xx 不重试）
     */
    public EasyImagesClient(String baseUrl,
                            String uploadPath,
                            Supplier<String> tokenSupplier,
                            Duration connectTimeout,
                            Duration callTimeout,
                            int maxRetries) {
        this.baseUrl = trimTrailingSlash(Objects.requireNonNull(baseUrl, "baseUrl"));
        this.uploadPath = ensureLeadingSlash(uploadPath == null ? DEFAULT_UPLOAD_PATH : uploadPath);
        this.tokenSupplier = Objects.requireNonNull(tokenSupplier, "tokenSupplier");


        this.client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout)
                .readTimeout(callTimeout)
                .writeTimeout(callTimeout)
                .callTimeout(callTimeout)
                .retryOnConnectionFailure(true) // TCP 层自动重试
                .addInterceptor(new RetryInterceptor(maxRetries))
                .build();
    }

    // ========================= 上传 API =========================
    /** 同步上传本地文件（请在你自己的 IO 线程中调用）。 */
    public UploadResult upload(Path imageFile) throws IOException {
        Objects.requireNonNull(imageFile, "imageFile");
        byte[] data = Files.readAllBytes(imageFile);
        String filename = imageFile.getFileName().toString();
        String contentType = guessContentType(filename);
        return upload(data, filename, contentType);
    }


    /** 同步上传内存数据（请避免在 MC 主线程中调用）。 */
    public UploadResult upload(byte[] data, String filename, String contentType) throws IOException {
        Objects.requireNonNull(data, "data");
        if (filename == null || filename.isBlank()) filename = "upload.bin";
        if (contentType == null || contentType.isBlank()) contentType = guessContentType(filename);


        String token = tokenSupplier.get();
        MultipartBody.Builder mp = new MultipartBody.Builder(randomBoundary()).setType(MultipartBody.FORM)
                .addFormDataPart("token", token == null ? "" : token)
                .addFormDataPart("image", filename,
                        RequestBody.create(data, MediaType.parse(contentType)));


        Request req = new Request.Builder()
                .url(this.baseUrl + this.uploadPath)
                .header("User-Agent", "Fizzy/1.0 (EasyImagesClient)")
                .post(mp.build())
                .build();


        try (Response resp = client.newCall(req).execute()) {
            String body = resp.body() == null ? "" : resp.body().string();
            if (!resp.isSuccessful()) {
                return new UploadResult(false, null, resp.code(), body);
            }
            return new UploadResult(true, extractUrl(body), resp.code(), body);
        }
    }

    /** 异步上传（推荐：避免阻塞主线程）。 */
    public CompletableFuture<UploadResult> uploadAsync(byte[] data, String filename, String contentType) {
        CompletableFuture<UploadResult> fut = new CompletableFuture<>();
        String token = tokenSupplier.get();
        MultipartBody.Builder mp = new MultipartBody.Builder(randomBoundary()).setType(MultipartBody.FORM)
                .addFormDataPart("token", token == null ? "" : token)
                .addFormDataPart("image", filename == null ? "upload.bin" : filename,
                        RequestBody.create(data, MediaType.parse(contentType == null ? "application/octet-stream" : contentType)));


        Request req = new Request.Builder()
                .url(this.baseUrl + this.uploadPath)
                .header("User-Agent", "Fizzy/1.0 (EasyImagesClient)")
                .post(mp.build())
                .build();


        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { fut.completeExceptionally(e); }
            @Override public void onResponse(Call call, Response resp) {
                try (Response r = resp) {
                    String body = r.body() == null ? "" : r.body().string();
                    if (!r.isSuccessful()) {
                        fut.complete(new UploadResult(false, null, r.code(), body));
                    } else {
                        fut.complete(new UploadResult(true, extractUrl(body), r.code(), body));
                    }
                } catch (IOException e) {
                    fut.completeExceptionally(e);
                }
            }
        });
        return fut;
    }

    // ========================= 下载 API =========================
    /** 异步下载到文件（覆盖写入）。 */
    public CompletableFuture<Void> downloadToAsync(String url, Path dest) {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(dest, "dest");
        CompletableFuture<Void> fut = new CompletableFuture<>();
        Request req = new Request.Builder().url(url).get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { fut.completeExceptionally(e); }
            @Override public void onResponse(Call call, Response resp) {
                try (Response r = resp) {
                    if (!r.isSuccessful() || r.body() == null) {
                        fut.completeExceptionally(new IOException("HTTP " + r.code()));
                        return;
                    }
                    byte[] bytes = r.body().bytes();
                    Files.createDirectories(dest.getParent());
                    Files.write(dest, bytes);
                    fut.complete(null);
                } catch (IOException e) {
                    fut.completeExceptionally(e);
                }
            }
        });
        return fut;
    }


    // ========================= 工具方法 =========================
    private static String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }


    private static String randomBoundary() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder("FizzyOk");
        for (int i = 0; i < 20; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }


    private static String trimTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }


    private static String ensureLeadingSlash(String s) {
        return (s == null || s.isBlank()) ? DEFAULT_UPLOAD_PATH : (s.charAt(0) == '/' ? s : "/" + s);
    }


    private static final Pattern URL_PATTERN = Pattern.compile("(https?://[^\\s\"'<>]+)");
    private static final Pattern JSON_URL_FIELD = Pattern.compile("\"url\"\\s*:\\s*\"(https?://[^\\\"]+)\"");


    private static String extractUrl(String raw) {
        if (raw == null) return null;
        Matcher mJson = JSON_URL_FIELD.matcher(raw);
        if (mJson.find()) return mJson.group(1);
        Matcher m = URL_PATTERN.matcher(raw);
        if (m.find()) return m.group(1);
        return null;
    }


    @Override
    public void close() {
        // 主动清理资源（一般可不调，等待 GC 即可）
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        // cache 若有配置，也可一并关闭
    }


    // ========================= 数据类型 =========================
    public static final class UploadResult {
        public final boolean success;
        public final String url;
        public final int httpCode;
        public final String rawResponse;
        public UploadResult(boolean success, String url, int httpCode, String rawResponse) {
            this.success = success; this.url = url; this.httpCode = httpCode; this.rawResponse = rawResponse;
        }
        @Override public String toString() {
            return "UploadResult{" + "success=" + success + ", url='" + url + '\'' + ", httpCode=" + httpCode + ", rawResponseLen=" + (rawResponse == null ? 0 : rawResponse.length()) + '}';
        }
    }


    // ========================= Token 帮助函数 =========================
    /**
     * 简单示例：从 tokenList 文件中读取首个非空且非注释行作为 token。
     */
    public static Supplier<String> tokenFromFile(Path tokenList) {
        Objects.requireNonNull(tokenList, "tokenList");
        return () -> {
            try {
                for (String line : Files.readAllLines(tokenList, StandardCharsets.UTF_8)) {
                    String t = line.trim();
                    if (!t.isEmpty() && !t.startsWith("#")) return t;
                }
            } catch (IOException ignored) {}
            return "";
        };
    }
}
