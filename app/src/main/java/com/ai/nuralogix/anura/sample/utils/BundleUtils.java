package com.ai.nuralogix.anura.sample.utils;

import android.os.Bundle;

import java.util.Map;

import ai.nuralogix.anurasdk.config.Configuration;

public class BundleUtils {

    public static final String DFX_BUNDLE_KEY = "DFX_BUNDLE_KEY";

    public static Bundle createRuntimeBundle(Configuration configuration) {
        Bundle bundle = new Bundle();

        Map<String, Object> runtimeConfigs = configuration.getRuntimeConfigs();
        for (String key : runtimeConfigs.keySet()) {
            Enum<?> runtimeKey = configuration.nullableRuntimeValueOf(key);
            if (runtimeKey != null) {
                bundle.putString(key, configuration.getRuntimeParameterString(runtimeKey));
            }
        }
        return bundle;
    }

    public static void updateRuntimeConfiguration(Configuration configuration, Bundle bundle) {
        for (String key : bundle.keySet()) {
            Enum<?> runtimeKey = configuration.nullableRuntimeValueOf(key);
            if (runtimeKey != null) {
                configuration.setRuntimeParameter(runtimeKey, bundle.getString(key));
            }
        }
    }
}
