package com.meshulro;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReaderSignal;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.SoftDollarTier;
import com.ib.client.TickType;

//! [ewrapperimpl]
public class EWrapperImpl implements EWrapper {
	// ! [ewrapperimpl]
	protected String m_tags;

	protected String m_account;

	protected Logger m_logger;

	protected double m_unrealizedPnL;

	protected double m_netLiquidationByCurrency;

	protected List<Order> m_orders;

	protected List<Contract> m_contracts;

	public String account() {
		return m_account;
	}

	public double unrealizedPnL() {
		return m_unrealizedPnL;
	}

	public double netLiquidationByCurrency() {
		return m_netLiquidationByCurrency;
	}

	public List<Contract> contracts() {
		return m_contracts;
	}

	public List<Order> orders() {
		return m_orders;
	}

	public void tags(final String v) {
		m_tags = v;
	}

	public void account(final String v) {
		m_account = v;
	}

	// ! [socket_declare]
	private final EReaderSignal readerSignal;
	private final EClientSocket clientSocket;
	protected int currentOrderId = -1;
	// ! [socket_declare]

	// ! [socket_init]
	public EWrapperImpl(final Logger p_logger) {
		m_logger = p_logger;
		readerSignal = new EJavaSignal();
		clientSocket = new EClientSocket(this, readerSignal);

		m_orders = new LinkedList<>();
		m_contracts = new LinkedList<>();
	}

	// ! [socket_init]
	public EClientSocket getClient() {
		return clientSocket;
	}

	public EReaderSignal getSignal() {
		return readerSignal;
	}

	public int getCurrentOrderId() {
		return currentOrderId;
	}

	public void initialize() {
		m_netLiquidationByCurrency = 0;
		m_unrealizedPnL = 0;
		m_orders.clear();
		m_contracts.clear();
	}

	// ! [tickprice]
	@Override
	public void tickPrice(final int tickerId, final int field, final double price, final int canAutoExecute) {
		m_logger.info("Tick Price. Ticker Id:" + tickerId + ", Field: " + field + ", Price: " + price
				+ ", CanAutoExecute: " + canAutoExecute);
	}
	// ! [tickprice]

