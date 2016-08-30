package org.yeastrc.xlink.www.proxl_xml_file_import.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.yeastrc.xlink.base.constants.Database_OneTrueZeroFalse_Constants;
import org.yeastrc.xlink.db.DBConnectionFactory;
import org.yeastrc.xlink.base.proxl_xml_file_import.dao.ProxlXMLFileImportTrackingHistoryDAO;
import org.yeastrc.xlink.base.proxl_xml_file_import.dto.ProxlXMLFileImportTrackingDTO;
import org.yeastrc.xlink.base.proxl_xml_file_import.enum_classes.ProxlXMLFileImportStatus;

/**
 * 
 * table proxl_xml_file_import_tracking
 */
public class ProxlXMLFileImportTracking_ForWebAppDAO {

	private static final Logger log = Logger.getLogger(ProxlXMLFileImportTracking_ForWebAppDAO.class);
	

	//  private constructor
	private ProxlXMLFileImportTracking_ForWebAppDAO() { }
	
	/**
	 * @return newly created instance
	 */
	public static ProxlXMLFileImportTracking_ForWebAppDAO getInstance() { 
		return new ProxlXMLFileImportTracking_ForWebAppDAO(); 
	}
	
	



	/**
	 * @param id
	 * @return 
	 * @throws Exception
	 */
	public ProxlXMLFileImportStatus getStatusForId( int id ) throws Exception {


		ProxlXMLFileImportStatus result = null;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		final String sql = "SELECT status_id FROM proxl_xml_file_import_tracking WHERE id = ?";
		
		try {
			
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.PROXL );
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setInt( 1, id );
			
			rs = pstmt.executeQuery();
			
			if ( rs.next() ) {
				
				result = ProxlXMLFileImportStatus.fromValue( rs.getInt( "status_id" ) );
			}
			
		} catch ( Exception e ) {
			
			String msg = "Failed to select status, id: " + id + ", sql: " + sql;
			
			log.error( msg, e );
			
			throw e;
			

		} finally {
			
			// be sure database handles are closed
			if( rs != null ) {
				try { rs.close(); } catch( Throwable t ) { ; }
				rs = null;
			}
			
			if( pstmt != null ) {
				try { pstmt.close(); } catch( Throwable t ) { ; }
				pstmt = null;
			}
			
			if( conn != null ) {
				try { conn.close(); } catch( Throwable t ) { ; }
				conn = null;
			}
			
		}
		
		return result;
	}
	
	




