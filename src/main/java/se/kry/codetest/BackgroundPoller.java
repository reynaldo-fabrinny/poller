package se.kry.codetest;

import io.vertx.core.Future;

import java.util.List;
import java.util.Map;

public class BackgroundPoller {

    public Future<List<String>> pollServices(Map<String, String> services) {
        //TODO here make the calls and check

        return Future.failedFuture("TODO");
    }
}
