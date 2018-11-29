package com.ela.wallet.sdk.didlibrary.http;

import android.os.Process;
import android.text.TextUtils;

import com.ela.wallet.sdk.didlibrary.R;
import com.ela.wallet.sdk.didlibrary.bean.BalanceBean;
import com.ela.wallet.sdk.didlibrary.global.Constants;
import com.ela.wallet.sdk.didlibrary.global.Urls;
import com.ela.wallet.sdk.didlibrary.utils.DidLibrary;
import com.ela.wallet.sdk.didlibrary.utils.LogUtil;
import com.ela.wallet.sdk.didlibrary.utils.Utilty;
import com.ela.wallet.sdk.didlibrary.widget.DidAlertDialog;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
    public enum Status implements NanoHTTPD.Response.IStatus {
        SWITCH_PROTOCOL(101, "Switching Protocols"),
        NOT_USE_POST(700, "not use post");

        private final int requestStatus;
        private final String description;

        Status(int requestStatus, String description) {
            this.requestStatus = requestStatus;
            this.description = description;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public int getRequestStatus() {
            return 0;
        }
    }

    public HttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        LogUtil.w("httpd serve:");
        if (Method.POST.equals(session.getMethod())) {
            Map<String, String> files = new HashMap<String, String>();
            Map<String, String> header = session.getHeaders();
            try {
                session.parseBody(files);
                String body = session.getQueryParameterString();
                LogUtil.d("header : " + header);
                LogUtil.d("body : " + body);
                header.get("http-client-ip");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", "HelloWorld");
        }else {
            String uri = session.getUri();
            LogUtil.w("uri=" + uri);
            String params = session.getQueryParameterString();
            if (uri.startsWith("/api/v1/getDid")) {
                String did = Utilty.getPreference(Constants.SP_KEY_DID, "");
                if (TextUtils.isEmpty(did)) {
                    return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"500\",\"result\":\"internal error\"}"));
                } else {
                    return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"200\",\"result\":\"%s\"}", did));
                }
            } else if (uri.startsWith("/api/v1/getAddress")) {
                String candyAddress = Utilty.getPreference(Constants.SP_KEY_DID_ADDRESS, "");
                if (TextUtils.isEmpty(candyAddress)) {
                    return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"500\",\"result\":\"internal error\"}"));
                } else {
                    return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"200\",\"result\":\"%s\"}", candyAddress));
                }
            } else if (uri.contains("/api/v1/getBalance")) {
                String balance = dealWithBalance();
                return newFixedLengthResponse(Response.Status.OK, "application/json", balance);
            } else if (uri.startsWith("/api/v1/sendTransfer")) {

            } else if (uri.startsWith("/api/v1/getTxById")) {

            } else if (uri.startsWith("/api/v1/getAllTxs")) {

            } else if (uri.startsWith("/api/v1/setDidInfo")) {

            } else if (uri.startsWith("/api/v1/getDidInfo")) {

            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"200\"}");
        }
    }

    /**
     * GET http://127.0.0.1:port/api/v1/getBalance
     * @return The current logined user balance. units is sela, 1 ela = 100000000 sela.
     */
    private String balanceResult;
    private String dealWithBalance() {
        LogUtil.d("dealWithBalance:" + Process.myTid());
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        String url = String.format("%s%s%s", Urls.SERVER_DID, Urls.DID_BALANCE, Utilty.getPreference(Constants.SP_KEY_DID_ADDRESS, ""));
        HttpRequest.sendRequestWithHttpURLConnection(url, new HttpRequest.HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                balanceResult = response;
                countDownLatch.countDown();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        try {
            countDownLatch.await(30, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtil.d("balanceResult==" + balanceResult);
        return balanceResult;
    }
}
