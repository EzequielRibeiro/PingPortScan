package org.ping.cool.Local.response;

import java.util.Map;

public interface MainAsyncResponse {

    //Delegate to handle map outputs
    void processFinish(Map<String, String> output);

    //Delegate to handle integer outputs
    void processFinish(int output);
}
