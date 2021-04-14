package cn.cbsd.cjyfunctionlib.func_server;

/**
 * @author Created by WZW on 2021-04-13 16:10.
 * @description
 */
public interface IServerEvent {

    void onServerStart(String ip);

    void onServerError(String message);

    void onServerStop();
}
