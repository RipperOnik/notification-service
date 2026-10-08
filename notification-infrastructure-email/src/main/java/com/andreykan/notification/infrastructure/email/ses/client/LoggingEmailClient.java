package com.andreykan.notification.infrastructure.email.ses.client;

import com.andreykan.notification.application.email.sendemail.EmailClient;
import com.andreykan.notification.application.email.sendemail.SendEmailConfirmation;
import com.andreykan.notification.application.email.sendemail.SendEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("local")
public class LoggingEmailClient implements EmailClient {
    private final String fromEmail;

    public LoggingEmailClient(@Value("${app.email.from-address}") String fromEmail) {
        this.fromEmail = fromEmail;
    }

    private static final Logger log =
            LoggerFactory.getLogger(LoggingEmailClient.class);

    @Override
    public SendEmailConfirmation send(
            SendEmailRequest request
    ) {
        String messageId =
                UUID.randomUUID().toString();

        log.info(
                "Simulated email delivery to={} from={} subject={} messageId={}",
                request.to(),
                fromEmail,
                request.subject(),
                messageId
        );

        return new SendEmailConfirmation(
                messageId
        );
    }
}
