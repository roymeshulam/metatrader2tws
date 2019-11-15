package com.meshulro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TelegramHandler extends Handler {
	protected String m_URL;

	public TelegramHandler(final String p_botToken, final String p_chatId) {
		m_URL = "https://api.telegram.org/bot" + p_botToken + "/sendMessage?chat_id=@" + p_chatId + "&text=";
	}

	@Override
	public void publish(final LogRecord p_record) {
		try {
			final URLConnection l_URLConnection = new URL(m_URL + p_record.getMessage()).openConnection();
			final BufferedReader l_bufferedReader = new BufferedReader(
					new InputStreamReader(l_URLConnection.getInputStream()));
			l_bufferedReader.readLine();
			l_bufferedReader.close();
		} catch (final IOException e) {
			System.err.println("Failed sending message".concat(p_record.getMessage()));
			e.printStackTrace();
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}
