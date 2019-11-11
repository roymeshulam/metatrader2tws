package com.meshulro;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MetaTraderIbBridge {
	protected Logger m_logger;

	protected String m_ibAccount;

	protected String m_ibLogFilePath;

	protected String m_orderBookFilePath;

	protected long m_orderBookLastModified;

	protected final Properties m_properties = new Properties();

	protected final ScheduledExecutorService m_scheduler = Executors.newScheduledThreadPool(1);

	public MetaTraderIbBridge() {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
		m_logger = java.util.logging.Logger.getLogger(MetaTraderIbBridge.class.getName());

		try {
			final FileHandler fh = new FileHandler(System.getProperty("user.dir").concat("/logs/log.txt"), true);
			fh.setFormatter(new SimpleFormatter());
			m_logger.addHandler(fh);
		} catch (final SecurityException e) {
			m_logger.severe("Security exception creating the log file");
			m_logger.severe(e.toString());
		} catch (final IOException e) {
			m_logger.severe("IO error when creating the log file");
			m_logger.severe(e.toString());
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(System.getProperty("user.dir").concat("/resources/config.properties"));
		} catch (final FileNotFoundException e) {
			m_logger.severe("Config properties file wasn't found, exiting");
			m_logger.severe(e.toString());
			System.exit(1);
		}

		try {
			m_properties.load(fis);
		} catch (final IOException e) {
			m_logger.severe("Failed reading config properties file, exiting");
			m_logger.severe(e.toString());
			System.exit(1);
		}

		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			m_logger.severe("Failed getting hostname, exiting");
			m_logger.severe(e.toString());
			System.exit(1);
		}

		m_ibAccount = m_properties.getProperty("IbAccount");
		m_ibLogFilePath = m_properties.getProperty(hostname.concat("IbLogFilePath"));
		m_orderBookFilePath = m_properties.getProperty(hostname.concat("OrderBookFilePath"));
		m_orderBookLastModified = new File(m_orderBookFilePath).lastModified();
		if (m_orderBookLastModified == 0) {
			m_logger.severe("Last modified returned 0, file does not exist or if an I/O error occurs, exiting");
			System.exit(1);
		} else {
			m_logger.info("Initialization complete, connected MetaTrader to IB Account ".concat(m_ibAccount));
			m_logger.info("Order book last modified date "
					+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(m_orderBookLastModified));
		}
	}

	public void CheckForNewTradingInstructions() {
		final Runnable orderBookReader = new Runnable() {
			@Override
			public void run() {
				final File file = new File(m_orderBookFilePath);
				if (file.lastModified() != 0 && file.lastModified() != m_orderBookLastModified) {
					m_orderBookLastModified = file.lastModified();

					MetaTraderContract l_metaTraderContract = null;
					try {
						final BufferedReader br = new BufferedReader(new FileReader(file));
						l_metaTraderContract = MetaTraderContract.FromString(br.readLine());
						br.close();
					} catch (final FileNotFoundException e) {
						m_logger.severe("Order book file wasn't found, returning");
						m_logger.severe(e.toString());
						return;
					} catch (final IOException e) {
						m_logger.severe("Failed reading order book file, returing");
						m_logger.severe(e.toString());
						return;
					}

					m_logger.info("Order book last modify date "
							+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(m_orderBookLastModified));
					m_logger.info(l_metaTraderContract.toString());
					if (l_metaTraderContract.instruction().equals("Close")) {

					} else {

					}
				}
			}
		};

		m_scheduler.scheduleAtFixedRate(orderBookReader,
				ZonedDateTime.now().getSecond() > 30 ? 90 - ZonedDateTime.now().getSecond()
						: 30 - ZonedDateTime.now().getSecond(),
				60, SECONDS);
	}

	public static void main(String[] args) throws UnknownHostException {
		new MetaTraderIbBridge().CheckForNewTradingInstructions();
	}
}
