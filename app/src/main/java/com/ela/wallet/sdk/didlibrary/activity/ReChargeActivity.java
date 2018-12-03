package com.ela.wallet.sdk.didlibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.ela.wallet.sdk.didlibrary.R;
import com.ela.wallet.sdk.didlibrary.base.BaseActivity;
import com.ela.wallet.sdk.didlibrary.global.Constants;
import com.ela.wallet.sdk.didlibrary.utils.DidLibrary;
import com.ela.wallet.sdk.didlibrary.utils.LogUtil;
import com.ela.wallet.sdk.didlibrary.utils.Utilty;
import com.ela.wallet.sdk.didlibrary.widget.DidAlertDialog;


public class ReChargeActivity extends BaseActivity {
    private EditText et_scan_address;
    private ImageView iv_scan;

    @Override
    protected int getRootViewId() {
        return R.layout.activity_recharge;
    }

    @Override
    protected void initView() {
        et_scan_address = findViewById(R.id.et_scan_address);
        iv_scan = findViewById(R.id.iv_scan);

        iv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(ReChargeActivity.this, ScanActivity.class);
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_SCAN);
            }
        });
    }

    @Override
    protected void initData() {
        if (!Utilty.isBacked()) {
            showBackupDialog();
        }
    }

    public void onOKClick(View view) {
        //todo:
//        String fromAddress = "ESs1jakyQjxBvEgwqEGxtceastbPAR1UJ4";
//        DidLibrary.Chongzhi(fromAddress, "1");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.i("onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INTENT_REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK && data != null) {
            String result = data.getStringExtra(Constants.INTENT_PARAM_KEY_SCANRESUTL);
            et_scan_address.setText(result);
        }
    }

    private void showBackupDialog() {
        new DidAlertDialog(this)
                .setTitle(getString(R.string.send_backup))
                .setMessage(getString(R.string.send_tips))
                .setMessageGravity(Gravity.LEFT)
                .setRightButton(getString(R.string.btn_backup), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(ReChargeActivity.this, BackupTipsActivity.class);
                        ReChargeActivity.this.startActivity(intent);
                    }
                })
                .show();
    }
}
