package com.smritisathi.network;

import com.smritisathi.network.dto.GameSessionRequest;
import com.smritisathi.network.dto.ScoreResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit API interface for cognitive scoring service.
 * Matches Section D's POST /api/score contract.
 */
public interface ScoringApiService {

    @POST("api/score")
    Call<ScoreResponse> calculateScore(@Body List<GameSessionRequest> sessions);
}
