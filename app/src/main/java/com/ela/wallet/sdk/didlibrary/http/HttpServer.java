package com.ela.wallet.sdk.didlibrary.http;

import com.ela.wallet.sdk.didlibrary.global.Constants;
import com.ela.wallet.sdk.didlibrary.utils.LogUtil;
import com.ela.wallet.sdk.didlibrary.utils.Utilty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
    /*这类就是要自定义一些返回值，我在这里定义了700。都属于自定义*/
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
        LogUtil.w("httpd serve");
        /*我在这里做了一个限制，只接受POST请求。这个是项目需求。*/
        if (Method.POST.equals(session.getMethod())) {
            Map<String, String> files = new HashMap<String, String>();
            /*获取header信息，NanoHttp的header不仅仅是HTTP的header，还包括其他信息。*/
            Map<String, String> header = session.getHeaders();

            try {
                /*这句尤为重要就是将将body的数据写入files中，大家可以看看parseBody具体实现，倒现在我也不明白为啥这样写。*/
                session.parseBody(files);
                /*看就是这里，POST请教的body数据可以完整读出*/
                String body = session.getQueryParameterString();
                LogUtil.d("header : " + header);
                LogUtil.d("body : " + body);
                /*这里是从header里面获取客户端的IP地址。NanoHttpd的header包含的东西不止是HTTP heaer的内容*/
                header.get("http-client-ip");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            /*这里就是为客户端返回的信息了。我这里返回了一个200和一个HelloWorld*/
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", "HelloWorld");
        }else {
            String uri = session.getUri();
            LogUtil.w("uri=" + uri);
            String params = session.getQueryParameterString();
            if (uri.startsWith("/did")) {
                String address = Utilty.getPreference(null, Constants.SP_KEY_DID_ADDRESS, "");
                return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"200\",\"did\":\"%s\"}", address));
            } else if (uri.startsWith("/banlence")) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", String.format("{\"status\":\"200\",\"result\":\"%s\"}", 0));
            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"200\"}");
        }
    }
}
