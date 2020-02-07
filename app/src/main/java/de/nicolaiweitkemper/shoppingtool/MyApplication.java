package de.nicolaiweitkemper.shoppingtool;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.sender.HttpSender;

@AcraCore(buildConfigClass = BuildConfig.class)
//@AcraMailSender(mailTo = "nico.weio+acra@gmail.com")
@AcraHttpSender(uri = "https://collector.tracepot.com/e2b5dd1b",
//        basicAuthLogin = "yourlogin", // optional
//        basicAuthPassword = "y0uRpa$$w0rd", // optional
        httpMethod = HttpSender.Method.POST)
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}