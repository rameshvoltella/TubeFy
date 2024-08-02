package com.ramzmania.tubefy.data.remote.api;


import android.os.Build;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adds a custom {@code User-Agent} header to OkHttp requests.
 */
public class UserAgentInterceptor implements Interceptor {

    public final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    public UserAgentInterceptor() {
        this(getDefaultUserAgent());
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userAgentRequest = chain.request()
                .newBuilder()
                .header("User-Agent", userAgent)
                .build();

//        Log.d("OkHttp", String.format("--> Sending request %s on %s%n%s", userAgentRequest.url(), chain.connection(), userAgentRequest.headers()));
        return chain.proceed(userAgentRequest);
    }


    public static String getDefaultUserAgent() {
        try {
            StringBuilder result = new StringBuilder(64);
            result.append("Dalvik/");
            result.append(System.getProperty("java.vm.version")); // such as 1.1.0
            result.append(" (Linux; U; Android ");

            String version = Build.VERSION.RELEASE; // "1.0" or "3.4b5"
            result.append(version.length() > 0 ? version : "1.0");

            // add the model for the release build
            if ("REL".equals(Build.VERSION.CODENAME)) {
                String model = Build.MODEL;
                if (model.length() > 0) {
                    result.append("; ");
                    result.append(model);
                }
            }
            String id = Build.ID; // "MASTER" or "M4-rc20"
            if (id.length() > 0) {
                result.append(" Build/");
                result.append(id);
            }
            result.append(")");
            return result.toString();
        }
        catch (Exception e)
        {
            return String.format(Locale.US,
                    "%s/%s (Android %s; %s; %s %s; %s)",
                    Build.VERSION.CODENAME,
                    Build.ID,
                    Build.VERSION.RELEASE,
                    Build.MODEL,
                    Build.BRAND,
                    Build.DEVICE,
                    Locale.getDefault().getLanguage());
        }
    }
}
