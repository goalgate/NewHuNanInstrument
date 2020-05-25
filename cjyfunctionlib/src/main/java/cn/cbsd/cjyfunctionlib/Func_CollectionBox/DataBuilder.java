package cn.cbsd.cjyfunctionlib.Func_CollectionBox;

import android.hardware.Sensor;

import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.NetDAM0888Data;
import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.SensorAI;
import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.SensorDIO;

public class DataBuilder extends NetDAM0888Data {

    SensorAIBuilder[] sensorAIs = new SensorAIBuilder[8];

    public DataBuilder() {
        super();
        for (int i = 0; i < 8; i++) {
            sensorAIs[i] = new SensorAIBuilder();

        }
    }

    @Override
    public void setAI(int[] val) {
        super.setAI(val);
        if (val != null && val.length >= 8) {
            for (int i = 0; i < 8; i++) {
                sensorAIs[i].setCollectionVal(val[i]);
            }
        }
    }

    @Override
    public SensorAIBuilder getAI(int i) {
        if (i >= 0 && i < 8) {
            return sensorAIs[i];
        } else {
            return null;
        }
    }

}
