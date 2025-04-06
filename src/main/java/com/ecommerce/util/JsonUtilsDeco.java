package com.ecommerce.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public interface JsonUtilsDeco {
    Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();
    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();
    Gson snakeGson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

}
