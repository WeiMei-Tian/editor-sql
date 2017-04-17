package com.gmobile.sqliteeditor.adapter.listener;

import android.app.Activity;
import android.content.Intent;

/**
 * @author Administrator
 * @version 2016/7/24 0024
 *          ${tags}
 */
public class SqlActivityResultHandler {

    private Activity mActivity;

    public SqlActivityResultHandler(Activity activity) {
        this.mActivity = activity;
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {

//        if (requestCode == GooglePurchaseHandle.REQUEST_CODE && data != null) {
//
//            int responseCode = data.getIntExtra(GooglePurchaseHandle.RESPONSE_CODE, 0);
//            String purchaseData = data.getStringExtra(GooglePurchaseHandle.RESPONSE_INAPP_PURCHASE_DATA);
//
//            if (responseCode == GooglePurchaseHandle.BILLING_RESPONSE_RESULT_OK
//                    && resultCode == Activity.RESULT_OK) {
//                if (purchaseData == null) {
//                    return;
//                }
//
//                PurchaseHandle.processGooglePurchase(mActivity, purchaseData);
//            }
//        }
    }

}
