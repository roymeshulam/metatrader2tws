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

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.ib.client.Order;

public class MetaTraderIbBridge {
	protected Logger m_logger;

	protected EWrapperImpl m_wrapper;

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

		m_wrapper = new EWrapperImpl(m_logger);
		m_wrapper.account(m_properties.getProperty("IbAccount"));
		m_wrapper.tag("AvailableFunds");

		m_ibLogFilePath = m_properties.getProperty(hostname.concat("IbLogFilePath"));
		m_orderBookFilePath = m_properties.getProperty(hostname.concat("OrderBookFilePath"));
		m_orderBookLastModified = new File(m_orderBookFilePath).lastModified();
		if (m_orderBookLastModified == 0) {
			m_logger.severe("Last modified returned 0, file does not exist or if an I/O error occurs, exiting");
			System.exit(1);
		} else {
			m_logger.info("Initialization complete, connected MetaTrader to IB Account ".concat(m_wrapper.account()));
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
						m_logger.severe("Failed reading order book file, returning");
						m_logger.severe(e.toString());
						return;
					}

					m_logger.info("New instruction");
					m_logger.info(l_metaTraderContract.toString());

					final EClientSocket l_client = m_wrapper.getClient();
					final EReaderSignal l_signal = m_wrapper.getSignal();
					l_client.eConnect("127.0.0.1", 4002, 0);

					final EReader l_reader = new EReader(l_client, l_signal);
					l_reader.start();
					new Thread() {
						@Override
						public void run() {
							while (l_client.isConnected()) {
								l_signal.waitForSignal();
								try {
									l_reader.processMsgs();
								} catch (final IOException e) {
									m_logger.severe("Reader encountered IO Exception");
									m_logger.severe(e.toString());
								}

							}
						}
					}.start();

					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						m_logger.info("Thread encountered InterruptedException");
						m_logger.info(e.toString());
					}

					if (l_metaTraderContract.action().equals("Close")) {
					} else {
						final double l_availableFunds = getAvailableFunds();
						if (l_availableFunds > 0) {
							final Contract l_contract = new Contract();
							l_contract.symbol(l_metaTraderContract.currency1());
							l_contract.secType("CFD");
							l_contract.currency(l_metaTraderContract.currency2());
							l_contract.exchange("SMART");

							final Order l_order = new Order();
							l_order.action(l_metaTraderContract.action());
							l_order.orderType("MKT");
							l_order.totalQuantity(Math.round(l_availableFunds * l_metaTraderContract.m_relativeSize));

							l_client.reqIds(-1);
							l_client.placeOrder(m_wrapper.getCurrentOrderId() + 1, l_contract, l_order);
						} else {
							m_logger.severe("getAvailableFunds returned 0, please place order manually");
						}
					}
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						m_logger.info("Thread encountered InterruptedException");
						m_logger.info(e.toString());
					}
					l_client.eDisconnect();
				}
			}
		};

		m_scheduler.scheduleAtFixedRate(orderBookReader,
				ZonedDateTime.now().getSecond() < 15 ? 15 - ZonedDateTime.now().getSecond()
						: ZonedDateTime.now().getSecond() > 45 ? 75 - ZonedDateTime.now().getSecond()
								: 45 - ZonedDateTime.now().getSecond(),
				30, SECONDS);
	}

	public static void main(String[] args) throws UnknownHostException {
		new MetaTraderIbBridge().CheckForNewTradingInstructions();
	}

	protected double getAvailableFunds() {
		final EClientSocket l_client = m_wrapper.getClient();
		l_client.reqAccountSummary(1, "All", "AvailableFunds");
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			m_logger.info("Thread encountered InterruptedException");
			m_logger.info(e.toString());
		}
		l_client.cancelAccountSummary(1);

		return m_wrapper.value();
	}

}
