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

		String l_hostname = null;
		try {
			l_hostname = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			m_logger.severe("Failed getting hostname, exiting");
			m_logger.severe(e.toString());
			System.exit(1);
		}

		m_wrapper = new EWrapperImpl(m_logger);
		m_wrapper.account(m_properties.getProperty(l_hostname.concat("IbAccount")));

		m_ibLogFilePath = m_properties.getProperty(l_hostname.concat("IbLogFilePath"));
		try {
			final FileHandler l_fileHandler = new FileHandler(m_ibLogFilePath, true);
			l_fileHandler.setFormatter(new SimpleFormatter());
			m_logger.addHandler(l_fileHandler);
		} catch (final SecurityException e) {
			m_logger.severe("Security exception creating the log file");
			m_logger.severe(e.toString());
		} catch (final IOException e) {
			m_logger.severe("IO error when creating the log file");
			m_logger.severe(e.toString());
		}

		final TelegramHandler l_telegramHandler = new TelegramHandler(
				m_properties.getProperty(l_hostname.concat("TelegramBotToken")),
				m_properties.getProperty(l_hostname.concat("TelegramChatId")));
		l_telegramHandler.setFormatter(new SimpleFormatter());
		m_logger.addHandler(l_telegramHandler);

		m_orderBookFilePath = m_properties.getProperty(l_hostname.concat("OrderBookFilePath"));
		m_orderBookLastModified = new File(m_orderBookFilePath).lastModified();
		if (m_orderBookLastModified == 0) {
			m_logger.severe("Last modified returned 0, file does not exist or if an I/O error occurs, exiting");
			System.exit(1);
		} else {
			m_logger.info("Initialization complete on ".concat(l_hostname)
					.concat(", connected MetaTrader to IB Account ").concat(m_wrapper.account()));
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					m_logger.info("Performing shutdown");
				}
			});
		}
	}

	public void CheckForNewTradingInstructions() {
		final Runnable orderBookReader = () -> {
			final File file = new File(m_orderBookFilePath);
			if (file.lastModified() != 0 && file.lastModified() != m_orderBookLastModified) {
				m_orderBookLastModified = file.lastModified();

				MetaTraderContract l_metaTraderContract = null;
				try {
					final BufferedReader br = new BufferedReader(new FileReader(file));
					l_metaTraderContract = MetaTraderContract.FromString(br.readLine());
					br.close();
				} catch (final FileNotFoundException e1) {
					m_logger.severe("Order book file wasn't found, returning");
					m_logger.severe(e1.toString());
					return;
				} catch (final IOException e2) {
					m_logger.severe("Failed reading order book file, returning");
					m_logger.severe(e2.toString());
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
					Thread.sleep(2000);
				} catch (final InterruptedException e3) {
					m_logger.info("Thread encountered InterruptedException");
					m_logger.info(e3.toString());
				}

				m_wrapper.getClient().reqIds(-1);
				try {
					Thread.sleep(10000);
				} catch (final InterruptedException e) {
					m_logger.info("Thread encountered InterruptedException");
					m_logger.info(e.toString());
				}
				int l_currentOrderId = m_wrapper.getCurrentOrderId();

				if (l_metaTraderContract.action().equals("Close")) {
					m_logger.info("Cancelling all orders");
					m_wrapper.getClient().reqGlobalCancel();
					try {
						Thread.sleep(5000);
					} catch (final InterruptedException e) {
						m_logger.info("Thread encountered InterruptedException");
						m_logger.info(e.toString());
					}

					m_logger.info("Closing all positions");
					m_wrapper.getClient().reqPositions();
					try {
						Thread.sleep(5000);
					} catch (final InterruptedException e) {
						m_logger.info("Thread encountered InterruptedException");
						m_logger.info(e.toString());
					}
					m_wrapper.getClient().cancelPositions();

					for (int i = m_wrapper.orders().size() - 1; i >= 0; i--) {
						final Order l_marketOrder = new Order();
						l_marketOrder.action(m_wrapper.orders().get(i).getAction().equals("Buy") ? "Sell" : "Buy");
						l_marketOrder.orderType("MKT");
						l_marketOrder.totalQuantity(m_wrapper.orders().get(i).totalQuantity());

						m_logger.info("Placing market order");
						l_client.placeOrder(l_currentOrderId++, m_wrapper.contracts().get(i), l_marketOrder);
						try {
							Thread.sleep(5000);
						} catch (final InterruptedException e) {
							m_logger.info("Thread encountered InterruptedException");
							m_logger.info(e.toString());
						}
					}
					m_wrapper.initialize();
				} else {
					m_wrapper.tags("NetLiquidationByCurrency,UnrealizedPnL");
					m_wrapper.getClient().reqAccountSummary(1, "All", "$LEDGER");
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						m_logger.info("Thread encountered InterruptedException");
						m_logger.info(e.toString());
					}
					m_wrapper.getClient().cancelAccountSummary(1);

					final double l_netLiquidation = m_wrapper.netLiquidationByCurrency();
					m_logger.info("Net Liquidation = ".concat(Double.toString(l_netLiquidation)));

					final double l_unrealizedPnL = m_wrapper.unrealizedPnL();
					m_logger.info("Unrealized PnL = ".concat(Double.toString(l_unrealizedPnL)));

					m_logger.info("Gross Liquidation = ".concat(Double.toString(l_netLiquidation - l_unrealizedPnL)));

					final double l_totalQuantity = Math
							.round(l_metaTraderContract.m_relativeSize * (l_netLiquidation - l_unrealizedPnL));
					m_logger.info("Total Quantity = ".concat(Double.toString(l_totalQuantity)));
					if (l_totalQuantity > 0) {
						final Contract l_marketContract = new Contract();
						l_marketContract.secType("CFD");
						l_marketContract.exchange("SMART");
						l_marketContract.symbol(l_metaTraderContract.currency1());
						l_marketContract.currency(l_metaTraderContract.currency2());

						final Order l_marketOrder = new Order();
						l_marketOrder.action(l_metaTraderContract.action());
						l_marketOrder.orderType("MKT");
						l_marketOrder.totalQuantity(l_totalQuantity);

						m_logger.info("Placing market order");
						l_client.placeOrder(l_currentOrderId++, l_marketContract, l_marketOrder);
						try {
							Thread.sleep(5000);
						} catch (final InterruptedException e) {
							m_logger.info("Thread encountered InterruptedException");
							m_logger.info(e.toString());
						}

						if (l_metaTraderContract.m_takeProfit > 0) {
							final Contract l_limitContract = new Contract();
							l_limitContract.secType("CFD");
							l_limitContract.exchange("SMART");
							l_limitContract.symbol(l_metaTraderContract.currency1());
							l_limitContract.currency(l_metaTraderContract.currency2());

							final Order l_limitOrder = new Order();
							l_limitOrder.action(l_metaTraderContract.action() == "Buy" ? "Sell" : "Buy");
							l_limitOrder.tif("GTC");
							l_limitOrder.orderType("LMT");
							l_limitOrder.totalQuantity(l_totalQuantity);
							l_limitOrder.lmtPrice(l_metaTraderContract.takepProfit());

							m_logger.info("Placing limit order");
							l_client.placeOrder(l_currentOrderId++, l_limitContract, l_limitOrder);
							try {
								Thread.sleep(15000);
							} catch (final InterruptedException e) {
								m_logger.info("Thread encountered InterruptedException");
								m_logger.info(e.toString());
							}
						}
					} else {
						m_logger.severe("Total quantity = 0, please place order manually");
					}
				}
				try {
					Thread.sleep(60000);
				} catch (final InterruptedException e) {
					m_logger.info("Thread encountered InterruptedException");
					m_logger.info(e.toString());
				}
				l_client.eDisconnect();
			}
		};

		m_scheduler.scheduleAtFixedRate(orderBookReader,
				ZonedDateTime.now().getSecond() < 15 ? 15 - ZonedDateTime.now().getSecond()
						: ZonedDateTime.now().getSecond() > 45 ? 75 - ZonedDateTime.now().getSecond()
								: 45 - ZonedDateTime.now().getSecond(),
				30, SECONDS);
	}

	public static void main(final String[] args) {
		new MetaTraderIbBridge().CheckForNewTradingInstructions();
	}
}