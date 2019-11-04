package com.chuckerteam.chucker.sample;

import android.app.Application;

import com.chuckerteam.chucker.api.RetentionManager;
import com.chuckerteam.chucker.api.config.ChuckerJavaConfig;
import com.chuckerteam.chucker.api.config.ErrorsFeature;
import com.chuckerteam.chucker.api.config.HttpFeature;
import com.chuckerteam.chucker.api.config.TabFeature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.chuckerteam.chucker.api.dsl.Configuration_dslKt.DEFAULT_MAX_CONTENT_LENGTH;

public class ChuckerJavaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        HashSet<String> headersToRedact = new HashSet<>();
        headersToRedact.add("Authorization");
        headersToRedact.add("Auth-Token");
        headersToRedact.add("User-Session");

        List<TabFeature> features = Arrays.asList(
                new HttpFeature(
                        true,
                        true,
                        RetentionManager.Period.ONE_HOUR,
                        DEFAULT_MAX_CONTENT_LENGTH,
                        headersToRedact
                ),
                new ErrorsFeature(
                        true,
                        true
                )
        );

        ChuckerJavaConfig.configure(features);
    }
}
