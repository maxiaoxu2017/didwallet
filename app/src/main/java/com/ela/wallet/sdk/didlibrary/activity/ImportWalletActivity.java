package com.ela.wallet.sdk.didlibrary.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import com.ela.wallet.sdk.didlibrary.R;
import com.ela.wallet.sdk.didlibrary.base.BaseActivity;
import com.ela.wallet.sdk.didlibrary.widget.DidAlertDialog;

public class ImportWalletActivity extends BaseActivity {

    @Override
    protected int getRootViewId() {
        return R.layout.activity_import_wallet;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View view) {

    }

    public void onImportClick(View view) {
        DidAlertDialog confirmDialog = new DidAlertDialog(this);
        confirmDialog.setTitle(getString(R.string.dialog_becareful))
                .setMessage(getString(R.string.dialog_switch_wallet))
                .setMessageGravity(Gravity.CENTER)
                .setLeftButton(getString(R.string.btn_cancel), null)
                .setRightButton(getString(R.string.btn_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showErrorDialog();
                    }
                })
                .show();
    }

    @Override
    public String getTitleText() {
        return getString(R.string.import_wallet);
    }

    private void showErrorDialog() {
        new DidAlertDialog(this)
                .setTitle(getString(R.string.dialog_unable_import))
                .setMessage(getString(R.string.dialog_invalid_phrase))
                .setMessageGravity(Gravity.CENTER)
                .setRightButton(getString(R.string.btn_ok), null)
                .show();
    }
}
