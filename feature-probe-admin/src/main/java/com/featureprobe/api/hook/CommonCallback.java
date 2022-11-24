package com.featureprobe.api.hook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureprobe.api.base.hook.ICallback;
import com.featureprobe.api.base.model.CallbackResult;
import com.featureprobe.api.base.model.HookContext;
import com.featureprobe.api.mapper.HookContextMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component(value = "COMMON")
public class CommonCallback implements ICallback {

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionPool( new ConnectionPool(5, 5, TimeUnit.SECONDS))
            .connectTimeout(Duration.ofSeconds(3))
            .readTimeout(Duration.ofSeconds(3))
            .writeTimeout(Duration.ofSeconds(3))
            .retryOnConnectionFailure(true)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public CallbackResult callback(HookContext hookContext, String url) {
        CallbackResult result = new CallbackResult();
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String requestBodyStr = mapper.writeValueAsString(HookContextMapper.INSTANCE
                    .contextToRequestBody(hookContext));
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyStr);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = httpClient.newCall(request).execute();
            log.debug("Common Callback response： {}", response);
            result.setSuccess(response.isSuccessful());
            result.setRequestBody(requestBodyStr);
            result.setStatusCode(response.code());
            result.setResponseBody(response.body().string());
        } catch (Exception e) {
            log.error("Common Callback error", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setTime(new Date());
            return result;
        }
        result.setTime(new Date());
        return result;
    }

}
