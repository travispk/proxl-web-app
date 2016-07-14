package org.yeastrc.xlink.www.searcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.yeastrc.xlink.db.DBConnectionFactory;
import org.yeastrc.xlink.www.objects.SearchProtein;

public class SearchUtils {

	private static final Logger log = Logger.getLogger(SearchUtils.class);
			

	private static final String NAME_SQL =
			"SELECT DISTINCT  annotation.name "
					+ " FROM annotation "
					+ " INNER JOIN search_protein_sequence_annotation AS spsa "
					+ " ON annotation.id = spsa.annotation_id "
			+ " WHERE spsa.search_id = ? AND spsa.protein_sequence_id = ? ";
	
	/**
	 * Get the name for this protein in the context of its search 
	 * @param prprotein
	 * @return
	 * @throws Exception
	 */
	public static String getProteinNameForSearch( SearchProtein prprotein ) throws Exception {
		
		String result = null;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		

//		String sql = "SELECT GROUP_CONCAT(accessionString) FROM tblProteinDatabase WHERE proteinID = ? AND databaseID = ? GROUP BY proteinID";
		

		String sql = NAME_SQL;
		
		try {
			
			int searchId = prprotein.getSearch().getId();
			int proteinSequenceId = prprotein.getProteinSequenceObject().getProteinSequenceId();
			
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.PROXL );
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setInt( 1, searchId );
			pstmt.setInt( 2, proteinSequenceId );
			
			rs = pstmt.executeQuery();
			
			StringBuilder resultsSB = new StringBuilder( 1000 );

			while( rs.next() ) {
				
				if ( resultsSB.length() > 0 ) {
					
					resultsSB.append( "," );
				}
				
				resultsSB.append( rs.getString( "name" ) );
			}
			
			if ( resultsSB.length() > 0 ) {
				
				result = resultsSB.toString();
			}
			
		} catch ( Exception e ) {
			
			log.error( "ERROR getting protein name for search.  sql: " + sql, e );
			
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
	

	private static final String DESCRIPTION_SQL =
			"SELECT DISTINCT  annotation.description "
					+ " FROM annotation "
					+ " INNER JOIN search_protein_sequence_annotation AS spsa "
					+ " ON annotation.id = spsa.annotation_id "
			+ " WHERE spsa.search_id = ? AND spsa.protein_sequence_id = ? ";
	
	/**
	 * Get the description for this protein in the context of its search (based on
	 * the FASTA file used to do the search.
	 * @param prprotein
	 * @return
	 * @throws Exception
	 */
	public static String getProteinDescriptionForSearch( SearchProtein prprotein ) throws Exception {

		String result = null;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		

//		String sql = "SELECT description FROM tblProteinDatabase WHERE proteinID = ? AND databaseID = ? AND description IS NOT NULL AND description <> ''";
		

		String sql = DESCRIPTION_SQL;
		
		try {
			
			int searchId = prprotein.getSearch().getId();
			int proteinSequenceId = prprotein.getProteinSequenceObject().getProteinSequenceId();
			
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.PROXL );
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setInt( 1, searchId );
			pstmt.setInt( 2, proteinSequenceId );
			
			rs = pstmt.executeQuery();
			
			StringBuilder resultsSB = new StringBuilder( 1000 );

			while( rs.next() ) {
				
				if ( resultsSB.length() > 0 ) {
					
					resultsSB.append( ", " );
				}
				
				resultsSB.append( rs.getString( "name" ) );
			}
			
			if ( resultsSB.length() > 0 ) {
				
				result = resultsSB.toString();
			}

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
		
		if( result == null || result.equals( "" ) )
			result = "No description in FASTA.";
		
		return result;		
	}
	
}
