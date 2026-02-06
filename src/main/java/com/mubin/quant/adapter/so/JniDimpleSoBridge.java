package com.mubin.quant.adapter.so;

import com.mubin.quant.core.MarketDataService;
import com.mubin.quant.ingest.DimpleQuoteMessage;

public class JniDimpleSoBridge implements SoQuoteBridge {

    private final String libName;
    private final String configPath;
    private final DimpleEventParser parser;
    private final MarketDataService marketDataService;

    private long nativeHandle;
    private boolean started;

    public JniDimpleSoBridge(String libName,
                             String configPath,
                             DimpleEventParser parser,
                             MarketDataService marketDataService) {
        this.libName = libName;
        this.configPath = configPath;
        this.parser = parser;
        this.marketDataService = marketDataService;
    }

    @Override
    public synchronized void start() {
        if (started) {
            return;
        }

        System.loadLibrary(libName);
        nativeHandle = nativeCreate(configPath);
        nativeRegisterCallback(nativeHandle, this::onNativeEvent);
        nativeStart(nativeHandle);
        started = true;
    }

    @Override
    public synchronized void stop() {
        if (!started) {
            return;
        }

        nativeStop(nativeHandle);
        nativeDestroy(nativeHandle);
        nativeHandle = 0L;
        started = false;
    }

    private void onNativeEvent(String rawEvent) {
        try {
            DimpleQuoteMessage message = parser.parse(rawEvent);
            marketDataService.onDimpleQuote(message);
        } catch (Exception ignored) {
            // Keep callback side-effect free on parse failures; upstream bridge may retry or log.
        }
    }

    // JNI placeholders for the native dimple bridge implementation.
    private native long nativeCreate(String configPath);

    private native void nativeRegisterCallback(long handle, JniCallback callback);

    private native void nativeStart(long handle);

    private native void nativeStop(long handle);

    private native void nativeDestroy(long handle);

    @FunctionalInterface
    public interface JniCallback {
        void onEvent(String rawEvent);
    }
}
