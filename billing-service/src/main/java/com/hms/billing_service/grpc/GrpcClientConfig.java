package com.hms.billing_service.grpc;

import com.hms.grpc.hr.HrGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    return HrGrpcServiceGrpc.newBlockingStub(hrGrpcChannel);
  }
}
