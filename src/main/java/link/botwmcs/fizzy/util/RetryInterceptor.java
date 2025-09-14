package link.botwmcs.fizzy.util;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class RetryInterceptor implements Interceptor {
    private final int maxRetries;
    public RetryInterceptor(int maxRetries) {
        this.maxRetries = Math.max(0, maxRetries);
    }

    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        int attempt = 0;
        IOException lastIo = null;
        while (true) {
            try {
                Response response = chain.proceed(request);
                // 成功：直接返回
                if (response.isSuccessful()) {
                    return response;
                }
                // 429 / 503 ：若带 Retry-After 则等待（优先级高于默认策略）
                int code = response.code();
                if ((code == 429 || code == 503)) {
                    long retryAfter = parseRetryAfterMillis(response);
                    if (retryAfter >= 0 && attempt < maxRetries) {
                        response.close();
                        sleepQuietly(Math.min(retryAfter, 120_000L));
                        attempt++;
                        continue; // 再试
                    }
                }

                // 5xx（服务器错误）可重试
                if (code >= 500 && code < 600 && attempt < maxRetries) {
                    response.close();
                    sleepQuietly(backoffMillis(++attempt));
                    continue;
                }

                // 其他情况：不重试
                return response;
            } catch (IOException ioe) {
                lastIo = ioe;
                if (attempt >= maxRetries) throw lastIo;
                // 连接类异常、超时等：指数退避后再试
                sleepQuietly(backoffMillis(++attempt));
            }
        }
    }


    // utils
    private static long parseRetryAfterMillis(Response r) {
        String v = r.header("Retry-After");
        if (v == null) return -1L;
        v = v.trim();
        // 仅解析秒值（HTTP-date 解析较复杂，必要时可扩展）
        try {
            long seconds = Long.parseLong(v);
            if (seconds < 0) return -1L;
            return seconds * 1000L;
        } catch (NumberFormatException ignore) {
            return -1L;
        }
    }

    private static long backoffMillis(int attempt) {
        long base = 200L * (1L << Math.min(6, attempt)); // 最多放大到 12.8s
        double jitter = 0.5 + Math.random(); // 0.5x ~ 1.5x 抖动
        long delay = (long) (base * jitter);
        return Math.min(delay, 15_000L);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



}
