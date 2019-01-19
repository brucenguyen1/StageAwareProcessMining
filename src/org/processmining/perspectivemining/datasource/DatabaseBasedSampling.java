package org.processmining.perspectivemining.datasource;

/**
 * This class extracts sampling window rowsets from an event log
 * @author Bruce
 *
 */
public class DatabaseBasedSampling {
//	private PerspectiveSettingObject pConfig = null;
//	
//	public DatabaseBasedSampling(PerspectiveSettingObject pConfig) {
//		this.pConfig = pConfig;
//	}
//	
//	/**
//	 * @return
//	 * @throws ClassNotFoundException
//	 * @throws SQLException
//	 */
//	public List<WindowRowSet> getSamples() throws ClassNotFoundException, SQLException {
//		String sql = "";
//		sql += "select *";
//		sql += " from app.event";
//		sql += " order by timestamp";
//		CachedRowSet crs = ConnectionManager.executeSQL(sql);
//		
//		boolean firstWindow = true;
//		Instant startWindowInstant = null;
//		WindowRowSet windowRowSet = null;
//		List<WindowRowSet> windowRsList = new ArrayList<>();
//		crs.beforeFirst();
//		while (crs.next()) {
//			if (firstWindow) {
//				startWindowInstant = crs.getTimestamp("timestamp").toInstant();
//				windowRowSet = new WindowRowSet();
//				windowRowSet.addCurrentRow(crs);
//				firstWindow = false;
//				windowRsList.add(windowRowSet);
//			}
//			else {
//				Instant currentInstant = crs.getTimestamp("timestamp").toInstant();
//				long currentDuration = currentInstant.toEpochMilli() - startWindowInstant.toEpochMilli();
//				if (1.0*currentDuration/1000 > this.pConfig.getWindowSize() || crs.isLast()) {
//					windowRsList.add(windowRowSet);
//
//					if (!crs.isLast()) {
//						startWindowInstant = currentInstant;
//						windowRowSet = new WindowRowSet();
//						windowRowSet.addCurrentRow(crs);
//					}
//					
//					break; //stop once reaching the window size or end of the dataset
//				}
//				else {
//					windowRowSet.addCurrentRow(crs);
//				}
//				
//			}
//			
//		}
//		
//		return windowRsList;
//	}
	
}
