package org.springframework.cloud.fn.consumer.sftp;

import java.util.function.Consumer;

import com.jcraft.jsch.ChannelSftp;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.dsl.SftpMessageHandlerSpec;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;

@Configuration
@EnableConfigurationProperties(SftpConsumerProperties.class)
@Import(SftpConsumerSessionFactoryConfiguration.class)
public class SftpConsumerConfiguration {

	private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

	@Bean
	public IntegrationFlow ftpInboundFlow(SftpConsumerProperties properties, SessionFactory<ChannelSftp.LsEntry> ftpSessionFactory) {
		IntegrationFlowBuilder integrationFlowBuilder =
				IntegrationFlows.from(MessageConsumer.class, (gateway) -> gateway.beanName("sftpConsumer"));

		SftpMessageHandlerSpec handlerSpec =
				Sftp.outboundAdapter(new SftpRemoteFileTemplate(ftpSessionFactory), properties.getMode())
						.remoteDirectory(properties.getRemoteDir())
						.remoteFileSeparator(properties.getRemoteFileSeparator())
						.autoCreateDirectory(properties.isAutoCreateDir())
						.temporaryFileSuffix(properties.getTmpFileSuffix());
		if (properties.getFilenameExpression() != null) {
			handlerSpec.fileNameExpression(EXPRESSION_PARSER.parseExpression(properties.getFilenameExpression())
					.getExpressionString());
		}
		return integrationFlowBuilder
				.handle(handlerSpec)
				.get();
	}

	private interface MessageConsumer extends Consumer<Message<?>> {

	}

}
