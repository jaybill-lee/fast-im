package org.jaybill.fast.im.user.center.service.impl;

import com.google.code.kaptcha.Producer;
import io.lettuce.core.SetArgs;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.user.center.consts.RedisKey;
import org.jaybill.fast.im.user.center.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private Producer captchaProducer;
    @Autowired
    private StatefulRedisClusterConnection<String, String> redisConnection;

    @Override
    public String createCaptchaBase64(String id) {
        AssertUtil.notNull(id);

        // create captcha image
        var text = captchaProducer.createText();
        var bufferedImage = captchaProducer.createImage(text);

        // convert to base64
        var outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (IOException e) {
            log.error("ImageIO write error, id:{}, e:", id, e);
            throw new RuntimeException(e);
        }
        var imageBytes = outputStream.toByteArray();
        var imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

        // bind id -> imageBase64
        redisConnection.sync().set(RedisKey.getCaptchaImage(id), text, new SetArgs().ex(Duration.ofMinutes(2)));
        return imageBase64;
    }
}
