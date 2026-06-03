package com.hms.reception_service.grpc;

import com.hms.grpc.hr.HrGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class GrpcClientConfig {

  @Value("${hr.grpc.host:localhost}")
  private String hrHost;

  @Value("${hr.grpc.port:9091}")
  private int hrPort;

  @Bean
  public ManagedChannel hrGrpcChannel() {
    return ManagedChannelBuilder.forAddress(hrHost, hrPort).usePlaintext().build();
  }

  @Bean
  public HrGrpcServiceGrpc.HrGrpcServiceBlockingStub hrGrpcServiceBlockingStub(
      ManagedChannel hrGrpcChannel) {
    return HrGrpcServiceGrpc.newBlockingStub(hrGrpcChannel)
        .withInterceptors(
            new BearerTokenAuthenticationInterceptor(
                (Supplier<String>) GrpcClientConfig::currentToken));
  }

  private static String currentToken() {
    if (SecurityContextHolder.getContext().getAuthentication()
        instanceof JwtAuthenticationToken authentication) {
      return authentication.getToken().getTokenValue();
    }
    throw new AuthenticationCredentialsNotFoundException(
        "An authenticated request is required for HR validation");
  }
}
