package com.xl.job.admin.router;

import java.util.List;
import java.util.Random;

/**
 * 随机路由策略
 */
public class RandomRouter implements Router {

    private final Random random = new Random();

    @Override
    public String route(List<String> addressList, Long jobId) {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }

        int index = random.nextInt(addressList.size());
        return addressList.get(index);
    }
}
