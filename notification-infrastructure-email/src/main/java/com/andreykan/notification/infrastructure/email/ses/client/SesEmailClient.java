package com.andreykan.notification.infrastructure.email.ses.client;

import com.andreykan.notification.application.email.sendemail.EmailClient;
import com.andreykan.notification.application.email.sendemail.SendEmailConfirmation;
import com.andreykan.notification.application.email.sendemail.SendEmailRequest;
import com.andreykan.notification.application.email.sendemail.exceptions.SendEmailException;
import com.andreykan.notification.domain.notification.delivery.DeliveryFailure;
import com.andreykan.notification.domain.notification.delivery.DeliveryFailureType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.time.Instant;

@Component
@Profile("prod")
public class SesEmailClient implements EmailClient {
    private final SesV2Client sesClient;
    private final String fromEmail;


    public SesEmailClient(SesV2Client sesClient, @Value("${app.email.from-address}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    @Override
    public SendEmailConfirmation send(SendEmailRequest request) {
        try {
            var destination =
                    Destination.builder()
                            .toAddresses(request.to())
                            .build();

            var subject =
                    Content.builder()
                            .data(request.subject())
                            .charset("UTF-8")
                            .build();

            var body =
                    Body.builder()
                            .text(
                                    Content.builder()
                                            .data(request.body())
                                            .charset("UTF-8")
                                            .build()
                            )
                            .build();
            var message =
                    Message.builder()
                            .subject(subject)
                            .body(body)
                            .build();

            var sesRequest =
                    software.amazon.awssdk.services.sesv2.model.SendEmailRequest
                            .builder()
                            .fromEmailAddress(fromEmail)
                            .destination(destination)
                            .content(
                                    EmailContent.builder()
                                            .simple(message)
                                            .build()
                            ).build();

            var response =
                    sesClient.sendEmail(sesRequest);

            return new SendEmailConfirmation(response.messageId());
        }
        catch (SesV2Exception ex) {
            throw mapException(ex);
        }
    }

    private SendEmailException mapException(
            SesV2Exception ex
    ) {
        boolean isTransientFailure =
                ex.statusCode() >= 500
                        || ex.statusCode() == 429;

        var failure =
                isTransientFailure
                        ? new DeliveryFailure(DeliveryFailureType.Transient, ex.getMessage())
                        : new DeliveryFailure(DeliveryFailureType.Permanent, ex.getMessage());

        return new SendEmailException(
                ex,
                failure,
                Instant.now()
        );
    }
}
