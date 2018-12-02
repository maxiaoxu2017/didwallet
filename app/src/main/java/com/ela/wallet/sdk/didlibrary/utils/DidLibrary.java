package com.ela.wallet.sdk.didlibrary.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.ela.wallet.sdk.didlibrary.activity.DidLaunchActivity;
import com.ela.wallet.sdk.didlibrary.activity.HomeActivity;
import com.ela.wallet.sdk.didlibrary.global.Constants;
import com.ela.wallet.sdk.didlibrary.global.Urls;
import com.ela.wallet.sdk.didlibrary.http.HttpRequest;
import com.ela.wallet.sdk.didlibrary.service.DidService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.elastos.wallet.lib.ElastosWallet;
import org.elastos.wallet.lib.ElastosWalletDID;
import org.elastos.wallet.lib.ElastosWalletHD;
import org.elastos.wallet.lib.ElastosWalletSign;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DidLibrary {

    private static Context mContext;
    private static ElastosWallet.Data mSeed;
    private static int mSeedLen;

    private static String mPrivateKey;

    public static void openLog(boolean open) {
        if (open) {
            LogUtil.setLogLevel(LogUtil.VERBOSE);
        }
    }

    public static String init(Context context) {
        LogUtil.d("init");
        mContext = context;
        Utilty.setContext(context);
        Intent intent = new Intent();
        intent.setClass(context, DidService.class);
        context.startService(intent);
        if (!"true".equals(Utilty.getPreference(Constants.SP_KEY_DID_ISBACKUP, "false")) &&
                TextUtils.isEmpty(Utilty.getPreference(Constants.SP_KEY_DID_MNEMONIC, ""))) {
            GenrateMnemonic();
        }
        if (TextUtils.isEmpty(Utilty.getPreference(Constants.SP_KEY_DID, ""))) {
            Did();
        }
        return "init success";
    }

    public static void launch(Context context) {
        Intent intent = new Intent();
        if (context instanceof Application) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setClass(context, HomeActivity.class);
        context.startActivity(intent);
    }

    private static String GenrateMnemonic() {
        LogUtil.d("GenrateMnemonic");
        String message = "";
        String language = "";
        String words = "";
        String sp_lang = Utilty.getPreference(Constants.SP_KEY_APP_LANGUAGE, "");
        if ("chinese".equals(sp_lang)) {
            language = "chinese";
            words = FileUtil.readAssetsTxt(mContext, "ElastosWalletLib/mnemonic_chinese.txt");
        } else {
            language = "english";
            words = "";
        }


        String mnemonic = ElastosWallet.generateMnemonic(language, words);
        if (mnemonic == null) {
            String errmsg = "Failed to generate mnemonic.";
            LogUtil.e(errmsg);
            message += errmsg;
            return message;
        }
        Utilty.setPreference(Constants.SP_KEY_DID_MNEMONIC, mnemonic);
        message += "mnemonic: " + mnemonic + "\n";

//        mnemonic = "搅 退 未 晚 亮 盖 做 织 航 尘 阶 票";
//        language = "chinese";
        //中文助记词库
//        String allChineseWords = FileUtil.readAssetsTxt(mContext, "ElastosWalletLib/mnemonic_chinese.txt");
        mSeed = new ElastosWallet.Data();
        int ret = ElastosWallet.getSeedFromMnemonic(mSeed, mnemonic, language, words, "");
        if (ret <= 0) {
            String errmsg = "Failed to get seed from mnemonic. ret=" + ret + "\n";
            LogUtil.e(errmsg);
            message += errmsg;
            return message;
        }
        mSeedLen = ret;
        message += "seed: " + mSeed.buf + ", len: " + mSeedLen + "\n";

        String privateKey = ElastosWallet.getSinglePrivateKey(mSeed, mSeedLen);
        if (privateKey == null) {
            String errmsg = "Failed to generate privateKey.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        mPrivateKey = privateKey;
        message += "privateKey: " + privateKey + "\n";
        Utilty.setPreference(Constants.SP_KEY_DID_PRIVATEKEY, privateKey);
        LogUtil.d("privatekey=" + privateKey);

        String publicKey = ElastosWallet.getSinglePublicKey(mSeed, mSeedLen);
        if (publicKey == null) {
            String errmsg = "Failed to generate publicKey.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        Utilty.setPreference(Constants.SP_KEY_DID_PUBLICKEY, publicKey);
        message += "publicKey: " + publicKey + "\n";

        String address = ElastosWallet.getAddress(publicKey);
        if (address == null) {
            String errmsg = "Failed to get address.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        message += "address: " + address + "\n";
        Utilty.setPreference(Constants.SP_KEY_DID_ADDRESS, address);

        ElastosWallet.Data data = new ElastosWallet.Data();
        data.buf = new byte[]{0, 1, 2, 3, 4, 5};
        ElastosWallet.Data signedData = new ElastosWallet.Data();
        int signedLen = ElastosWallet.sign(privateKey, data, data.buf.length, signedData);
        if (signedLen <= 0) {
            String errmsg = "Failed to sign data.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }

        boolean verified = ElastosWallet.verify(publicKey, data, data.buf.length, signedData, signedLen);
        message += "verified: " + verified + "\n";

        message += "================================================\n";
        return message;
    }


    private String HDWalletAddress() {
        String message = "";

        ElastosWallet.Data masterPublicKey = ElastosWalletHD.getMasterPublicKey(mSeed, mSeedLen, ElastosWalletHD.COIN_TYPE_ELA);
        if (masterPublicKey == null) {
            String errmsg = "Failed to generate master publicKey.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        message += "masterPublicKey: " + masterPublicKey.buf + "\n";

        int count = 10;
        String[] privateKeys = new String[count];
        String[] publicKeys = new String[count];
        String[] addresses = new String[count];
        for (int idx = 0; idx < count; idx++) {
            privateKeys[idx] = ElastosWalletHD.generateSubPrivateKey(mSeed, mSeedLen, ElastosWalletHD.COIN_TYPE_ELA, ElastosWalletHD.INTERNAL_CHAIN, idx);
            publicKeys[idx] = ElastosWalletHD.generateSubPublicKey(masterPublicKey, ElastosWalletHD.INTERNAL_CHAIN, idx);
            addresses[idx] = ElastosWallet.getAddress(publicKeys[idx]);

            message += "addresses[" + idx + "]: " + addresses[idx] + "\n";
        }

        message += "================================================\n";
        return message;
    }

    private static String Did() {
        String message = "";

        ElastosWallet.Data idChainMasterPublicKey = ElastosWalletDID.getIdChainMasterPublicKey(mSeed, mSeedLen);
        if (idChainMasterPublicKey == null) {
            String errmsg = "Failed to generate id chain master publicKey.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        message += "idChainMasterPublicKey: " + idChainMasterPublicKey.buf + "\n";

        int count = 1;
        String[] privateKeys = new String[count];
        String[] publicKeys = new String[count];
        String[] dids = new String[count];
        for (int idx = 0; idx < count; idx++) {
            privateKeys[idx] = ElastosWalletDID.generateIdChainSubPrivateKey(mSeed, mSeedLen, 0, idx);
            publicKeys[idx] = ElastosWalletDID.generateIdChainSubPublicKey(idChainMasterPublicKey, 0, idx);
            dids[idx] = ElastosWalletDID.getDid(publicKeys[idx]);

            message += "dids[" + idx + "]: " + dids[idx] + "\n";
            Utilty.setPreference(Constants.SP_KEY_DID, dids[idx]);
        }

        message += "================================================\n";
        return message;
    }

    /**
     * 充值 ELA-DID
     */
    public static void testChongzhi() {
        String fromAddress = "ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4";
        String toAddress = Utilty.getPreference(Constants.SP_KEY_DID_ADDRESS, "");
        String param = String.format("  {\"inputs\":[\"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4\"],\"outputs\":[{\"addr\":\"%s\",\"amt\":100000000}]}", toAddress);
        LogUtil.d("chongzhi param=" + param);
        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_WALLET + Urls.ELA_CCT, param, new HttpRequest.HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                LogUtil.d("chongzhi response=" + response);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                        String signed = testSignTxData();
                        LogUtil.d("chongzhi signed data=" + signed);
                        String signdparam = String.format("{\"data\":\"%s\"}", signed);
                        LogUtil.d("chongzhi srt data=" + signdparam);
                        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_WALLET + Urls.ELA_SRT, signdparam, new HttpRequest.HttpCallbackListener() {
                            @Override
                            public void onFinish(final String response) {
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.d("chongzhi srt result:" + response);
                                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        });


    }

    /**
     * 提现 DID-ELA
     */
    public static void testTixian() {
        String toAddress = "ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4";
        String fromAddress = Utilty.getPreference(Constants.SP_KEY_DID_ADDRESS, "");
        String param = String.format("  {\"inputs\":[\"%s\"],\"outputs\":[{\"addr\":\"%s\",\"amt\":10}]}", fromAddress, toAddress);

        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_DID + Urls.DID_CCT, param, new HttpRequest.HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                        String signed = getSignedData(response);
                        LogUtil.d("signed data=" + signed);
                        String signdparam = String.format("{\"data\":\"%s\"}", signed);
                        LogUtil.d("signdparam data=" + signdparam);
                        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_DID + Urls.DID_SRT, signdparam, new HttpRequest.HttpCallbackListener() {
                            @Override
                            public void onFinish(final String response) {
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.d("tixian result:" + response);
                                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }
                });
                LogUtil.d("response=" + response);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    /**
     * 转账 DID-DID
     */
    public static void testZhuanzhang() {
        String fromAddress = "EYegRY3DQPUrD8igKzCaH19ZZAYN3DTeNF";
        String toAddress = "EMXabt61cDgaCXuFvx4NSvKt2t33JJqTxT";


//        ArrayList<String> arrayList = new ArrayList<>();
//        arrayList.add("EU3e23CtozdSvrtPzk9A1FeC9iGD896DdV");
//        JsonObject param = new JsonObject();
//        param.addProperty("inputs", arrayList.add(""));
//        param.addProperty("outputs", "[{\"addr\": \"EPzxJrHefvE7TCWmEGQ4rcFgxGeGBZFSHw\",\"amt\": 1000}]");
//        LogUtil.d("param:" + param.toString());
        String param = " {\"inputs\":[\"EYegRY3DQPUrD8igKzCaH19ZZAYN3DTeNF\"],\"outputs\":[{\"addr\":\"EMXabt61cDgaCXuFvx4NSvKt2t33JJqTxT\", \"amt\" :1000}]}";
        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_DID + Urls.DID_CTX, param, new HttpRequest.HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                LogUtil.d("response:" + response);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                        String signed = getSignedData(response);
                        LogUtil.d("signed data=" + signed);
                        String signdparam = String.format("{\"data\":\"%s\"}", signed);
                        LogUtil.d("signdparam data=" + signdparam);
                        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_DID + Urls.DID_SRT, signdparam, new HttpRequest.HttpCallbackListener() {
                            @Override
                            public void onFinish(final String response) {
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.d("zhuanzhang result:" + response);
                                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        });

    }

    /**
     * 收款 DID-DID
     */
    public static void testSHoukuan() {
        String fromAddress = "EYegRY3DQPUrD8igKzCaH19ZZAYN3DTeNF";
        String toAddress = "EMXabt61cDgaCXuFvx4NSvKt2t33JJqTxT";


//        ArrayList<String> arrayList = new ArrayList<>();
//        arrayList.add("EU3e23CtozdSvrtPzk9A1FeC9iGD896DdV");
//        JsonObject param = new JsonObject();
//        param.addProperty("inputs", arrayList.add(""));
//        param.addProperty("outputs", "[{\"addr\": \"EPzxJrHefvE7TCWmEGQ4rcFgxGeGBZFSHw\",\"amt\": 1000}]");
//        LogUtil.d("param:" + param.toString());
        String param = " {\"inputs\":[\"EMXabt61cDgaCXuFvx4NSvKt2t33JJqTxT\"],\"outputs\":[{\"addr\":\"EYegRY3DQPUrD8igKzCaH19ZZAYN3DTeNF\", \"amt\" :1000}]}";
        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_DID + Urls.DID_CTX, param, new HttpRequest.HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                LogUtil.d("response:" + response);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                        String signed = "";//testCosignTxData();
                        LogUtil.d("signed data=" + signed);
                        String signdparam = String.format("{\"data\":\"%s\"}", signed);
                        LogUtil.d("signdparam data=" + signdparam);
                        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_DID + Urls.DID_SRT, signdparam, new HttpRequest.HttpCallbackListener() {
                            @Override
                            public void onFinish(final String response) {
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.d("shoukuan result:" + response);
                                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }


    private static String getSignedData(String origin) {
        ElastosWallet.Data data = new ElastosWallet.Data();
        data.buf = origin.getBytes();
        ElastosWallet.Data signedData = new ElastosWallet.Data();
        int signedLen = ElastosWallet.sign(mPrivateKey, data, data.buf.length, signedData);
//        int signedLen = ElastosWalletSign.generateRawTransaction(origin);
        if (signedLen <= 0) {
            String errmsg = "Failed to sign data.\n";
            LogUtil.e(errmsg);
        }
        testSignTxData();
        return Utilty.bytesToHexString(signedData.buf);
    }

    private static String testSignTxData() {
        String message = "";

//        String transaction = "{\"Transactions\":[{\"UTXOInputs\":[{"
//                + "\"txid\":\"f176d04e5980828770acadcfc3e2d471885ab7358cd7d03f4f61a9cd0c593d54\","
//                + "\"privateKey\":\"b6f010250b6430b2dd0650c42f243d5445f2044a9c2b6975150d8b0608c33bae\","
//                + "\"index\":0,\"address\":\"EeniFrrhuFgQXRrQXsiM1V4Amdsk4vfkVc\"}],"
//                + "\"Outputs\":[{\"address\":\"EbxU18T3M9ufnrkRY7NLt6sKyckDW4VAsA\","
//                + "\"amount\":1000000}]}]}";
//        String transaction = String.format("{\"Transactions\":[{\"UTXOInputs\":[{\"address\":\"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4\",\"txid\":\"583ca6c3780b3ba880b446c7ce5427e538a82fc185e54749e61805a97dc3b222\",\"index\":0, \"privateKey\":\"%s\"}],\"CrossChainAsset\":[{\"amount\":10,\"address\":\"ELdKTfcrYCvGPh4iBNj1NWreRs3Rej7D4i\"}],\"Fee\":20000,\"Outputs\":[{\"amount\":10010,\"address\":\"XKUh4GLhFJiqAMTF6HyWQrV9pK9HcGUdfJ\"},{\"amount\":9999979990,\"address\":\"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4\"}]}]}", mPrivateKey);
        String transaction = String.format("{\"Transactions\":[{\"UTXOInputs\":[{\"address\":\"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4\",\"txid\":\"583ca6c3780b3ba880b446c7ce5427e538a82fc185e54749e61805a97dc3b222\",\"index\":0, \"privateKey\":\"%s\"}],\"CrossChainAsset\":[{\"amount\":100000000,\"address\":\"ENUFoHcsfvXkAVomXJLDrhM189k7qSy3xD\"}],\"Fee\":20000,\"Outputs\":[{\"amount\":100010000,\"address\":\"XKUh4GLhFJiqAMTF6HyWQrV9pK9HcGUdfJ\"},{\"amount\":9899980000,\"address\":\"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4\"}]}]}", "840d6c631e3d612aa624dae2d7f6d354e58135a7a6cb16ed6dd264b7d104aae7");
        LogUtil.d("origin data:" + transaction);
        String signedData = ElastosWalletSign.generateRawTransaction(transaction);
        if(signedData == null) {
            String errmsg = "Failed to generate raw transaction.\n";
            message += errmsg;

            return message;
        }
        message += signedData + "\n";

        message += "================================================\n";
        return signedData;
    }

    private static String parseChongzhiData(String data) {
        String returnData = "";
        try {
            JSONObject js = new JSONObject(data);
//            String result
        }catch (Exception e) {
            e.printStackTrace();
        }
        return returnData;
    }







    public static boolean importWallet(String mnemonic) {
        LogUtil.i("importWallet mnemonic=" + mnemonic);
        String language = "";
        String words = "";
        String sp_lang = Utilty.getPreference(Constants.SP_KEY_APP_LANGUAGE, "");
        if (TextUtils.isEmpty(sp_lang)) {
            if (Utilty.isChinese(mnemonic)) {
                language = "chinese";
            } else {
                language = "english";
            }
        } else {
            language = sp_lang;
        }

        if (language.equals("chinese")) {
            words = FileUtil.readAssetsTxt(mContext, "ElastosWalletLib/mnemonic_chinese.txt");
        } else {
            words = "";
        }

        mSeed = new ElastosWallet.Data();
        int ret = ElastosWallet.getSeedFromMnemonic(mSeed, mnemonic, language, words, "");
        if (ret <= 0) {
            String errmsg = "Failed to get seed from mnemonic. ret=" + ret + "\n";
            LogUtil.e(errmsg);
            return false;
        }
        mSeedLen = ret;

        String privateKey = ElastosWallet.getSinglePrivateKey(mSeed, mSeedLen);
        if (privateKey == null) {
            String errmsg = "Failed to generate privateKey.\n";
            LogUtil.e(errmsg);
            return false;
        }
        LogUtil.d("privatekey=" + privateKey);

        String publicKey = ElastosWallet.getSinglePublicKey(mSeed, mSeedLen);
        if (publicKey == null) {
            String errmsg = "Failed to generate publicKey.\n";
            LogUtil.e(errmsg);
            return false;
        }

        String address = ElastosWallet.getAddress(publicKey);
        if (address == null) {
            String errmsg = "Failed to get address.\n";
            LogUtil.e(errmsg);
            return false;
        }

        ElastosWallet.Data data = new ElastosWallet.Data();
        data.buf = new byte[]{0, 1, 2, 3, 4, 5};
        ElastosWallet.Data signedData = new ElastosWallet.Data();
        int signedLen = ElastosWallet.sign(privateKey, data, data.buf.length, signedData);
        if (signedLen <= 0) {
            String errmsg = "Failed to sign data.\n";
            LogUtil.e(errmsg);
            return false;
        }

        boolean verified = ElastosWallet.verify(publicKey, data, data.buf.length, signedData, signedLen);
        if (!verified) {
            return false;
        }

        ElastosWallet.Data idChainMasterPublicKey = ElastosWalletDID.getIdChainMasterPublicKey(mSeed, mSeedLen);
        if (idChainMasterPublicKey == null) {
            String errmsg = "Failed to generate id chain master publicKey.\n";
            LogUtil.e(errmsg);
            return false;
        }

        int count = 1;
        String[] privateKeys = new String[count];
        String[] publicKeys = new String[count];
        String[] dids = new String[count];
        for (int idx = 0; idx < count; idx++) {
            privateKeys[idx] = ElastosWalletDID.generateIdChainSubPrivateKey(mSeed, mSeedLen, 0, idx);
            publicKeys[idx] = ElastosWalletDID.generateIdChainSubPublicKey(idChainMasterPublicKey, 0, idx);
            dids[idx] = ElastosWalletDID.getDid(publicKeys[idx]);

            Utilty.setPreference(Constants.SP_KEY_DID, dids[idx]);
        }

        //after import success:
        mPrivateKey = privateKey;
        Utilty.setPreference(Constants.SP_KEY_DID_PRIVATEKEY, privateKey);
        Utilty.setPreference(Constants.SP_KEY_DID_PUBLICKEY, publicKey);
        Utilty.setPreference(Constants.SP_KEY_DID_ADDRESS, address);
        Utilty.setPreference(Constants.SP_KEY_DID_ISBACKUP, "true");
        Utilty.setPreference(Constants.SP_KEY_DID_MNEMONIC, "");
        Utilty.setPreference(Constants.SP_KEY_APP_LANGUAGE, language);
        return true;
    }

    public static String resetAddress() {
        String address = ElastosWallet.getAddress(Utilty.getPreference(Constants.SP_KEY_DID_PUBLICKEY, ""));
        if (TextUtils.isEmpty(address)) return null;
        Utilty.setPreference(Constants.SP_KEY_DID_ADDRESS, address);
        return address;
    }

}