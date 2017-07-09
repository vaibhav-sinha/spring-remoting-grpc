package com.github.vaibhavsinha.remoting.grpc;

import com.github.vaibhavsinha.grpc.RemoteInvocationRequest;
import com.github.vaibhavsinha.grpc.RemoteInvocationResponse;
import com.github.vaibhavsinha.grpc.RemotingServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by vaibhav on 05/07/17.
 */
public class GrpcInvokerProxyFactoryBean extends RemoteInvocationBasedAccessor implements FactoryBean<Object>, MethodInterceptor, InitializingBean {

    private Object serviceProxy;

    private ManagedChannel channel;
    private RemotingServiceGrpc.RemotingServiceBlockingStub remotingServiceBlockingStub;


    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (getServiceInterface() == null) {
            throw new IllegalArgumentException("Property 'serviceInterface' is required");
        }
        this.serviceProxy = new ProxyFactory(getServiceInterface(), this).getProxy(getBeanClassLoader());
        channel = ManagedChannelBuilder.forTarget(getServiceUrl()).usePlaintext(true).build();
        remotingServiceBlockingStub = RemotingServiceGrpc.newBlockingStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override
    public Object getObject() throws Exception {
        return serviceProxy;
    }

    @Override
    public Class<?> getObjectType() {
        return getServiceInterface();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        RemoteInvocation remoteInvocation = new RemoteInvocation(invocation);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(remoteInvocation);
        RemoteInvocationResponse remoteInvocationResponse = remotingServiceBlockingStub.execute(RemoteInvocationRequest.newBuilder().setData(ByteString.copyFrom(out.toByteArray())).build());
        ByteArrayInputStream in = new ByteArrayInputStream(remoteInvocationResponse.getData().toByteArray());
        ObjectInputStream is = new ObjectInputStream(in);
        RemoteInvocationResult remoteInvocationResult = (RemoteInvocationResult) is.readObject();
        return remoteInvocationResult.getValue();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        shutdown();
    }
}
