package org.springframework.cloud.fn.consumer.sftp;

import java.io.File;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.fn.test.support.sftp.SftpTestSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"sftp.consumer.remoteDir = sftpTarget",
				"sftp.consumer.factory.username = foo",
				"sftp.consumer.factory.password = foo",
				"sftp.consumer.mode = FAIL",
				"sftp.consumer.filenameExpression = payload.name.toUpperCase()",
				"sftp.consumer.factory.allowUnknownKeys = true"
		})
public class SftpConsumerTests extends SftpTestSupport {

	@Autowired
	Consumer<Message<?>> sftpConsumer;

	@Test
	public void sendFiles() {
		for (int i = 1; i <= 2; i++) {
			String pathname = "/localSource" + i + ".txt";
			String upperPathname = pathname.toUpperCase();
			new File(getTargetRemoteDirectory() + upperPathname).delete();
			assertThat(new File(getTargetRemoteDirectory() + upperPathname).exists()).isFalse();
			sftpConsumer.accept(new GenericMessage<>(new File(getSourceLocalDirectory() + pathname)));
			File expected = new File(getTargetRemoteDirectory() + upperPathname);
			assertThat(expected.exists()).isTrue();
			// verify the uppercase on a case-insensitive file system
			File[] files = getTargetRemoteDirectory().listFiles();
			for (File file : files) {
				assertThat(file.getName().startsWith("LOCALSOURCE")).isTrue();
			}
		}
	}

	@Test
	public void serverRefreshed() { // noop test to test the dirs are refreshed properly
		String pathname = "/LOCALSOURCE1.TXT";
		assertThat(getTargetRemoteDirectory().exists()).isTrue();
		assertThat(new File(getTargetRemoteDirectory() + pathname).exists()).isFalse();
	}

	@SpringBootApplication
	static class TestApplication {
	}
}

