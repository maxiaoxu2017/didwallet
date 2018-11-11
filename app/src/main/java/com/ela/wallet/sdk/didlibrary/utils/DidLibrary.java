package com.ela.wallet.sdk.didlibrary.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ela.wallet.sdk.didlibrary.activity.DidLaunchActivity;
import com.ela.wallet.sdk.didlibrary.global.Constants;
import com.ela.wallet.sdk.didlibrary.service.DidService;

import org.elastos.wallet.lib.ElastosWallet;
import org.elastos.wallet.lib.ElastosWalletDID;
import org.elastos.wallet.lib.ElastosWalletHD;

public class DidLibrary {

    private static Context mContext;
    private static ElastosWallet.Data mSeed;
    private static int mSeedLen;

    public static void openLog(boolean open) {
        if (open) {
            LogUtil.setLogLevel(LogUtil.VERBOSE);
        }
    }

    public static String init(Context context) {
        mContext = context;
        Intent intent = new Intent();
        intent.setClass(context, DidService.class);
        context.startService(intent);
        GenrateMnemonic();
        return "";
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
        return address;
    }



    private String HDWalletAddress() {
        String message = "";

        ElastosWallet.Data masterPublicKey = ElastosWalletHD.getMasterPublicKey(mSeed, mSeedLen, ElastosWalletHD.COIN_TYPE_ELA);
        if(masterPublicKey == null) {
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
        if(idChainMasterPublicKey == null) {
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
}
