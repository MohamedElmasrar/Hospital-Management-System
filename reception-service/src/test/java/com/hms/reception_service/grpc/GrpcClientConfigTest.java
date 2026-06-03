package com.hms.reception_service.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hms.grpc.hr.HrGrpcServiceGrpc;
import com.hms.grpc.hr.ValidateDepartmentRequest;
import com.hms.grpc.hr.ValidateDepartmentResponse;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class GrpcClientConfigTest {

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void hrCallsRelayTheCurrentRequestToken() {
    ManagedChannel channel = mock(ManagedChannel.class);
    ClientCall<ValidateDepartmentRequest, ValidateDepartmentResponse> call = mock(ClientCall.class);
    when(channel.<ValidateDepartmentRequest, ValidateDepartmentResponse>newCall(any(), any()))
        .thenReturn(call);
    var stub = new GrpcClientConfig().hrGrpcServiceBlockingStub(channel);
    var authorization = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    for (String value : new String[] {"first-request-token", "second-request-token"}) {
      var jwt = Jwt.withTokenValue(value).header("alg", "HS256").subject("receptionist").build();
      SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
      Metadata headers = new Metadata();
      stub.getChannel()
          .newCall(HrGrpcServiceGrpc.getValidateDepartmentMethod(), CallOptions.DEFAULT)
          .start(new ClientCall.Listener<>() {}, headers);

      assertThat(headers.get(authorization)).isEqualTo("Bearer " + value);
    }
  }
}
