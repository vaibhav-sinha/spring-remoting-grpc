# Spring Remoting gRPC

This library enables using Spring Remoting with gRPC as the underlying transport.

## Installation

The artifact is available on Maven Central Repository and can be downloaded by adding the following dependency in pom.xml

    <dependency>
        <groupId>com.github.vaibhav-sinha</groupId>
        <artifactId>spring-remoting-grpc</artifactId>
        <version>0.1.0</version>
    </dependency>

## Usage

Create a Service Exporter bean in the context of the service you want to expose

    @Bean
    public GrpcInvokerServiceExporter userServiceServer() {
        GrpcInvokerServiceExporter grpcInvokerServiceExporter = new GrpcInvokerServiceExporter();
        grpcInvokerServiceExporter.setServiceInterface(UserService.class);
        grpcInvokerServiceExporter.setService(userService());
        grpcInvokerServiceExporter.setPort(8888);
        return grpcInvokerServiceExporter;
    }

Create a Proxy of the service on the client end

    @Bean
    public UserService userServiceClient() throws Exception {
        return (UserService) grpcInvokerProxyFactoryBean().getObject();
    }

    @Bean
    public GrpcInvokerServiceExporter userServiceServer() {
        GrpcInvokerServiceExporter grpcInvokerServiceExporter = new GrpcInvokerServiceExporter();
        grpcInvokerServiceExporter.setServiceInterface(UserService.class);
        grpcInvokerServiceExporter.setService(userService());
        grpcInvokerServiceExporter.setPort(8888);
        return grpcInvokerServiceExporter;
    }

Look in the tests to find the complete example.