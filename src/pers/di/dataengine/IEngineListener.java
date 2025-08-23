package pers.di.dataengine;

public class IEngineListener {
    public void onInitialize(DAContext context){};
	public void onUnInitialize(DAContext context){};
	public void onTradingDayFinish(DAContext context){};
}
