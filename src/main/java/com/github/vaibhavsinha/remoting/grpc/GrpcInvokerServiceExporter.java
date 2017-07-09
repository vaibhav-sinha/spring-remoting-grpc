package com.github.vaibhavsinha.remoting.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.remoting.rmi.RemoteInvocationSerializingExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by vaibhav on 05/07/17.
 */
public class GrpcInvokerServiceExporter extends RemoteInvocationSerializingExporter {

    private Server server;

    private int port;

    public RemoteInvocationResult invokeForInvocation(RemoteInvocation remoteInvocation) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeAndCreateResult(remoteInvocation, getProxy());
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void prepare() {
        super.prepare();
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        blockUntilShutdown();
    }

    private void start() throws IOException {
        server = ServerBuilder.forPort(port).addService(new RemotingServiceImpl(this)).build().start();
        logger.info("Server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                GrpcInvokerServiceExporter.this.stop();
            }
        });
    }

    private void stop() {
        if(server != null && !server.isShutdown()) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if(server != null) {
            server.awaitTermination();
        }
    }
}
