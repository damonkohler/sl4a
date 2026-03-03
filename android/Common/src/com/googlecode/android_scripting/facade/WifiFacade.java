package com.googlecode.android_scripting.facade;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Base64;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * WifiManager functions.
 */
// TODO: make methods handle various wifi states properly
// e.g. wifi connection result will be null when flight mode is on
public class WifiFacade extends RpcReceiver {

  private final Service mService;
  private final WifiManager mWifi;
    private WifiLock mLock = null;

  public WifiFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mWifi = (WifiManager) mService.getSystemService(Context.WIFI_SERVICE);
    mLock = null;
  }

  private void makeLock(int wifiMode) {
    if (mLock == null) {
      mLock = mWifi.createWifiLock(wifiMode, "sl4a");
      mLock.acquire();
    }
  }

    private WifiConfiguration genWifiConfig(JSONObject j) throws JSONException {
        if (j == null) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        if (j.has("SSID")) {
            config.SSID = "\"" + j.getString("SSID") + "\"";
        } else if (j.has("ssid")) {
            config.SSID = "\"" + j.getString("ssid") + "\"";
        }
        if (j.has("password")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.preSharedKey = "\"" + j.getString("password") + "\"";
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        if (j.has("BSSID")) {
            config.BSSID = j.getString("BSSID");
        }
        if (j.has("hiddenSSID")) {
            config.hiddenSSID = j.getBoolean("hiddenSSID");
        }
        if (j.has("priority")) {
            config.priority = j.getInt("priority");
        }
        if (j.has("apBand")) {
            Log.e("apBand was ignored by no-system SL4A");
            // config.apBand = j.getInt("apBand");
        }
        if (j.has("preSharedKey")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.preSharedKey = j.getString("preSharedKey");
        }
        if (j.has("wepKeys")) {
            // Looks like we only support static WEP.
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            JSONArray keys = j.getJSONArray("wepKeys");
            String[] wepKeys = new String[keys.length()];
            for (int i = 0; i < keys.length(); i++) {
                wepKeys[i] = keys.getString(i);
            }
            config.wepKeys = wepKeys;
        }
        if (j.has("wepTxKeyIndex")) {
            config.wepTxKeyIndex = j.getInt("wepTxKeyIndex");
        }
        return config;
    }

    @TargetApi(18)
    @RpcMinSdk(18)
    private WifiConfiguration genWifiEnterpriseConfig(JSONObject j) throws JSONException,
            GeneralSecurityException {
        if (j == null) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        if (j.has("SSID")) {
            config.SSID = j.getString("SSID");
        }
        if (j.has("FQDN")) {
            config.FQDN = j.getString("FQDN");
        }
        /* API23
        if (j.has("providerFriendlyName")) {
            config.providerFriendlyName = j.getString("providerFriendlyName");
        }
        if (j.has("roamingConsortiumIds")) {
            JSONArray ids = j.getJSONArray("roamingConsortiumIds");
            long[] rIds = new long[ids.length()];
            for (int i = 0; i < ids.length(); i++) {
                rIds[i] = ids.getLong(i);
            }
            config.roamingConsortiumIds = rIds;
        }
         */
        // API 18
        final String EAP_KEY = "EAP";
        final String PHASE2_KEY = "Phase2";
        final String CA_CERT_KEY = "CA_Cert";
        final String CLIENT_CERT_KEY = "Client_Cert";
        final String PRIVATE_KEY_ID_KEY = "Private_Key_ID";
        final String IDENTITY_KEY = "Identity_Key";
        final String PASSWORD_KEY = "Password_Key";
        final String ALTSUBJECT_MATCH_KEY = "AltSubject";
        final String DOM_SUFFIX_MATCH_KEY = "SuffixMatch";
        final String REALM_KEY = "Realm";

        WifiEnterpriseConfig eConfig = new WifiEnterpriseConfig();
        if (j.has(EAP_KEY)) {
            int eap = j.getInt(EAP_KEY);
            eConfig.setEapMethod(eap);
        }
        if (j.has(PHASE2_KEY)) {
            int p2Method = j.getInt(PHASE2_KEY);
            eConfig.setPhase2Method(p2Method);
        }
        if (j.has(CA_CERT_KEY)) {
            String certStr = j.getString(CA_CERT_KEY);
            Log.v("CA Cert String is " + certStr);
            eConfig.setCaCertificate(strToX509Cert(certStr));
        }
        if (j.has(CLIENT_CERT_KEY)
                && j.has(PRIVATE_KEY_ID_KEY)) {
            String certStr = j.getString(CLIENT_CERT_KEY);
            String keyStr = j.getString(PRIVATE_KEY_ID_KEY);
            Log.v("Client Cert String is " + certStr);
            Log.v("Client Key String is " + keyStr);
            X509Certificate cert = strToX509Cert(certStr);
            PrivateKey privKey = strToPrivateKey(keyStr);
            Log.v("Cert is " + cert);
            Log.v("Private Key is " + privKey);
            eConfig.setClientKeyEntry(privKey, cert);
        }
        if (j.has(IDENTITY_KEY)) {
            String identity = j.getString(IDENTITY_KEY);
            Log.v("Setting identity to " + identity);
            eConfig.setIdentity(identity);
        }
        if (j.has(PASSWORD_KEY)) {
            String pwd = j.getString(PASSWORD_KEY);
            Log.v("Setting password to " + pwd);
            eConfig.setPassword(pwd);
        }
        if (j.has(ALTSUBJECT_MATCH_KEY)) {
            String altSub = j.getString(ALTSUBJECT_MATCH_KEY);
            Log.v("Setting Alt Subject to " + altSub);
            // API23
            // eConfig.setAltSubjectMatch(altSub);
        }
        if (j.has(DOM_SUFFIX_MATCH_KEY)) {
            String domSuffix = j.getString(DOM_SUFFIX_MATCH_KEY);
            Log.v("Setting Domain Suffix Match to " + domSuffix);
            // API23
            // eConfig.setDomainSuffixMatch(domSuffix);
        }
        if (j.has(REALM_KEY)) {
            String realm = j.getString(REALM_KEY);
            Log.v("Setting Domain Suffix Match to " + realm);
            // API23
            // eConfig.setRealm(realm);
        }
        config.enterpriseConfig = eConfig;
        return config;
    }

    @TargetApi(8)
    private byte[] base64StrToBytes(String input) {
        return Base64.decode(input, Base64.DEFAULT);
    }

    private X509Certificate strToX509Cert(String certStr) throws CertificateException {
        byte[] certBytes = base64StrToBytes(certStr);
        InputStream certStream = new ByteArrayInputStream(certBytes);
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return (X509Certificate) cf.generateCertificate(certStream);
    }

    private PrivateKey strToPrivateKey(String key) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        byte[] keyBytes = base64StrToBytes(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        return priv;
    }

    @Rpc(description = "Add a network.")
    public Integer wifiAddNetwork(@RpcParameter(name = "wifiConfig") JSONObject wifiConfig)
            throws JSONException {
        return mWifi.addNetwork(genWifiConfig(wifiConfig));
    }

    /**
     * Connects to a WPA protected wifi network
     *
     * @param wifiSSID SSID of the wifi network
     * @param wifiPassword password for the wifi network
     * @return true on success
     * @throws ConnectException
     * @throws JSONException
     */
    @Rpc(description = "Connects a wifi network by ssid", returns = "True if the operation succeeded.")
    public Boolean wifiConnect(@RpcParameter(name = "config") JSONObject config)
            throws ConnectException, JSONException {
        WifiConfiguration wifiConfig = genWifiConfig(config);
        int nId = mWifi.addNetwork(wifiConfig);
        if (nId < 0) {
            Log.e("Got negative network Id.");
            return false;
        }
        mWifi.disconnect();
        mWifi.enableNetwork(nId, true);
        return mWifi.reconnect();
    }

    @Rpc(description = "Enable a configured network. Initiate a connection if disableOthers is true", returns = "True if the operation succeeded.")
    public Boolean wifiEnableNetwork(@RpcParameter(name = "netId") Integer netId,
            @RpcParameter(name = "disableOthers") Boolean disableOthers) {
        return mWifi.enableNetwork(netId, disableOthers);
    }

    @Rpc(description = "Connect to a wifi network that uses Enterprise authentication methods.")
    public void wifiEnterpriseConnect(@RpcParameter(name = "config") JSONObject config)
            throws JSONException, GeneralSecurityException {
        // Create Certificate
        // WifiActionListener listener = new WifiActionListener(mEventFacade, "EnterpriseConnect");
        WifiConfiguration wifiConfig = genWifiEnterpriseConfig(config);
        if (wifiConfig == null) {return;}
        // API23
        // if (wifiConfig.isPasspoint()) {
            // Log.d("Got a passpoint config, add it and save config.");
            mWifi.addNetwork(wifiConfig);
            mWifi.saveConfiguration();
        /* } else {
            Log.d("Got a non-passpoint enterprise config, connect directly.");
            mWifi.connect(wifiConfig, listener);
        }
         */
    }

  @Rpc(description = "Returns the list of access points found during the most recent Wifi scan.")
  public List<ScanResult> wifiGetScanResults() {
    return mWifi.getScanResults();
  }

  @Rpc(description = "Acquires a full Wifi lock.")
  public void wifiLockAcquireFull() {
    makeLock(WifiManager.WIFI_MODE_FULL);
  }

  @Rpc(description = "Acquires a scan only Wifi lock.")
  public void wifiLockAcquireScanOnly() {
    makeLock(WifiManager.WIFI_MODE_SCAN_ONLY);
  }

  @Rpc(description = "Releases a previously acquired Wifi lock.")
  public void wifiLockRelease() {
    if (mLock != null) {
      mLock.release();
      mLock = null;
    }
  }

  @Rpc(description = "Starts a scan for Wifi access points.", returns = "True if the scan was initiated successfully.")
  public Boolean wifiStartScan() {
    return mWifi.startScan();
  }

  @Rpc(description = "Checks Wifi state.", returns = "True if Wifi is enabled.")
  public Boolean checkWifiState() {
    return mWifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
  }

  @Rpc(description = "Toggle Wifi on and off.", returns = "True if Wifi is enabled.")
  public Boolean toggleWifiState(@RpcParameter(name = "enabled") @RpcOptional Boolean enabled) {
    if (enabled == null) {
      enabled = !checkWifiState();
    }
    mWifi.setWifiEnabled(enabled);
    return enabled;
  }

  @Rpc(description = "Disconnects from the currently active access point.", returns = "True if the operation succeeded.")
  public Boolean wifiDisconnect() {
    return mWifi.disconnect();
  }

  @Rpc(description = "Returns information about the currently active access point.")
  public WifiInfo wifiGetConnectionInfo() {
    return mWifi.getConnectionInfo();
  }

  @Rpc(description = "Reassociates with the currently active access point.", returns = "True if the operation succeeded.")
  public Boolean wifiReassociate() {
    return mWifi.reassociate();
  }

  @Rpc(description = "Reconnects to the currently active access point.", returns = "True if the operation succeeded.")
  public Boolean wifiReconnect() {
    return mWifi.reconnect();
  }

  @Override
  public void shutdown() {
    wifiLockRelease();
  }
}
