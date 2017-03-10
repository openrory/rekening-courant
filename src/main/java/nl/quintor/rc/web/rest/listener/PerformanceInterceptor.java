package nl.quintor.rc.web.rest.listener;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;

/**
 * Created by marcel on 5-2-2017.
 */
@Provider
public class PerformanceInterceptor implements RequestEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceInterceptor.class);
    private final int requestNumber;
    private final long startTime;

    public PerformanceInterceptor(int requestNumber) {
        this.requestNumber = requestNumber;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onEvent(RequestEvent event) {
        switch (event.getType()) {
            case RESOURCE_METHOD_START:
                LOG.info(String.format("Resource method %s started for request %s", event.getUriInfo().getMatchedResourceMethod().getHttpMethod(), requestNumber));
                break;
            case FINISHED:
                LOG.info(String.format("Request %s finished. Processing time %s ms.", requestNumber, (System.currentTimeMillis() - startTime)));
                break;
        }
    }
}