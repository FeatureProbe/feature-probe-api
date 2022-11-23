package com.featureprobe.api.base.model;

import com.featureprobe.api.base.hook.ICallback;
import com.featureprobe.api.base.hook.IHookRule;
import lombok.Data;

@Data
public class WebHook {

    private Long organizationId;

    private String name;

    private ICallback hook;

    private IHookRule rule;

    private String url;

    public void callback(HookContext hookContext) {
        if (rule.isHid(hookContext)) {
            CallbackResult result = hook.callback(hookContext, url);
            // 发送 Spring event

        }
    }

}
