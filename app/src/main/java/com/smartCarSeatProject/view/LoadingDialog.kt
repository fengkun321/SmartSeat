package com.smartCarSeatProject.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView

import com.smartCarSeatProject.R


class LoadingDialog : Dialog {

    private var mTextView: TextView? = null
    var strMsg = "waitting...";

    constructor(context: Context) : super(context) {}

    constructor(context: Context?, theme: Int) : super(context!!, theme) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.loading_dialog)
        this.setCanceledOnTouchOutside(false)
        mTextView = findViewById<View>(R.id.loading_text) as TextView?
        this.mTextView?.text = strMsg
    }

    fun updateStatusText(text: String) {
        strMsg = text
        this.mTextView?.text = text
    }

    fun showAndMsg(text: String) {
        strMsg = text
        this.show()
    }

}
