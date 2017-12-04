package com.nedeos.ride;

import android.app.Application;
import android.content.Context;

import com.nedeos.ride.messages.RideDBHelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Karsten on 21.09.2017.
 */

public class RideApplication extends Application {

    public static String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqsuQzs8VLO31xWFDL3H34QRFlXDTGaWLgrZacRZFciPo+k2kBoA3ZGyHYvNKE88e1KrYbsV1TnNJXyH0BxBD9e2GSszh88aG7L8Zj6PHFteHie+NfMFQBz8GlNWh42zqQpb0Sa1cfAlJvU5YTmF3RprunzsHcjidDU6IWp29Njf/Hy/QT7PgRvs+gYJlY6+aGaP0Cwou2bjUG65inf8vNvWT+ptoQhejUAOi6G3bOBF/cDUkPCdBWi6VGZJpT9YdM+jIPMcfKGMov7e/0jZ7UF1bhmOI1EBRPs0+bq8BvydlUSfWeBvShWlv6kDO+icjBlKCoumePRNhpSlaVMAPQIDAQAB";

    public static final String ARG_MESSAGE_NOTIFICATION = "com.nedeos.ride.messageNotification";

    public static final String ARG_IMAGE_UPLOAD_FAILED = "com.nedeos.ride.imageUploadFailed";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

    public static long MAX_DIRECTORY_SIZE = 1024 * 1024 * 10;

    public static final float ALPHA_FULL = 1.0f;

    public static final int MAX_IMAGE_SIZE = 512;

    public static final String ARG_ICON = "icon";
    public static final String ARG_SHOT = "shot";
    public static final String ARG_COST = "cost";
    public static final String ARG_USED = "used";
    public static final String ARG_HEAD = "head";
    public static final String ARG_TEXT = "text";
    public static final String ARG_LINK = "link";
    public static final String ARG_TIME = "time";
    public static final String ARG_NOID = "noid";
    public static final String ARG_STAT = "stat";
    public static final String ARG_PROD = "prod";
    public static final String ARG_USID = "usid";

    public static final String ARG_IMAGE_1 = "image_1";
    public static final String ARG_IMAGE_2 = "image_2";
    public static final String ARG_IMAGE_3 = "image_3";

    public static final String ARG_SINGLE = "single";

    public static final String ARG_IMAGE_NUMBER = "number";

    public static final String ARG_QUERY = "query";

    public static final String ARG_MESSAGE = "message";

    public static final String ARG_SUBSCRIPTION = "subscription";

    public static final String DB_MESSAGES_TABLE = "messages";

    public static final String DB_FOUNDSUBS_TABLE = "foundsubs";

    public static final String DB_SUBSCRIPTIONS_TABLE = "subscriptions";

    public static final String ACCOUNTFRAGMENT_ACCOUNT_LIST = "accountfragment.account.list";

    public static final String VIEWFRAGMENT_MESSAGE_LIST = "viewfragment.message.list";

    public static final String DETAILFRAGMENT_DETAIL_USER = "detailfragment.detail.user";

    public static final String USERFRAGMENT_USER_LIST = "userfragment.user.list";

    public static final int DB_MAX_BACKLOG_ENTRIES = 200;

    public static final int ACTIVITY_REQUEST_CODE = 2000;

    public static final String ARG_IDENTIFY = "identify";
    public static final String ARG_PASSWORD = "password";

    public static final String ARG_TOKENUPD = "tokenupd";
    public static final String ARG_CATEGORY = "category";

    public static final String ARG_LATITUDE = "latitude";
    public static final String ARG_LONGITUDE = "longitude";

    public static final String ARG_PURCHASE = "purchase";

    public static final String ARG_LAYOUT_POSITION = "layoutposition";

    public static final String ARG_SUBSCRIPTION_HASH = "subshash";

    public static final int RIDE_PRO_RETURN_CODE = 1000;

    public static final String ARG_ORDER_ID = "orderId";

    public static final String ARG_PACKAGE_NAME = "packageName";

    public static final String ARG_PRODUCT_ID = "productId";

    public static final String ARG_PURCHASE_TIME = "purchaseTime";

    public static final String ARG_PURCHASE_STATE = "purchaseState";

    public static final String ARG_DEVELOPER_PAYLOAD = "developerPayload";

    public static final String ARG_TOKEN = "token";

    public static final String ARG_PURCHASE_TOKEN = "purchaseToken";

    public final static int ACCOUNT_SET_PHOTO = 100;
    public final static int ACCOUNT_GET_PHOTO = 200;

    public final static int ACCOUNT_SET_PHOTO_IMAGE1 = 110;
    public final static int ACCOUNT_GET_PHOTO_IMAGE1 = 120;

    public final static int ACCOUNT_SET_PHOTO_IMAGE2 = 210;
    public final static int ACCOUNT_GET_PHOTO_IMAGE2 = 220;

    public final static int ACCOUNT_SET_PHOTO_IMAGE3 = 310;
    public final static int ACCOUNT_GET_PHOTO_IMAGE3 = 320;

    private static OkHttpClient okHttpClient = null;
    private static RideDBHelper rideDBHelper = null;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeOkHttpClient(getApplicationContext());
        initializeRideDBHelper(getApplicationContext());
    }

    private void initializeRideDBHelper(Context context) {

        if (rideDBHelper == null) {
            rideDBHelper = new RideDBHelper(context);
        }
    }

    public static RideDBHelper getRideDBHelper() {
        return rideDBHelper;
    }

    private void initializeOkHttpClient(Context context) {
        Cache cache = new Cache(context.getCacheDir(), MAX_DIRECTORY_SIZE);

        if (okHttpClient == null) {
            Interceptor netInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String credentials = Credentials.basic("onBGs9Saby4", "m4Kj7AVqP03H");

                    Request origRequest = chain.request();

                    Request finaRequest = origRequest.newBuilder()
                            .header("Authorization", credentials)
                            .build();

                    return chain.proceed(finaRequest);
                }
            };

            Interceptor cchInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {

                    Response origResponse = chain.proceed(chain.request());

                    return origResponse.newBuilder()
                            .header("Cache-Control", "max-age=" + (60 * 60 * 24))
                            .build();
                }
            };

            List<Protocol> protocols = new ArrayList<Protocol>();

            protocols.add(Protocol.HTTP_2);
            protocols.add(Protocol.HTTP_1_1);

            try {
                okHttpClient = new OkHttpClient.Builder()
                        .addNetworkInterceptor(netInterceptor)
                        .addNetworkInterceptor(cchInterceptor)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .sslSocketFactory(new TLSSocketFactory(), new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        })
                        .protocols(protocols)
                        .cache(cache)
                        .build();

            } catch (KeyManagementException e) {
                // e.printStackTrace();

            } catch (NoSuchAlgorithmException e) {
                // e.printStackTrace();
            }
        }
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    private static class TLSSocketFactory extends SSLSocketFactory {

        private SSLSocketFactory delegate;

        public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {

            SSLContext context = SSLContext.getInstance("TLS");

            context.init(null, null, null);

            delegate = context.getSocketFactory();
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket enableTLSOnSocket(Socket socket) {

            if ((socket != null) && (socket instanceof SSLSocket)) {
                ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2"});
            }

            return socket;
        }
    }

}
