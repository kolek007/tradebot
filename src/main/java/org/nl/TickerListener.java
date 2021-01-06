package org.nl;

import lombok.extern.slf4j.Slf4j;
import org.nl.util.Util;

import javax.annotation.Nonnull;

@Slf4j
public class TickerListener {

    public TickerListener(@Nonnull String ssoToken) {
        log.info("SSO TOKEN: {}", ssoToken);
    }

    public void init() {
        log.info("INIT STARTED");
        //type your code here
        double[] testData = new double[]{0, 1, 2, 2, 3, 1, 5, 0};
        final int[] maxExtremums = Util.findMaxExtremums(testData);
        if(maxExtremums[0] == 6 && maxExtremums[1] == 4) {
            log.info("SUCCESS");
        } else {
            log.info("FAIL");
        }
        log.info("INIT ENDED");
    }

    private void destroy() {
        log.info("DESTROY STARTED");
        //type your code here
        log.info("DESTROY ENDED");
    }
}
