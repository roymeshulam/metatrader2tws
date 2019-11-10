/**
 *
 */
package com.meshulro;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author roymeshulam
 *
 */
public class OrderBookReader {
///Users/roymeshulam/Library/PlayOnMac/wineprefix/MetaTrader/drive_c/users/roymeshulam/Application Data/MetaQuotes/Terminal/Common/Files
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void beepForAnHour() {
		final Runnable beeper = new Runnable() {
			@Override
			public void run() {
				System.out.println(LocalTime.now());
			}
		};

		scheduler.scheduleAtFixedRate(beeper, 60 - ZonedDateTime.now().getSecond(), 60, SECONDS);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new OrderBookReader().beepForAnHour();
	}
}
