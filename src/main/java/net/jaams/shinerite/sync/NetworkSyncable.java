package net.jaams.shinerite.sync;

import java.util.Map;



public interface NetworkSyncable {

    String getSyncId();

    Map<String, String> getSourcesSnapshot();

    void applyNetworkSync(Map<String, String> sources);
}
