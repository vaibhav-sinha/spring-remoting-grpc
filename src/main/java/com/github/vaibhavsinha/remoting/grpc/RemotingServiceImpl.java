package com.github.vaibhavsinha.remoting.grpc;

import com.github.vaibhavsinha.grpc.RemoteInvocationRequest;
import com.github.vaibhavsinha.grpc.RemoteInvocationResponse;
import com.github.vaibhavsinha.grpc.RemotingServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by vaibhav on 05/07/17.
 */
public class RemotingServiceImpl extends RemotingServiceGrpc.RemotingServiceImplBase {

    private GrpcInvokerServiceExporter exporter;

    public RemotingServiceImpl(GrpcInvokerServiceExporter exporter) {
        this.exporter = exporter;
    }

    @Override
    public void execute(RemoteInvocationRequest request, StreamObserver<RemoteInvocationResponse> responseObserver) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(request.getData().toByteArray());
            ObjectInputStream is = new ObjectInputStream(in);
            RemoteInvocation remoteInvocation = (RemoteInvocation) is.readObject();
            RemoteInvocationResult remoteInvocationResult = exporter.invokeForInvocation(remoteInvocation);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(remoteInvocationResult);
            responseObserver.onNext(RemoteInvocationResponse.newBuilder().setData(ByteString.copyFrom(out.toByteArray())).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
