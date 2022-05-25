package com.featureprobe.api.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
public class SdkKeyGenerateUtil {

    public static String getServerSdkKey() {
        return "server-" + DigestUtils.sha1Hex(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    public static String getClientSdkKey() {
        return "client-" + DigestUtils.sha1Hex(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

}