	// ! [ticksize]
	@Override
	public void tickSize(final int tickerId, final int field, final int size) {
		m_logger.info("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);
	}
	// ! [ticksize]

	// ! [tickoptioncomputation]
	@Override
	public void tickOptionComputation(final int tickerId, final int field, final double impliedVol, final double delta,
			final double optPrice, final double pvDividend, final double gamma, final double vega, final double theta,
			final double undPrice) {
		m_logger.info("TickOptionComputation. TickerId: " + tickerId + ", field: " + field + ", ImpliedVolatility: "
				+ impliedVol + ", Delta: " + delta + ", OptionPrice: " + optPrice + ", pvDividend: " + pvDividend
				+ ", Gamma: " + gamma + ", Vega: " + vega + ", Theta: " + theta + ", UnderlyingPrice: " + undPrice);
	}
	// ! [tickoptioncomputation]

	// ! [tickgeneric]
	@Override
	public void tickGeneric(final int tickerId, final int tickType, final double value) {
		m_logger.info("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: "
				+ value);
	}
	// ! [tickgeneric]

	// ! [tickstring]
	@Override
	public void tickString(final int tickerId, final int tickType, final String value) {
		m_logger.info("Tick string. Ticker Id:" + tickerId + ", Type: " + tickType + ", Value: " + value);
	}

	// ! [tickstring]
	@Override
	public void tickEFP(final int tickerId, final int tickType, final double basisPoints,
			final String formattedBasisPoints, final double impliedFuture, final int holdDays,
			final String futureLastTradeDate, final double dividendImpact, final double dividendsToLastTradeDate) {
		m_logger.info("TickEFP. " + tickerId + ", Type: " + tickType + ", BasisPoints: " + basisPoints
				+ ", FormattedBasisPoints: " + formattedBasisPoints + ", ImpliedFuture: " + impliedFuture
				+ ", HoldDays: " + holdDays + ", FutureLastTradeDate: " + futureLastTradeDate + ", DividendImpact: "
				+ dividendImpact + ", DividendsToLastTradeDate: " + dividendsToLastTradeDate);
	}

	// ! [orderstatus]
	@Override
	public void orderStatus(final int orderId, final String status, final double filled, final double remaining,
			final double avgFillPrice, final int permId, final int parentId, final double lastFillPrice,
			final int clientId, final String whyHeld) {
		m_logger.info("OrderStatus. Id: " + orderId + ", Status: " + status + ", Filled: " + filled + ", Remaining: "
				+ remaining + ", AvgFillPrice: " + avgFillPrice + ", PermId: " + permId + ", ParentId: " + parentId
				+ ", LastFillPrice: " + lastFillPrice + ", ClientId: " + clientId + ", WhyHeld: " + whyHeld);
	}
	// ! [orderstatus]

	// ! [openorder]
	@Override
	public void openOrder(final int orderId, final Contract contract, final Order order, final OrderState orderState) {
		m_logger.info("OpenOrder. ID: " + orderId + ", " + contract.symbol() + ", " + contract.secType() + " @ "
				+ contract.exchange() + ": " + order.action() + ", " + order.orderType() + " " + order.totalQuantity()
				+ ", " + orderState.status());
	}
	// ! [openorder]

	// ! [openorderend]
	@Override
	public void openOrderEnd() {
		m_logger.info("OpenOrderEnd");
	}
	// ! [openorderend]

	// ! [updateaccountvalue]
	@Override
	public void updateAccountValue(final String key, final String value, final String currency,
			final String accountName) {
		m_logger.info("UpdateAccountValue. Key: " + key + ", Value: " + value + ", Currency: " + currency
				+ ", AccountName: " + accountName);
	}
	// ! [updateaccountvalue]

	// ! [updateportfolio]
	@Override
	public void updatePortfolio(final Contract contract, final double position, final double marketPrice,
			final double marketValue, final double averageCost, final double unrealizedPNL, final double realizedPNL,
			final String accountName) {
		m_logger.info("UpdatePortfolio. " + contract.symbol() + ", " + contract.secType() + " @ " + contract.exchange()
				+ ": Position: " + position + ", MarketPrice: " + marketPrice + ", MarketValue: " + marketValue
				+ ", AverageCost: " + averageCost + ", UnrealisedPNL: " + unrealizedPNL + ", RealisedPNL: "
				+ realizedPNL + ", AccountName: " + accountName);
	}
	// ! [updateportfolio]

	// ! [updateaccounttime]
	@Override
	public void updateAccountTime(final String timeStamp) {
		m_logger.info("UpdateAccountTime. Time: " + timeStamp + "\n");
	}
	// ! [updateaccounttime]

	// ! [accountdownloadend]
	@Override
	public void accountDownloadEnd(final String accountName) {
		m_logger.info("Account download finished: " + accountName + "\n");
	}
	// ! [accountdownloadend]

	// ! [nextvalidid]
	@Override
	public void nextValidId(final int orderId) {
		m_logger.info("Next Valid Id: [" + orderId + "]");
		currentOrderId = orderId;
	}
	// ! [nextvalidid]

	// ! [contractdetails]
	@Override
	public void contractDetails(final int reqId, final ContractDetails contractDetails) {
		m_logger.info("ContractDetails. ReqId: [" + reqId + "] - [" + contractDetails.contract().symbol() + "], ["
				+ contractDetails.contract().secType() + "], ConId: [" + contractDetails.contract().conid() + "] @ ["
				+ contractDetails.contract().exchange() + "]");
	}

	// ! [contractdetails]
	@Override
	public void bondContractDetails(final int reqId, final ContractDetails contractDetails) {
		m_logger.info("bondContractDetails");
	}

	// ! [contractdetailsend]
	@Override
	public void contractDetailsEnd(final int reqId) {
		m_logger.info("ContractDetailsEnd. " + reqId + "\n");
	}
	// ! [contractdetailsend]

	// ! [execdetails]
	@Override
	public void execDetails(final int reqId, final Contract contract, final Execution execution) {
		m_logger.info("ExecDetails. " + reqId + " - [" + contract.symbol() + "], [" + contract.secType() + "], ["
				+ contract.currency() + "], [" + execution.execId() + "], [" + execution.orderId() + "], ["
				+ execution.shares() + "]");
	}
	// ! [execdetails]

	// ! [execdetailsend]
	@Override
	public void execDetailsEnd(final int reqId) {
		m_logger.info("ExecDetailsEnd. " + reqId + "\n");
	}
	// ! [execdetailsend]

	// ! [updatemktdepth]
	@Override
	public void updateMktDepth(final int tickerId, final int position, final int operation, final int side,
			final double price, final int size) {
		m_logger.info("UpdateMarketDepth. " + tickerId + " - Position: " + position + ", Operation: " + operation
				+ ", Side: " + side + ", Price: " + price + ", Size: " + size + "");
	}

	// ! [updatemktdepth]
	@Override
	public void updateMktDepthL2(final int tickerId, final int position, final String marketMaker, final int operation,
			final int side, final double price, final int size) {
		m_logger.info("updateMktDepthL2");
	}

	// ! [updatenewsbulletin]
	@Override
	public void updateNewsBulletin(final int msgId, final int msgType, final String message,
			final String origExchange) {
		m_logger.info("News Bulletins. " + msgId + " - Type: " + msgType + ", Message: " + message
				+ ", Exchange of Origin: " + origExchange + "\n");
	}
	// ! [updatenewsbulletin]

	// ! [managedaccounts]
	@Override
	public void managedAccounts(final String accountsList) {
		m_logger.info("Account list: " + accountsList);
	}
	// ! [managedaccounts]

	// ! [receivefa]
	@Override
	public void receiveFA(final int faDataType, final String xml) {
		m_logger.info("Receing FA: " + faDataType + " - " + xml);
	}
	// ! [receivefa]

	// ! [historicaldata]
	@Override
	public void historicalData(final int reqId, final String date, final double open, final double high,
			final double low, final double close, final int volume, final int count, final double WAP,
			final boolean hasGaps) {
		m_logger.info("HistoricalData. " + reqId + " - Date: " + date + ", Open: " + open + ", High: " + high
				+ ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + WAP
				+ ", HasGaps: " + hasGaps);
	}
	// ! [historicaldata]

	// ! [scannerparameters]
	@Override
	public void scannerParameters(final String xml) {
		m_logger.info("ScannerParameters. " + xml + "\n");
	}
	// ! [scannerparameters]

	// ! [scannerdata]
	@Override
	public void scannerData(final int reqId, final int rank, final ContractDetails contractDetails,
			final String distance, final String benchmark, final String projection, final String legsStr) {
		m_logger.info("ScannerData. " + reqId + " - Rank: " + rank + ", Symbol: " + contractDetails.contract().symbol()
				+ ", SecType: " + contractDetails.contract().secType() + ", Currency: "
				+ contractDetails.contract().currency() + ", Distance: " + distance + ", Benchmark: " + benchmark
				+ ", Projection: " + projection + ", Legs String: " + legsStr);
	}
	// ! [scannerdata]

	// ! [scannerdataend]
	@Override
	public void scannerDataEnd(final int reqId) {
		m_logger.info("ScannerDataEnd. " + reqId);
	}
	// ! [scannerdataend]

	// ! [realtimebar]
	@Override
	public void realtimeBar(final int reqId, final long time, final double open, final double high, final double low,
			final double close, final long volume, final double wap, final int count) {
		m_logger.info("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " + high + ", Low: "
				+ low + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + wap);
	}

	// ! [realtimebar]
	@Override
	public void currentTime(final long time) {
		m_logger.info("currentTime");
	}

	// ! [fundamentaldata]
	@Override
	public void fundamentalData(final int reqId, final String data) {
		m_logger.info("FundamentalData. ReqId: [" + reqId + "] - Data: [" + data + "]");
	}

	// ! [fundamentaldata]
	@Override
	public void deltaNeutralValidation(final int reqId, final DeltaNeutralContract underComp) {
		m_logger.info("deltaNeutralValidation");
	}

	// ! [ticksnapshotend]
	@Override
	public void tickSnapshotEnd(final int reqId) {
		m_logger.info("TickSnapshotEnd: " + reqId);
	}
	// ! [ticksnapshotend]

	// ! [marketdatatype]
	@Override
	public void marketDataType(final int reqId, final int marketDataType) {
		m_logger.info("MarketDataType. [" + reqId + "], Type: [" + marketDataType + "]\n");
	}
	// ! [marketdatatype]

	// ! [commissionreport]
	@Override
	public void commissionReport(final CommissionReport commissionReport) {
		m_logger.info("CommissionReport. [" + commissionReport.m_execId + "] - [" + commissionReport.m_commission
				+ "] [" + commissionReport.m_currency + "] RPNL [" + commissionReport.m_realizedPNL + "]");
	}
	// ! [commissionreport]

	// ! [position]
	@Override
	public void position(final String account, final Contract contract, final double pos, final double avgCost) {
		if (account.equals(m_account) && pos > 0) {
			final Order l_order = new Order();
			l_order.action(pos > 0 ? "Buy" : "Sell");
			l_order.orderType("MKT");
			l_order.totalQuantity(Math.abs(pos));
			m_orders.add(l_order);

			contract.exchange(contract.getSecType().equals("CFD") ? "SMART" : "IDEALPRO");
			m_contracts.add(contract);

			m_logger.info(
					"Position. " + account + " - Symbol: " + contract.symbol() + ", SecType: " + contract.secType()
							+ ", Currency: " + contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost);
		}
	}
	// ! [position]

	// ! [positionend]
	@Override
	public void positionEnd() {
		m_logger.info("PositionEnd \n");
	}
	// ! [positionend]

	// ! [accountsummary]
	@Override
	public void accountSummary(final int reqId, final String account, final String tag, final String value,
			final String currency) {
		if (account.equals(m_account) && m_tags.contains(tag)) {
			m_logger.info("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag + ", Value: " + value
					+ ", Currency: " + currency);

			if ("NetLiquidationByCurrency".equals(tag)) {
				m_netLiquidationByCurrency = Double.valueOf(value);
			} else if ("UnrealizedPnL".equals(tag)) {
				m_unrealizedPnL = Double.valueOf(value);
			}
		}
	}
	// ! [accountsummary]

	// ! [accountsummaryend]
	@Override
	public void accountSummaryEnd(final int reqId) {
		m_logger.info("AccountSummaryEnd. Req Id: " + reqId + "\n");
	}

	// ! [accountsummaryend]
	@Override
	public void verifyMessageAPI(final String apiData) {
		m_logger.info("verifyMessageAPI");
	}

	@Override
	public void verifyCompleted(final boolean isSuccessful, final String errorText) {
		m_logger.info("verifyCompleted");
	}

	@Override
	public void verifyAndAuthMessageAPI(final String apiData, final String xyzChallange) {
		m_logger.info("verifyAndAuthMessageAPI");
	}

	@Override
	public void verifyAndAuthCompleted(final boolean isSuccessful, final String errorText) {
		m_logger.info("verifyAndAuthCompleted");
	}

	// ! [displaygrouplist]
	@Override
	public void displayGroupList(final int reqId, final String groups) {
		m_logger.info("Display Group List. ReqId: " + reqId + ", Groups: " + groups + "\n");
	}
	// ! [displaygrouplist]

	// ! [displaygroupupdated]
	@Override
	public void displayGroupUpdated(final int reqId, final String contractInfo) {
		m_logger.info("Display Group Updated. ReqId: " + reqId + ", Contract info: " + contractInfo + "\n");
	}

	// ! [displaygroupupdated]
	@Override
	public void error(final Exception e) {
		m_logger.info("Exception: " + e.getMessage());
	}

	@Override
	public void error(final String str) {
		m_logger.info("Error STR");
	}

	// ! [m_logger.info]
	@Override
	public void error(final int id, final int errorCode, final String errorMsg) {
		if (id != -1) {
			m_logger.info("Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg + "\n");
		}
	}

	// ! [error]
	@Override
	public void connectionClosed() {
		m_logger.info("Connection closed");
	}

	// ! [connectack]
	@Override
	public void connectAck() {
		if (clientSocket.isAsyncEConnect()) {
			m_logger.info("Acknowledging connection");
			clientSocket.startAPI();
		}
	}
	// ! [connectack]

	// ! [positionmulti]
	@Override
	public void positionMulti(final int reqId, final String account, final String modelCode, final Contract contract,
			final double pos, final double avgCost) {
		m_logger.info("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode
				+ ", Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: "
				+ contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost + "\n");
	}
	// ! [positionmulti]

	// ! [positionmultiend]
	@Override
	public void positionMultiEnd(final int reqId) {
		m_logger.info("Position Multi End. Request: " + reqId + "\n");
	}
	// ! [positionmultiend]

	// ! [accountupdatemulti]
	@Override
	public void accountUpdateMulti(final int reqId, final String account, final String modelCode, final String key,
			final String value, final String currency) {
		m_logger.info("Account Update Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode
				+ ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");
	}
	// ! [accountupdatemulti]

	// ! [accountupdatemultiend]
	@Override
	public void accountUpdateMultiEnd(final int reqId) {
		m_logger.info("Account Update Multi End. Request: " + reqId + "\n");
	}
	// ! [accountupdatemultiend]

	// ! [securityDefinitionOptionParameter]
	@Override
	public void securityDefinitionOptionalParameter(final int reqId, final String exchange, final int underlyingConId,
			final String tradingClass, final String multiplier, final Set<String> expirations,
			final Set<Double> strikes) {
		m_logger.info("Security Definition Optional Parameter. Request: " + reqId + ", Trading Class: " + tradingClass
				+ ", Multiplier: " + multiplier + " \n");
	}

	// ! [securityDefinitionOptionParameter]
	@Override
	public void securityDefinitionOptionalParameterEnd(final int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void softDollarTiers(final int reqId, final SoftDollarTier[] tiers) {
		for (final SoftDollarTier tier : tiers) {
			m_logger.info("tier: " + tier + ", ");
		}

		m_logger.info("");
	}

}
