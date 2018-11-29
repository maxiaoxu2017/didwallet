package com.ela.wallet.sdk.didlibrary.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import com.ela.wallet.sdk.didlibrary.R;
import com.ela.wallet.sdk.didlibrary.base.BaseActivity;
import com.ela.wallet.sdk.didlibrary.widget.DidAlertDialog;

public class WordInputActivity extends BaseActivity {


    @Override
    protected int getRootViewId() {
        return R.layout.activity_word_input;
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

    public void onSureClick(View view) {
        showSuccessDialog();
    }

    public void onCleanClick(View view) {

    }

    private void showSuccessDialog() {
        new DidAlertDialog(this)
                .setTitle(getString(R.string.word_backdup))
                .setMessage(getString(R.string.word_tips))
                .setMessageGravity(Gravity.LEFT)
                .setRightButton(getString(R.string.btn_next), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPwdInputDialog();
                    }
                })
                .show();
    }

    private void showPwdInputDialog() {
        new DidAlertDialog(this)
                .setTitle(getString(R.string.word_newpwd))
                .setMessage(getString(R.string.word_pwdtips))
                .setMessageGravity(Gravity.LEFT)
                .setEditText(true)
                .setLeftButton(getString(R.string.btn_last), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showSuccessDialog();
                    }
                })
                .setRightButton(getString(R.string.btn_next), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //todo:clear top
                    }
                })
                .show();
    }
}
