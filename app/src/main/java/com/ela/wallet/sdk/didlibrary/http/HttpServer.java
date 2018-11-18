package com.ela.wallet.sdk.didlibrary.http;

import com.ela.wallet.sdk.didlibrary.global.Constants;
import com.ela.wallet.sdk.didlibrary.utils.LogUtil;
import com.ela.wallet.sdk.didlibrary.utils.Utilty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            if (uri.startsWith("/did")) {
                String address = Utilty.getPreference(null, Constants.SP_KEY_DID_ADDRESS, "");
                return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"200\",\"result\":\"%s\"}", address));
            } else if (uri.startsWith("/balance")) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"200\",\"result\":\"%s\"}", 0));
            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"200\"}");
        }
    }
}
