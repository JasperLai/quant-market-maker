package com.mubin.quant.risk;

import com.mubin.quant.domain.AssetClass;

import java.util.concurrent.atomic.DoubleAdder;

public class DailyExposureTracker {

    private final DoubleAdder fxNotional = new DoubleAdder();
    private final DoubleAdder goldQtyGrams = new DoubleAdder();

    public void addFxNotional(double notional) {
        fxNotional.add(notional);
    }

    public void addGoldQtyGrams(double qtyGrams) {
        goldQtyGrams.add(qtyGrams);
    }

    public double current(AssetClass assetClass) {
        return assetClass == AssetClass.FX ? fxNotional.sum() : goldQtyGrams.sum();
    }
}
