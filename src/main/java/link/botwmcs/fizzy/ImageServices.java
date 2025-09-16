package link.botwmcs.fizzy;

import link.botwmcs.fizzy.util.EasyImagesClient;

import java.time.Duration;

public final class ImageServices {
    public static EasyImagesClient IMAGES;

    public static void initImageClient() {
        IMAGES = new EasyImagesClient(
                Config.GALLERY_URL.get(),                           // 你的图床基址
                "/api/index.php",                             // 上传路径（默认就是这个）
                EasyImagesClient.tokenFromString(Config.TOKEN.get()),
                Duration.ofSeconds(5),                        // 连接超时
                Duration.ofSeconds(30),                       // 整体调用超时
                2                                             // 最大重试次数（仅 IO/5xx）
        );
    }

    public static void shutdown() {
        if (IMAGES != null) {
            IMAGES.close();                               // 可选：释放 OkHttp 资源
            IMAGES = null;
        }
    }

}
