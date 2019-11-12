package com.meshulro;

public class MetaTraderContract {
	protected static enum Indexes {
		Action(0), Currency1(1), Currency2(2), Relative_Size(3), Stop_Loss(4), Take_Profit(5);

		private final int m_value;

		Indexes(final int p_newValue) {
			m_value = p_newValue;
		}

		public int getValue() {
			return m_value;
		}
	};

	protected String m_action;

	protected String m_currency1;

	protected String m_currency2;

	protected double m_relativeSize;

	protected double m_stopLoss;

	protected double m_takeProfit;

	// Getters
	public String action() {
		return m_action;
	}

	public String currency1() {
		return m_currency1;
	}

	public String currency2() {
		return m_currency2;
	}

	public double relativeSize() {
		return m_relativeSize;
	}

	public double stopLoss() {
		return m_stopLoss;
	}

	public double takepProfit() {
		return m_takeProfit;
	}

	// Setters
	public void action(String v) {
		m_action = v;
	}

	public void currency1(String v) {
		m_currency1 = v;
	}

	public void currency2(String v) {
		m_currency2 = v;
	}

	public void relativeSize(double v) {
		m_relativeSize = v;
	}

	public void stopLoss(double v) {
		m_stopLoss = v;
	}

	public void takeProfit(double v) {
		m_takeProfit = v;
	}

	public static MetaTraderContract FromString(String p_input) {
		final MetaTraderContract l_metaTraderContract = new MetaTraderContract();

		final String[] l_words = p_input.split(";");
		if (l_words[Indexes.Action.getValue()].equals("Close")) {
			l_metaTraderContract.action("Close");
		} else {
			l_metaTraderContract.action(l_words[Indexes.Action.getValue()].equals("Buy") ? "Buy" : "Sell");
			l_metaTraderContract.currency1(l_words[Indexes.Currency1.getValue()]);
			l_metaTraderContract.currency2(l_words[Indexes.Currency2.getValue()]);
			l_metaTraderContract.relativeSize(Double.valueOf(l_words[Indexes.Relative_Size.getValue()]));
			l_metaTraderContract.stopLoss(Double.valueOf(l_words[Indexes.Stop_Loss.getValue()]));
			l_metaTraderContract.takeProfit(Double.valueOf(l_words[Indexes.Take_Profit.getValue()]));
		}

		return l_metaTraderContract;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		add(sb, "Action", m_action);
		add(sb, "Currency 1", m_currency1);
		add(sb, "Currency 2", m_currency2);
		add(sb, "Relative Size", m_relativeSize);
		add(sb, "Stop Loss", m_stopLoss);
		add(sb, "Take Profit", m_takeProfit);

		return sb.toString();
	}

	public static void add(StringBuilder sb, String tag, Object val) {
		if (val == null || val instanceof String && ((String) val).length() == 0) {
			return;
		}

		sb.append(tag);
		sb.append('\t');
		sb.append(val);
		sb.append('\n');
	}
}
