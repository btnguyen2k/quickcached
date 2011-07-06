package org.quickcached.cache.impl.h2database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickcached.QuickCached;

/**
 * H2 based implementation
 * @author Ifteqar Ahmed
 * @author Akshathkumar Shetty
 */
public class PurgeOldData {
	private static final Logger logger = Logger.getLogger(PurgeOldData.class.getName());

	public static void startCleanUp() throws Exception {
		Thread t = new Thread("PurgeOldData") {
			public void run() {
				int sleepTime = 1000 * 60;
				while(true) {
					try {
						initiateCleanig();
						Thread.sleep(sleepTime);
					} catch (Exception e) {
						logger.warning("Error :" + e);
					}
				}
			}

			private void initiateCleanig() {
				Connection con = null;
				PreparedStatement pstmt = null;

				try {
					con = H2CacheImpl.getConnection();
					java.sql.Timestamp spanOfTime = new java.sql.Timestamp(System.currentTimeMillis());

					String sql = "DELETE FROM DATA_CACHE WHERE EXPIRY_TIME_STAMP < ?";
					pstmt = con.prepareStatement(sql);
					pstmt.setTimestamp(1, spanOfTime);
					int rowsDeleted = pstmt.executeUpdate();

					if(QuickCached.DEBUG) logger.fine("Records expired: " + rowsDeleted);
				} catch (Exception e) {
					logger.log(Level.WARNING, "Error: " + e, e);
				} finally {
					try {
						pstmt.close();
					} catch (SQLException e1) {
						logger.log(Level.WARNING, "Error: " + e1, e1);
					}
					try {
						con.close();
					} catch (SQLException e1) {
						logger.log(Level.WARNING, "Error: " + e1, e1);
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}
}
