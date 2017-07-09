package com.github.vaibhavsinha.remoting.grpc;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by vaibhav on 05/07/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RemotingTest.TestConfig.class)
public class RemotingTest {

    @Autowired
    @Qualifier("userServiceClient")
    UserService userService;

    @Test
    public void testInvocation() {
        String username = userService.getUserById(1L);
        Assert.assertEquals("Expected username does not match the one received", "Vaibhav", username);
    }

    @Configuration
    public static class TestConfig {

        @Bean
        public UserService userService() {
            return new UserServiceImpl();
        }

        @Bean
        public GrpcInvokerProxyFactoryBean grpcInvokerProxyFactoryBean() {
            GrpcInvokerProxyFactoryBean grpcInvokerProxyFactoryBean = new GrpcInvokerProxyFactoryBean();
            grpcInvokerProxyFactoryBean.setServiceUrl("localhost:8888");
            grpcInvokerProxyFactoryBean.setServiceInterface(UserService.class);
            return grpcInvokerProxyFactoryBean;
        }

        @Bean(name = "userServiceClient")
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
    }

    public interface UserService {
        String getUserById(Long id);
    }

    public static class UserServiceImpl implements UserService {

        @Override
        public String getUserById(Long id) {
            return "Vaibhav";
        }

    }
}
