package com.foodtraceability.customers.component;

import com.foodtraceability.customers.dto.CaptchaCodeDTO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class CaptchaManager {

    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int WIDTH = 130;
    private static final int HEIGHT = 48;
    private static final int CODE_LEN = 4;
    private static final long EXPIRE_MS = 5 * 60 * 1000;

    private final ConcurrentHashMap<String, CaptchaInfo> cache = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public CaptchaManager() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "captcha-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            cache.entrySet().removeIf(e -> now - e.getValue().createTime > EXPIRE_MS);
        }, 10, 10, TimeUnit.MINUTES);
    }

    public CaptchaCodeDTO generate() {
        String code = randomCode();
        String key = UUID.randomUUID().toString().replace("-", "");
        String imageBase64 = "data:image/png;base64," + renderImage(code);

        cache.put(key, new CaptchaInfo(code, System.currentTimeMillis()));

        CaptchaCodeDTO dto = new CaptchaCodeDTO();
        dto.setCaptchaKey(key);
        dto.setImageBase64(imageBase64);
        return dto;
    }

    public boolean verify(String key, String code) {
        if (key == null || code == null) return false;
        CaptchaInfo info = cache.remove(key);
        if (info == null) return false;
        if (System.currentTimeMillis() - info.createTime > EXPIRE_MS) return false;
        return info.code.equalsIgnoreCase(code.trim());
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LEN; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    private String renderImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        try {
            // background
            g.setColor(new Color(245, 248, 250));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // border
            g.setColor(new Color(200, 210, 220));
            g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

            // draw characters
            Font[] fonts = {
                    new Font("Arial", Font.BOLD, 28),
                    new Font("Arial", Font.ITALIC, 28),
                    new Font("Arial", Font.PLAIN, 27)
            };

            for (int i = 0; i < code.length(); i++) {
                g.setFont(fonts[random.nextInt(fonts.length)]);
                g.setColor(randomColor());
                char ch = code.charAt(i);

                // rotate
                double radian = Math.toRadians((random.nextDouble() - 0.5) * 30);
                int x = 15 + i * 28 + random.nextInt(6);
                int y = 30 + random.nextInt(8);

                g.translate(x, y);
                g.rotate(radian);
                g.drawString(String.valueOf(ch), 0, 0);
                g.rotate(-radian);
                g.translate(-x, -y);
            }

            // noise dots
            for (int i = 0; i < 60; i++) {
                g.setColor(randomColor());
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(HEIGHT);
                g.fillOval(x, y, 1, 1);
            }

            // interference lines
            for (int i = 0; i < 3; i++) {
                g.setColor(randomColor());
                int x1 = random.nextInt(20);
                int y1 = random.nextInt(HEIGHT);
                int x2 = WIDTH - random.nextInt(20);
                int y2 = random.nextInt(HEIGHT);
                g.drawLine(x1, y1, x2, y2);
            }

            // output
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("图形验证码生成失败", e);
        } finally {
            g.dispose();
        }
    }

    private Color randomColor() {
        return new Color(30 + random.nextInt(120), 30 + random.nextInt(120), 30 + random.nextInt(120));
    }

    private static class CaptchaInfo {
        String code;
        long createTime;

        CaptchaInfo(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
    }
}
