package com.meshulro;

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
	protected String m_tag;

	protected double m_value;

	protected String m_account;

	protected Logger m_logger;

	public String tag() {
		return m_tag;
	}

	public double value() {
		return m_value;
	}

	public String account() {
		return m_account;
	}

	public void tag(String v) {
		m_tag = v;
	}

	public void value(double v) {
		m_value = v;
	}

	public void account(String v) {
		m_account = v;
	}

	// ! [socket_declare]
	private final EReaderSignal readerSignal;
	private final EClientSocket clientSocket;
	protected int currentOrderId = -1;
	// ! [socket_declare]

	// ! [socket_init]
	public EWrapperImpl(Logger p_logger) {
		m_logger = p_logger;
		readerSignal = new EJavaSignal();
		clientSocket = new EClientSocket(this, readerSignal);
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

	// ! [tickprice]
	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		m_logger.info("Tick Price. Ticker Id:" + tickerId + ", Field: " + field + ", Price: " + price
				+ ", CanAutoExecute: " + canAutoExecute);
	}
	// ! [tickprice]

	// ! [ticksize]
	@Override
	public void tickSize(int tickerId, int field, int size) {
		m_logger.info("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);
	}
	// ! [ticksize]

	// ! [tickoptioncomputation]
	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice) {
		m_logger.info("TickOptionComputation. TickerId: " + tickerId + ", field: " + field + ", ImpliedVolatility: "
				+ impliedVol + ", Delta: " + delta + ", OptionPrice: " + optPrice + ", pvDividend: " + pvDividend
				+ ", Gamma: " + gamma + ", Vega: " + vega + ", Theta: " + theta + ", UnderlyingPrice: " + undPrice);
	}
	// ! [tickoptioncomputation]

	// ! [tickgeneric]
	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		m_logger.info("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: "
				+ value);
	}
	// ! [tickgeneric]

	// ! [tickstring]
	@Override
	public void tickString(int tickerId, int tickType, String value) {
		m_logger.info("Tick string. Ticker Id:" + tickerId + ", Type: " + tickType + ", Value: " + value);
	}

	// ! [tickstring]
	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
			double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact,
			double dividendsToLastTradeDate) {
		m_logger.info("TickEFP. " + tickerId + ", Type: " + tickType + ", BasisPoints: " + basisPoints
				+ ", FormattedBasisPoints: " + formattedBasisPoints + ", ImpliedFuture: " + impliedFuture
				+ ", HoldDays: " + holdDays + ", FutureLastTradeDate: " + futureLastTradeDate + ", DividendImpact: "
				+ dividendImpact + ", DividendsToLastTradeDate: " + dividendsToLastTradeDate);
	}

	// ! [orderstatus]
	@Override
	public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice,
			int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		m_logger.info("OrderStatus. Id: " + orderId + ", Status: " + status + ", Filled: " + filled + ", Remaining: "
				+ remaining + ", AvgFillPrice: " + avgFillPrice + ", PermId: " + permId + ", ParentId: " + parentId
				+ ", LastFillPrice: " + lastFillPrice + ", ClientId: " + clientId + ", WhyHeld: " + whyHeld);
	}
	// ! [orderstatus]

	// ! [openorder]
	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
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
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		m_logger.info("UpdateAccountValue. Key: " + key + ", Value: " + value + ", Currency: " + currency
				+ ", AccountName: " + accountName);
	}
	// ! [updateaccountvalue]

	// ! [updateportfolio]
	@Override
	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
			double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		m_logger.info("UpdatePortfolio. " + contract.symbol() + ", " + contract.secType() + " @ " + contract.exchange()
				+ ": Position: " + position + ", MarketPrice: " + marketPrice + ", MarketValue: " + marketValue
				+ ", AverageCost: " + averageCost + ", UnrealisedPNL: " + unrealizedPNL + ", RealisedPNL: "
				+ realizedPNL + ", AccountName: " + accountName);
	}
	// ! [updateportfolio]

	// ! [updateaccounttime]
	@Override
	public void updateAccountTime(String timeStamp) {
		m_logger.info("UpdateAccountTime. Time: " + timeStamp + "\n");
	}
	// ! [updateaccounttime]

	// ! [accountdownloadend]
	@Override
	public void accountDownloadEnd(String accountName) {
		m_logger.info("Account download finished: " + accountName + "\n");
	}
	// ! [accountdownloadend]

	// ! [nextvalidid]
	@Override
	public void nextValidId(int orderId) {
		m_logger.info("Next Valid Id: [" + orderId + "]");
		currentOrderId = orderId;
	}
	// ! [nextvalidid]

	// ! [contractdetails]
	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		m_logger.info("ContractDetails. ReqId: [" + reqId + "] - [" + contractDetails.contract().symbol() + "], ["
				+ contractDetails.contract().secType() + "], ConId: [" + contractDetails.contract().conid() + "] @ ["
				+ contractDetails.contract().exchange() + "]");
	}

	// ! [contractdetails]
	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		m_logger.info("bondContractDetails");
	}

	// ! [contractdetailsend]
	@Override
	public void contractDetailsEnd(int reqId) {
		m_logger.info("ContractDetailsEnd. " + reqId + "\n");
	}
	// ! [contractdetailsend]

	// ! [execdetails]
	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		m_logger.info("ExecDetails. " + reqId + " - [" + contract.symbol() + "], [" + contract.secType() + "], ["
				+ contract.currency() + "], [" + execution.execId() + "], [" + execution.orderId() + "], ["
				+ execution.shares() + "]");
	}
	// ! [execdetails]

	// ! [execdetailsend]
	@Override
	public void execDetailsEnd(int reqId) {
		m_logger.info("ExecDetailsEnd. " + reqId + "\n");
	}
	// ! [execdetailsend]

	// ! [updatemktdepth]
	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		m_logger.info("UpdateMarketDepth. " + tickerId + " - Position: " + position + ", Operation: " + operation
				+ ", Side: " + side + ", Price: " + price + ", Size: " + size + "");
	}

	// ! [updatemktdepth]
	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
			int size) {
		m_logger.info("updateMktDepthL2");
	}

	// ! [updatenewsbulletin]
	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		m_logger.info("News Bulletins. " + msgId + " - Type: " + msgType + ", Message: " + message
				+ ", Exchange of Origin: " + origExchange + "\n");
	}
	// ! [updatenewsbulletin]

	// ! [managedaccounts]
	@Override
	public void managedAccounts(String accountsList) {
		m_logger.info("Account list: " + accountsList);
	}
	// ! [managedaccounts]

	// ! [receivefa]
	@Override
	public void receiveFA(int faDataType, String xml) {
		m_logger.info("Receing FA: " + faDataType + " - " + xml);
	}
	// ! [receivefa]

	// ! [historicaldata]
	@Override
	public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume,
			int count, double WAP, boolean hasGaps) {
		m_logger.info("HistoricalData. " + reqId + " - Date: " + date + ", Open: " + open + ", High: " + high
				+ ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + WAP
				+ ", HasGaps: " + hasGaps);
	}
	// ! [historicaldata]

	// ! [scannerparameters]
	@Override
	public void scannerParameters(String xml) {
		m_logger.info("ScannerParameters. " + xml + "\n");
	}
	// ! [scannerparameters]

	// ! [scannerdata]
	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		m_logger.info("ScannerData. " + reqId + " - Rank: " + rank + ", Symbol: " + contractDetails.contract().symbol()
				+ ", SecType: " + contractDetails.contract().secType() + ", Currency: "
				+ contractDetails.contract().currency() + ", Distance: " + distance + ", Benchmark: " + benchmark
				+ ", Projection: " + projection + ", Legs String: " + legsStr);
	}
	// ! [scannerdata]

	// ! [scannerdataend]
	@Override
	public void scannerDataEnd(int reqId) {
		m_logger.info("ScannerDataEnd. " + reqId);
	}
	// ! [scannerdataend]

	// ! [realtimebar]
	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume,
			double wap, int count) {
		m_logger.info("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " + high + ", Low: "
				+ low + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + wap);
	}

	// ! [realtimebar]
	@Override
	public void currentTime(long time) {
		m_logger.info("currentTime");
	}

	// ! [fundamentaldata]
	@Override
	public void fundamentalData(int reqId, String data) {
		m_logger.info("FundamentalData. ReqId: [" + reqId + "] - Data: [" + data + "]");
	}

	// ! [fundamentaldata]
	@Override
	public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
		m_logger.info("deltaNeutralValidation");
	}

	// ! [ticksnapshotend]
	@Override
	public void tickSnapshotEnd(int reqId) {
		m_logger.info("TickSnapshotEnd: " + reqId);
	}
	// ! [ticksnapshotend]

	// ! [marketdatatype]
	@Override
	public void marketDataType(int reqId, int marketDataType) {
		m_logger.info("MarketDataType. [" + reqId + "], Type: [" + marketDataType + "]\n");
	}
	// ! [marketdatatype]

	// ! [commissionreport]
	@Override
	public void commissionReport(CommissionReport commissionReport) {
		m_logger.info("CommissionReport. [" + commissionReport.m_execId + "] - [" + commissionReport.m_commission
				+ "] [" + commissionReport.m_currency + "] RPNL [" + commissionReport.m_realizedPNL + "]");
	}
	// ! [commissionreport]

	// ! [position]
	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		System.out
				.println("Position. " + account + " - Symbol: " + contract.symbol() + ", SecType: " + contract.secType()
						+ ", Currency: " + contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost);
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
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		if (account.equals(m_account) && tag.equals(m_tag)) {
			m_logger.info("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag + ", Value: " + value
					+ ", Currency: " + currency);
			value(Double.valueOf(value));
		}
	}
	// ! [accountsummary]

	// ! [accountsummaryend]
	@Override
	public void accountSummaryEnd(int reqId) {
		m_logger.info("AccountSummaryEnd. Req Id: " + reqId + "\n");
	}

	// ! [accountsummaryend]
	@Override
	public void verifyMessageAPI(String apiData) {
		m_logger.info("verifyMessageAPI");
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		m_logger.info("verifyCompleted");
	}

	@Override
	public void verifyAndAuthMessageAPI(String apiData, String xyzChallange) {
		m_logger.info("verifyAndAuthMessageAPI");
	}

	@Override
	public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
		m_logger.info("verifyAndAuthCompleted");
	}

	// ! [displaygrouplist]
	@Override
	public void displayGroupList(int reqId, String groups) {
		m_logger.info("Display Group List. ReqId: " + reqId + ", Groups: " + groups + "\n");
	}
	// ! [displaygrouplist]

	// ! [displaygroupupdated]
	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		m_logger.info("Display Group Updated. ReqId: " + reqId + ", Contract info: " + contractInfo + "\n");
	}

	// ! [displaygroupupdated]
	@Override
	public void error(Exception e) {
		m_logger.info("Exception: " + e.getMessage());
	}

	@Override
	public void error(String str) {
		m_logger.info("Error STR");
	}

	// ! [m_logger.info]
	@Override
	public void error(int id, int errorCode, String errorMsg) {
		m_logger.info("Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg + "\n");
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
	public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos,
			double avgCost) {
		m_logger.info("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode
				+ ", Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: "
				+ contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost + "\n");
	}
	// ! [positionmulti]

	// ! [positionmultiend]
	@Override
	public void positionMultiEnd(int reqId) {
		m_logger.info("Position Multi End. Request: " + reqId + "\n");
	}
	// ! [positionmultiend]

	// ! [accountupdatemulti]
	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value,
			String currency) {
		m_logger.info("Account Update Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode
				+ ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");
	}
	// ! [accountupdatemulti]

	// ! [accountupdatemultiend]
	@Override
	public void accountUpdateMultiEnd(int reqId) {
		m_logger.info("Account Update Multi End. Request: " + reqId + "\n");
	}
	// ! [accountupdatemultiend]

	// ! [securityDefinitionOptionParameter]
	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId,
			String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {
		m_logger.info("Security Definition Optional Parameter. Request: " + reqId + ", Trading Class: " + tradingClass
				+ ", Multiplier: " + multiplier + " \n");
	}

	// ! [securityDefinitionOptionParameter]
	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		for (final SoftDollarTier tier : tiers) {
			System.out.print("tier: " + tier + ", ");
		}

		m_logger.info("");
	}

}
