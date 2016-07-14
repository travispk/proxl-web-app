package org.yeastrc.xlink.www.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.yeastrc.xlink.db.DBConnectionFactory;
import org.yeastrc.xlink.www.dto.PeptideDTO;

public class PeptideDAO {
	
	private static final Logger log = Logger.getLogger(PeptideDAO.class);

	private PeptideDAO() { }
	public static PeptideDAO getInstance() { return new PeptideDAO(); }
	
	public PeptideDTO getPeptideDTOFromDatabase( int id ) throws Exception {
		
		
		PeptideDTO peptide = new PeptideDTO();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT sequence FROM peptide WHERE id = ?";
		
		try {
						
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.PROXL );
			
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setInt( 1, id );
			
			rs = pstmt.executeQuery();

			if( !rs.next() )
				throw new Exception( "could not find peptide with id " + id );
			
			peptide.setId( id );
			peptide.setSequence( rs.getString( 1 ) );
			
		} catch ( Exception e ) {
			
			log.error( "ERROR: database connection: '" + DBConnectionFactory.PROXL + "' sql: " + sql, e );
			
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
		

		return peptide;
	}
	
}
