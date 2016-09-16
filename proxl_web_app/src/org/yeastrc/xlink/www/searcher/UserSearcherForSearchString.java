package org.yeastrc.xlink.www.searcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.xlink.db.DBConnectionFactory;

/**
 * Return a list of users in the database for a query
 *
 *
 */
public class UserSearcherForSearchString {

	private static final Logger log = Logger.getLogger(UserSearcherForSearchString.class);
	
	private UserSearcherForSearchString() { }
	private static final UserSearcherForSearchString _INSTANCE = new UserSearcherForSearchString();
	public static UserSearcherForSearchString getInstance() { return _INSTANCE; }
	
	
	/**
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getAuthUserIdForQuery( String query ) throws Exception {
		
		String queryForLike = query + "%";
		
		
		List<Integer> userIds = new ArrayList<Integer>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT auth_user.id FROM auth_user"

		 		+ " INNER JOIN xl_user ON auth_user.id = xl_user.auth_user_id "
		 		
				+ " WHERE "
				+ "    auth_user.username LIKE ? OR auth_user.email LIKE ? "
				+ " OR xl_user.first_name LIKE ? OR xl_user.last_name LIKE ? "; 

		
		try {
			
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.PROXL );

			
			pstmt = conn.prepareStatement( sql );
			
			int count = 0;
			
			count++;
			pstmt.setString( count, queryForLike );

			count++;
			pstmt.setString( count, queryForLike );
			
			count++;
			pstmt.setString( count, queryForLike );
			
			count++;
			pstmt.setString( count, queryForLike );
			
			
			rs = pstmt.executeQuery();

			while( rs.next() ) {

				userIds.add( rs.getInt( 1 ) );
			}
			
		} catch ( Exception e ) {
			
			String msg = "getAuthUserIdForQuery(), sql: " + sql;
			
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
		
		
		
		return userIds;
	}
	
	
	
	
	
}