/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.client.proxy;

import com.hazelcast.client.spi.ClientProxy;
import com.hazelcast.concurrent.countdownlatch.client.*;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.util.ExceptionUtil;

import java.util.concurrent.TimeUnit;

/**
 * @ali 5/28/13
 */
public class ClientCountDownLatchProxy extends ClientProxy implements ICountDownLatch{

    Data key;

    public ClientCountDownLatchProxy(String serviceName, String objectId) {
        super(serviceName, objectId);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        AwaitRequest request = new AwaitRequest(getName(), getTimeInMillis(timeout, unit));
        Boolean result = invoke(request);
        return result;
    }

    public void countDown() {
        CountDownRequest request = new CountDownRequest(getName());
        invoke(request);
    }

    public int getCount() {
        GetCountRequest request = new GetCountRequest(getName());
        Integer result = invoke(request);
        return result;
    }

    public boolean trySetCount(int count) {
        SetCountRequest request = new SetCountRequest(getName(), count);
        Boolean result = invoke(request);
        return result;
    }

    protected void onDestroy() {
        CountDownLatchDestroyRequest request = new CountDownLatchDestroyRequest(getName());
        invoke(request);
    }

    public String getName() {
        return (String)getId();
    }

    private Data toData(Object o){
        return getContext().getSerializationService().toData(o);
    }

    private Data getKey(){
        if (key == null){
            key = toData(getId());
        }
        return key;
    }

    private long getTimeInMillis(final long time, final TimeUnit timeunit) {
        return timeunit != null ? timeunit.toMillis(time) : time;
    }

    private <T> T invoke(Object req) {
        try {
            return getContext().getInvocationService().invokeOnKeyOwner(req, getKey());
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
}
