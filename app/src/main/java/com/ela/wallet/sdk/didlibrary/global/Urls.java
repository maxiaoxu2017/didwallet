package com.ela.wallet.sdk.didlibrary.global;

public class Urls {

    private static String SERVER_DID_DEBUG = "http://18.179.20.67:8080";
    private static String SERVER_DID_RELEASE = "http://hw-did-api.elastos.org:8080";
    private static String SERVER_WALLET_DEBUG = "http://18.179.207.38:8080";
    private static String SERVER_WALLET_RELEASE = "";

    public static String SERVER_DID = SERVER_DID_RELEASE;
    public static String SERVER_WALLET = SERVER_WALLET_RELEASE;

    public static String DID_BALANCE = "/api/1/balance/";//{address};
    public static String DID_CTX = "/api/1/createTx";
    public static String DID_CCT = "/api/1/createCrossTx";
    public static String DID_SRT = "/api/1/sendRawTx";

    public static String ELA_BALANCE = "/api/1/balance/";
    public static String ELA_CTX = "/api/1/createTx";
    public static String ELA_CCT = "/api/1/createCrossTx";
    public static String ELA_SRT = "/api/1/sendRawTx";

    static {
        if (Constants.isDebug) {
            SERVER_DID = SERVER_DID_DEBUG;
            SERVER_WALLET = SERVER_WALLET_DEBUG;
        } else {
            SERVER_DID = SERVER_DID_RELEASE;
            SERVER_WALLET = SERVER_WALLET_RELEASE;
        }
    }
}
