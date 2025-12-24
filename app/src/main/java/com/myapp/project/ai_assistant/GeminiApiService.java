package com.myapp.project.ai_assistant;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    Call<GeminiModels.Response> getCompletion(
            @Query("key") String apiKey,
            @Body GeminiModels.Request request
    );
}