//CREATE TABLE IF NOT EXISTS proxl_xml_file_import_tracking (
//  id INT UNSIGNED NOT NULL,
//  status_id TINYINT UNSIGNED NOT NULL,
//  marked_for_deletion TINYINT NOT NULL DEFAULT 0,
//  project_id INT UNSIGNED NOT NULL,
//  auth_user_id INT UNSIGNED NOT NULL,
//  search_name VARCHAR(2000) NULL,
//  insert_request_url VARCHAR(255) NOT NULL,
//  inserted_search_id INT UNSIGNED NULL,
//  upload_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
//  last_updated_date_time TIMESTAMP NOT NULL,


	/**
	 * @param item
	 * @throws Exception
	 */
	public void save( ProxlXMLFileImportTrackingDTO item ) throws Exception {
		
		
		Connection dbConnection = null;

		try {
			
			dbConnection = DBConnectionFactory.getConnection( DBConnectionFactory.PROXL );

			save( item, dbConnection );

		} finally {
			
			if( dbConnection != null ) {
				try { dbConnection.close(); } catch( Throwable t ) { ; }
				dbConnection = null;
			}
			
		}
		
	}

	/**
	 * @param item
	 * @throws Exception
	 */
	public void save( ProxlXMLFileImportTrackingDTO item, Connection dbConnection ) throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		//  Insert field "id" since not autoincrement

		final String sql = "INSERT INTO proxl_xml_file_import_tracking ( "
				+ " id, project_id, priority, status_id, marked_for_deletion, insert_request_url, "
				+ " search_name, auth_user_id,  "
				+ " remote_user_ip_address, last_updated_date_time )"
				+ " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW() )";

		try {
			
			
//			pstmt = dbConnection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
			pstmt = dbConnection.prepareStatement( sql );
			
			int counter = 0;
			
			counter++;
			pstmt.setInt( counter, item.getId() );
			counter++;
			pstmt.setInt( counter, item.getProjectId() );
			counter++;
			pstmt.setInt( counter, item.getPriority() );

			counter++;
			pstmt.setInt( counter, item.getStatus().value() );
			

			counter++;
			
			if ( item.isMarkedForDeletion() ) {

				pstmt.setInt( counter, Database_OneTrueZeroFalse_Constants.DATABASE_FIELD_TRUE );
			} else {
				pstmt.setInt( counter, Database_OneTrueZeroFalse_Constants.DATABASE_FIELD_FALSE );
			}
			
			counter++;
			pstmt.setString( counter, item.getInsertRequestURL() );
			counter++;
			pstmt.setString( counter, item.getSearchName() );
			
			counter++;
			pstmt.setInt( counter, item.getAuthUserId() );
			
			counter++;
			pstmt.setString( counter, item.getRemoteUserIpAddress() );
			
			pstmt.executeUpdate();
			
//			rs = pstmt.getGeneratedKeys();
//
//			if( rs.next() ) {
//				item.setId( rs.getInt( 1 ) );
//			} else {
//				
//				String msg = "Failed to insert ProxlXMLFileImportTrackingDTO, generated key not found.";
//				
//				log.error( msg );
//				
//				throw new Exception( msg );
//			}
			
			
		} catch ( Exception e ) {
			
			String msg = "Failed to insert ProxlXMLFileImportTrackingDTO: " + item + ", sql: " + sql;
			
			log.error( msg, e );
			
			throw e;
			
		} finally {
			
			// be sure database handles are closed
			
			
			if( rs != null ) {
				try { rs.close(); } catch( Throwable t ) { ; }
				rs = null;
			}
			if( pstmt != null ) {
				try { pstmt.close(); } catch( Throwable t ) { ; }
				pstmt = null;
			}
			

		}
		

		ProxlXMLFileImportTrackingHistoryDAO.getInstance().save( item.getStatus(), item.getId() /* ProxlXMLFileImportTrackingId */, dbConnection );		
		
	}

	

	/**
	 * @param markedForDeletion
	 * @param status
	 * @param id
	 * @return true if record updated, false otherwise
	 * @throws Exception
	 */
	public boolean updateMarkedForDeletionForIdStatus( 
			
			boolean markedForDeletion, ProxlXMLFileImportStatus status, int id,
			Integer deletedByAuthUserId ) throws Exception {


		if ( markedForDeletion ) {
			
			if ( deletedByAuthUserId == null ) {
				
				throw new IllegalArgumentException( "deletedByAuthUserId == null invalid when markedForDeletion is true" );
			}
			
		} else {
			
			if ( deletedByAuthUserId != null ) {
				
				throw new IllegalArgumentException( "deletedByAuthUserId != null invalid when markedForDeletion is false" );
			}
			
		}

		boolean recordUpdated = false;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		String sql = null;
		
		
		
		if ( markedForDeletion ) {

			sql = "UPDATE proxl_xml_file_import_tracking "
					+ " SET marked_for_deletion = " + Database_OneTrueZeroFalse_Constants.DATABASE_FIELD_TRUE
					+ " , last_updated_date_time = NOW(),"
					+ " deleted_by_auth_user_id = ?, deleted_date_time = NOW() "
					+ " WHERE id = ? AND status_id = ?";
		} else {
			
			sql = "UPDATE proxl_xml_file_import_tracking "
					+ " SET marked_for_deletion = " + Database_OneTrueZeroFalse_Constants.DATABASE_FIELD_FALSE
					+ " , last_updated_date_time = NOW(),"
					+ " deleted_by_auth_user_id = NULL, deleted_date_time = NULL "
					+ " WHERE id = ? AND status_id = ?";
		}
		
//		CREATE TABLE `proxl_xml_file_import_tracking` (
//				  `id` int(10) unsigned NOT NULL,
//				  `status_id` tinyint(3) unsigned NOT NULL,
//				  `insert_request_url` varchar(255) COLLATE utf8_bin NOT NULL,
//				  `project_id` int(10) unsigned NOT NULL,
//				  `auth_user_id` int(10) unsigned NOT NULL,
//				  `inserted_search_id` int(10) unsigned DEFAULT NULL,
//				  `upload_date_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
//				  `last_updated_date_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
//				  `search_name` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
//				  `marked_for_deletion` tinyint(3) unsigned NOT NULL DEFAULT '0',
//				  `deleted_by_auth_user_id` int(10) unsigned DEFAULT NULL,
//				  `deleted_date_time` varchar(45) COLLATE utf8_bin DEFAULT NULL,
//		
		try {
			
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.PROXL );
			
			pstmt = conn.prepareStatement( sql );

			int counter = 0;
			

			
			if ( markedForDeletion ) {

				counter++;
				pstmt.setInt( counter, deletedByAuthUserId );
			}

			counter++;
			pstmt.setInt( counter, id );
			counter++;
			pstmt.setInt( counter, status.value() );
			
			
			int rowsUpdated = pstmt.executeUpdate();
			
			if ( rowsUpdated > 0 ) {
				
				recordUpdated = true;
			}
			
		} catch ( Exception e ) {
			
			String msg = "updateMarkedForDeletionForIdStatus(...)  id: " + id + ", sql: " + sql;
			
			log.error( msg, e );
			
			throw e;
			

		} finally {
			
			// be sure database handles are closed

			if( pstmt != null ) {
				try { pstmt.close(); } catch( Throwable t ) { ; }
				pstmt = null;
			}
			
			if( conn != null ) {
				try { conn.close(); } catch( Throwable t ) { ; }
				conn = null;
			}
			
		}
		
		return recordUpdated;
	}
	

}