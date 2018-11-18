package com.ela.wallet.sdk.didlibrary.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ela.wallet.sdk.didlibrary.activity.DidLaunchActivity;
import com.ela.wallet.sdk.didlibrary.global.Constants;
import com.ela.wallet.sdk.didlibrary.global.Urls;
import com.ela.wallet.sdk.didlibrary.http.HttpRequest;
import com.ela.wallet.sdk.didlibrary.service.DidService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.elastos.wallet.lib.ElastosWallet;
import org.elastos.wallet.lib.ElastosWalletDID;
import org.elastos.wallet.lib.ElastosWalletHD;

import java.util.ArrayList;

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
        Intent intent = new Intent();
        intent.setClass(context, DidService.class);
        context.startService(intent);
//        if (TextUtils.isEmpty(Utilty.getPreference(mContext, Constants.SP_KEY_DID_ADDRESS, ""))) {
            GenrateMnemonic();
//        }
        return "init success";
    }

    public static void launch(Context context) {
        Intent intent = new Intent();
        if (context instanceof Application) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setClass(context, DidLaunchActivity.class);
        context.startActivity(intent);
    }

    private static String GenrateMnemonic() {
        LogUtil.d("GenrateMnemonic");
        String message = "";
        String language = "english";
        String words = "";

        String mnemonic = ElastosWallet.generateMnemonic(language, words);
        if (mnemonic == null) {
            String errmsg = "Failed to generate mnemonic.";
            LogUtil.e(errmsg);
            message += errmsg;
            return message;
        }
        message += "mnemonic: " + mnemonic + "\n";

        mSeed = new ElastosWallet.Data();
        int ret = ElastosWallet.getSeedFromMnemonic(mSeed, mnemonic, language, words, "0");
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

        String publicKey = ElastosWallet.getSinglePublicKey(mSeed, mSeedLen);
        if (publicKey == null) {
            String errmsg = "Failed to generate publicKey.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        message += "publicKey: " + publicKey + "\n";

        String address = ElastosWallet.getAddress(publicKey);
        if (address == null) {
            String errmsg = "Failed to get address.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        message += "address: " + address + "\n";
        Utilty.setPreference(mContext, Constants.SP_KEY_DID_ADDRESS, address);

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

    private String Did() {
        String message = "";

        ElastosWallet.Data idChainMasterPublicKey = ElastosWalletDID.getIdChainMasterPublicKey(mSeed, mSeedLen);
        if (idChainMasterPublicKey == null) {
            String errmsg = "Failed to generate id chain master publicKey.\n";
            LogUtil.e(errmsg);
            message += errmsg;

            return message;
        }
        message += "idChainMasterPublicKey: " + idChainMasterPublicKey.buf + "\n";

        int count = 10;
        String[] privateKeys = new String[count];
        String[] publicKeys = new String[count];
        String[] dids = new String[count];
        for (int idx = 0; idx < count; idx++) {
            privateKeys[idx] = ElastosWalletDID.generateIdChainSubPrivateKey(mSeed, mSeedLen, 0, idx);
            publicKeys[idx] = ElastosWalletDID.generateIdChainSubPublicKey(idChainMasterPublicKey, 0, idx);
            dids[idx] = ElastosWalletDID.getDid(publicKeys[idx]);

            message += "dids[" + idx + "]: " + dids[idx] + "\n";
        }

        message += "================================================\n";
        return message;
    }

    /**
     * 充值 ELA-DID
     */
    public static void testChongzhi() {
        String fromAddress = "ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4";
        String toAddress = Utilty.getPreference(mContext, Constants.SP_KEY_DID_ADDRESS, "");
        String param = String.format("  {\"inputs\":[\"ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4\"],\"outputs\":[{\"addr\":\"%s\",\"amt\":10}]}", toAddress);

        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_WALLET + Urls.ELA_CCT, param, new HttpRequest.HttpCallbackListener() {
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
                        HttpRequest.sendRequestWithHttpURLConnection(Urls.SERVER_WALLET + Urls.ELA_SRT, signdparam, new HttpRequest.HttpCallbackListener() {
                            @Override
                            public void onFinish(final String response) {
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.d("chongzhi result:" + response);
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
     * 提现 DID-ELA
     */
    public static void testTixian() {
        String toAddress = "ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4";
        String fromAddress = Utilty.getPreference(mContext, Constants.SP_KEY_DID_ADDRESS, "");
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
        if (signedLen <= 0) {
            String errmsg = "Failed to sign data.\n";
            LogUtil.e(errmsg);
        }
        return Utilty.bytesToHexString(signedData.buf);
    }

}