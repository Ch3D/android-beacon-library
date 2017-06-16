package org.altbeacon.beacon.service.scanner;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;

import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.startup.StartupBroadcastReceiver;
import org.altbeacon.bluetooth.BluetoothCrashResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by dyoung on 5/28/17.
 */

@TargetApi(25)
public class CycledLeScannerForAndroidO extends CycledLeScannerForJellyBeanMr2 {
    private static final String TAG = CycledLeScannerForAndroidO.class.getSimpleName();

    public CycledLeScannerForAndroidO(Context context, long scanPeriod, long betweenScanPeriod, boolean backgroundFlag, CycledLeScanCallback cycledLeScanCallback, BluetoothCrashResolver crashResolver) {
        super(context, scanPeriod, betweenScanPeriod, backgroundFlag, cycledLeScanCallback, crashResolver);
        // We stop scanning here in case we were doing a passive background scan
        getBluetoothAdapter().getBluetoothLeScanner().stopScan(getScanCallbackIntent());
    }

    /**
     * @param beaconParsers
     */
    public void startAndroidOBackgroundScan(Set<BeaconParser> beaconParsers) {
        ScanSettings settings = (new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)).build();
        List<ScanFilter> filters = new ScanFilterUtils().createScanFiltersForBeaconParsers(
                new ArrayList(beaconParsers));
        try {
            int result = getBluetoothAdapter().getBluetoothLeScanner().startScan(filters, settings, getScanCallbackIntent());
            if (result != 0) {
                LogManager.e(TAG, "Failed to start background scan on Android O.  Code: "+result);
            }
            else {
                LogManager.d(TAG, "Started passive beacon scan");
            }
         }
        catch (SecurityException e) {
            LogManager.e(TAG, "SecurityException making Android O background scanner");
         }
    }

    // Low power scan results in the background will be delivered via Intent
    private PendingIntent getScanCallbackIntent() {
        Intent intent = new Intent(mContext, StartupBroadcastReceiver.class);
        intent.putExtra("o-scan", true);
        return PendingIntent.getBroadcast(mContext,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}