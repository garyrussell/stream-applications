package org.springframework.cloud.fn.consumer.sftp;

import com.jcraft.jsch.ChannelSftp;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

/**
 * Session factory configuration.
 *
 * @author Gary Russell
 *
 */
public class SftpConsumerSessionFactoryConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory(SftpConsumerProperties properties, BeanFactory beanFactory) {
		DefaultSftpSessionFactory sftpSessionFactory = new DefaultSftpSessionFactory();
		SftpConsumerProperties.Factory factory = properties.getFactory();
		sftpSessionFactory.setHost(factory.getHost());
		sftpSessionFactory.setPort(factory.getPort());
		sftpSessionFactory.setUser(factory.getUsername());
		sftpSessionFactory.setPassword(factory.getPassword());
		sftpSessionFactory.setPrivateKey(factory.getPrivateKey());
		sftpSessionFactory.setPrivateKeyPassphrase(factory.getPassPhrase());
		sftpSessionFactory.setAllowUnknownKeys(factory.isAllowUnknownKeys());
		if (factory.getKnownHostsExpression() != null) {
			sftpSessionFactory.setKnownHosts(factory.getKnownHostsExpression()
					.getValue(IntegrationContextUtils.getEvaluationContext(beanFactory), String.class));
		}
		if (factory.getCacheSessions() != null) {
			CachingSessionFactory<ChannelSftp.LsEntry> csf = new CachingSessionFactory<>(sftpSessionFactory);
			return csf;
		}
		else {
			return sftpSessionFactory;
		}
	}
}
