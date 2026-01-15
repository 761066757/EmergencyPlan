package com.emergency.util;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 通用工具类
 */
public class CommonUtils {

    /**
     * 获取当前请求对象
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 字符串转Long数组（逗号分隔）
     */
    public static Long[] strToLongArray(String str) {
        if (StrUtil.isBlank(str)) {
            return new Long[0];
        }
        String[] arr = str.split(",");
        Long[] result = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = Long.parseLong(arr[i].trim());
        }
        return result;
    }
}