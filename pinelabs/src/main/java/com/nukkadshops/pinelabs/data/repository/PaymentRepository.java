package com.nukkadshops.pinelabs.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.nukkadshops.pinelabs.data.models.CancelRequest;
import com.nukkadshops.pinelabs.data.models.CancelResponse;
import com.nukkadshops.pinelabs.data.models.StatusRequest;
import com.nukkadshops.pinelabs.data.models.StatusResponse;
import com.nukkadshops.pinelabs.data.models.UploadRequest;
import com.nukkadshops.pinelabs.data.models.UploadResponse;
import com.nukkadshops.pinelabs.data.models.VoidRequest;
import com.nukkadshops.pinelabs.data.models.VoidResponse;
import com.nukkadshops.pinelabs.data.network.ApiClient;
import com.nukkadshops.pinelabs.data.network.ApiService;
import com.nukkadshops.pinelabs.sdk.PaymentConfig;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {

    ApiService apiService;
    PaymentConfig confg;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable poller;

    public PaymentRepository(PaymentConfig config) {
        this.confg = config;
        apiService = ApiClient.getClient(config).create(ApiService.class);
    }

    public void upload(UploadRequest request, RepositoryCallback<UploadResponse> callback) {
        apiService.uploadResponseCall(request).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse res = response.body();
                callback.onComplete(res);
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                callback.onComplete(null);
            }
        });
    }

    public void status(StatusRequest request, MutableLiveData<StatusResponse> liveData) {
        apiService.statusResponseCall(request).enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                StatusResponse res = response.body();
                liveData.postValue(res);

                if (res != null) {
                    String msg = res.rm == null ? "" : res.rm.trim().toLowerCase();
                    if (msg.contains("approved") ||
                            msg.contains("failed") ||
                            msg.contains("invalid") ||
                            msg.contains("void") ||
                            msg.contains("voided")) {
                        stopStatusPolling();
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                liveData.postValue(null);
                handler.postDelayed(poller, 5000);
                stopStatusPolling();
            }
        });
    }


    public void startStatusPolling(long ptr, MutableLiveData<StatusResponse> liveData) {
        stopStatusPolling();

        poller = new Runnable() {
            @Override
            public void run() {
                StatusRequest req = new StatusRequest(
                        confg.getMerchantId(),
                        confg.getSecurityToken(),
                        confg.getStoreId(),
                        confg.getClientId(),
                        ptr
                );

                status(req, liveData);
                handler.postDelayed(this, 5000);
            }
        };

        handler.post(poller);
    }

    public void stopStatusPolling() {
        if (poller != null) {
            handler.removeCallbacks(poller);
            poller = null;
        }
    }

    public void cancel(CancelRequest request, RepositoryCallback<CancelResponse> callback) {
        apiService.cancelResponseCall(request).enqueue(new Callback<CancelResponse>() {
            @Override
            public void onResponse(Call<CancelResponse> call, Response<CancelResponse> response) {
                callback.onComplete(response.body());
            }

            @Override
            public void onFailure(Call<CancelResponse> call, Throwable t) {
                callback.onComplete(null);
            }
        });
    }

    public void voidTransaction(VoidRequest request, RepositoryCallback<VoidResponse> callback) {
        apiService.voidResponseCall(request).enqueue(new Callback<VoidResponse>() {
            @Override
            public void onResponse(Call<VoidResponse> call, Response<VoidResponse> response) {
                callback.onComplete(response.body());
            }

            @Override
            public void onFailure(Call<VoidResponse> call, Throwable t) {
                callback.onComplete(null);
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onComplete(T result);
    }
}
