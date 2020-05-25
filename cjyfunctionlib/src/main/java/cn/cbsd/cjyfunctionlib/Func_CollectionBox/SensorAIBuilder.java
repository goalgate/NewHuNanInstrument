package cn.cbsd.cjyfunctionlib.Func_CollectionBox;


import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.SensorAI;

public class SensorAIBuilder extends SensorAI {

    private DataCallback Event_;

    public SensorAIBuilder() {
    }

    public SensorAIBuilder(boolean enable, String name, String englishName, String unit,
                           float maxRange, float minRange, float minVal, float maxVal,
                           int precision, float alarmMaxVal, float alarmMinVal) {
        setEnable(enable);
        setEnglish_name(englishName);
        setName(name);
        setUnit(unit);
        setMaxRange(maxRange);
        setMinRange(minRange);
        setMinVal(minVal);
        setMaxVal(maxVal);
        setPrecision(precision);
        setAlarmMaxVal(alarmMaxVal);
        setAlarmMinVal(alarmMinVal);
    }

    public SensorAIBuilder setBuilderEnable(boolean enable) {
        setEnable(enable);
        return this;
    }

    public SensorAIBuilder setBuilderEnglishName(String englishName) {
        setEnglish_name(englishName);
        return this;
    }


    public SensorAIBuilder setBuilderName(String name) {
        setName(name);
        return this;
    }

    public SensorAIBuilder setBuilderUnit(String unit) {
        setUnit(unit);
        return this;
    }

    public SensorAIBuilder setBuilderMaxRange(float maxRange) {
        setMaxRange(maxRange);
        return this;
    }


    public SensorAIBuilder setBuilderMinRange(float minRange) {
        setMinRange(minRange);
        return this;
    }

    public SensorAIBuilder setBuilderMinVal(float minVal) {
        setMinVal(minVal);
        return this;
    }

    public SensorAIBuilder setBuilderMaxVal(float maxVal) {
        setMaxVal(maxVal);
        return this;
    }


    public SensorAIBuilder setSensorAIBuilderPrecision(int precision) {
        setPrecision(precision);
        return this;
    }

    public SensorAIBuilder setSensorAIBuilderAlarmMaxVal(float alarmMaxVal) {
        setAlarmMaxVal(alarmMaxVal);
        return this;
    }


    public SensorAIBuilder setSensorAIBuilderAlarmMinVal(float alarmMinVal) {
        setAlarmMinVal(alarmMinVal);
        return this;
    }

    public SensorAIBuilder setDataCallback(DataCallback dataCallback) {
        this.Event_ = dataCallback;
        return this;
    }

    public void DataTrigger(SensorAI sensorAI) {
        if(Event_!=null){
            this.Event_.data(sensorAI);
        }
    }


    public interface DataCallback {
        void data(SensorAI ai);
    }


}
