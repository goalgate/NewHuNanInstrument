package cn.cbsd.cjyfunctionlib.Func_FingerPrint.FingerPrintHelper;

public interface IUsbConnState {
    void onUsbConnected();

	void onUsbPermissionDenied();

	void onDeviceNotFound();
}
