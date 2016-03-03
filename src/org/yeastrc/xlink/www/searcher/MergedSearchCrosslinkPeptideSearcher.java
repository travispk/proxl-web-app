package org.yeastrc.xlink.www.searcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.xlink.dao.CrosslinkDAO;
import org.yeastrc.xlink.dao.PsmDAO;
import org.yeastrc.xlink.dao.ReportedPeptideDAO;
import org.yeastrc.xlink.db.DBConnectionFactory;
import org.yeastrc.xlink.dto.CrosslinkDTO;
import org.yeastrc.xlink.dto.PsmDTO;
import org.yeastrc.xlink.dto.ReportedPeptideDTO;
import org.yeastrc.xlink.dto.SearchDTO;
import org.yeastrc.xlink.dto.AnnotationTypeDTO;
import org.yeastrc.xlink.enum_classes.FilterDirectionType;
import org.yeastrc.xlink.enum_classes.Yes_No__NOT_APPLICABLE_Enum;
import org.yeastrc.xlink.searcher_constants.SearcherGeneralConstants;
import org.yeastrc.xlink.searcher_psm_peptide_cutoff_objects.SearcherCutoffValuesAnnotationLevel;
import org.yeastrc.xlink.searcher_psm_peptide_cutoff_objects.SearcherCutoffValuesRootLevel;
import org.yeastrc.xlink.searcher_psm_peptide_cutoff_objects.SearcherCutoffValuesSearchLevel;
import org.yeastrc.xlink.www.objects.MergedSearchProteinCrosslink;



/**
 * 
 *
 */
public class MergedSearchCrosslinkPeptideSearcher {
	
	private static final Logger log = Logger.getLogger(MergedSearchCrosslinkPeptideSearcher.class);
			

	private MergedSearchCrosslinkPeptideSearcher() { }
	public static MergedSearchCrosslinkPeptideSearcher getInstance() { return new MergedSearchCrosslinkPeptideSearcher(); }

	private final String SQL_LINKED_PEPTIDES_FIRST_PART = 

			"SELECT DISTINCT reported_peptide_id " 

		+ "FROM ( ";

	private final String SQL_LINKED_PEPTIDES_LAST_PART = 
			
		  " ) AS subquery_result  ";
		

	private final String SQL_LINKED_PEPTIDES_EACH_UNION_FIRST_PART_PART = 

			"SELECT unified_rp__rep_pept__search__generic_lookup.reported_peptide_id " 

		+ "FROM unified_rp__rep_pept__search__generic_lookup  "
		+ "INNER JOIN crosslink ON unified_rp__rep_pept__search__generic_lookup.sample_psm_id = crosslink.psm_id ";



	private final String SQL_LINKED_PEPTIDES_SUB_PER_UNION_WHERE_START = 

			" WHERE unified_rp__rep_pept__search__generic_lookup.search_id = ? "
					+ " AND crosslink.nrseq_id_1 = ? AND crosslink.nrseq_id_2 = ? "
					+ " AND crosslink.protein_1_position = ? AND crosslink.protein_2_position = ? ";

	
	
	/**
	 * @param crosslink
	 * @return
	 * @throws Exception
	 */
	public List<ReportedPeptideDTO> getLinkedPeptides( MergedSearchProteinCrosslink crosslink ) throws Exception {
		
		List<ReportedPeptideDTO> reportedPeptides = new ArrayList<ReportedPeptideDTO>();
		
		
		Collection<SearchDTO> searchesParam = crosslink.getSearches();
		
		SearcherCutoffValuesRootLevel searcherCutoffValuesRootLevel = crosslink.getSearcherCutoffValuesRootLevel();
		
		
		
		List<SearchDTO> searches = new ArrayList<>( searchesParam );
		
		Collections.sort( searches ); //  ensure in id order
		
		
		
		
		//  Copy cutoff values to lists (need to guarantee order since process same objects in multiple places)

		List<CutoffsPerSearchHolder> cutoffsPerSearchHolderList = new ArrayList<>( searches.size() );
		
		//  Process cutoffs per search
		
		for ( SearchDTO searchDTO : searches ) {
			
			int searchId = searchDTO.getId();
		
			SearcherCutoffValuesSearchLevel searcherCutoffValuesSearchLevel = searcherCutoffValuesRootLevel.getPerSearchCutoffs( searchId );
			
			if ( searcherCutoffValuesSearchLevel == null ) {
				
				String msg = "Unable to get cutoffs for search id: " + searchId;
				log.error( msg );
				throw new Exception(msg);
			}


			List<SearcherCutoffValuesAnnotationLevel> peptideCutoffValuesPerSearchList = 
					searcherCutoffValuesSearchLevel.getPeptidePerAnnotationCutoffsList();
			
			List<SearcherCutoffValuesAnnotationLevel> psmCutoffValuesPerSearchList = 
					searcherCutoffValuesSearchLevel.getPsmPerAnnotationCutoffsList();


			//  All cutoffs for search id are default?
			

			Yes_No__NOT_APPLICABLE_Enum   defaultPeptideCutoffs = Yes_No__NOT_APPLICABLE_Enum.NOT_APPLICABLE;
				
			boolean onlyDefaultPsmCutoffs = true;
			
			for ( SearcherCutoffValuesAnnotationLevel entry  : psmCutoffValuesPerSearchList ) {

				if ( ! entry.annotationValueMatchesDefault() ) {
					
					onlyDefaultPsmCutoffs = false;
					break;
				}
			}
			

			//   Check if any Peptide Cutoffs are default filters
			
			for ( SearcherCutoffValuesAnnotationLevel item : peptideCutoffValuesPerSearchList ) {

				if ( item.getAnnotationTypeDTO().getAnnotationTypeFilterableDTO() == null ) {
					
					String msg = "ERROR: Annotation type data must contain Filterable DTO data.  Annotation type id: " + item.getAnnotationTypeDTO().getId();
					log.error( msg );
					throw new Exception(msg);
				}
				
				if ( item.getAnnotationTypeDTO().getAnnotationTypeFilterableDTO().isDefaultFilter() ) {
					
					defaultPeptideCutoffs = Yes_No__NOT_APPLICABLE_Enum.YES;
					break;
				}
			}
			
			
			//   Check if all Peptide Cutoffs are default values
			
			for ( SearcherCutoffValuesAnnotationLevel item : peptideCutoffValuesPerSearchList ) {
				
				if ( ! item.annotationValueMatchesDefault() ) {
					
					defaultPeptideCutoffs = Yes_No__NOT_APPLICABLE_Enum.NO;
					break;
				}
			}

			
			CutoffsPerSearchHolder cutoffsPerSearchHolder = new CutoffsPerSearchHolder();
			
			cutoffsPerSearchHolder.searchId = searchId;
			
			cutoffsPerSearchHolder.peptideCutoffValuesList = peptideCutoffValuesPerSearchList;
			cutoffsPerSearchHolder.psmCutoffValuesList = psmCutoffValuesPerSearchList;
			
			cutoffsPerSearchHolder.onlyDefaultPsmCutoffs = onlyDefaultPsmCutoffs;
			
			cutoffsPerSearchHolder.defaultPeptideCutoffs = defaultPeptideCutoffs;
			
			cutoffsPerSearchHolderList.add( cutoffsPerSearchHolder );
		}

		////////////
		
		//  All cutoffs are default?

		boolean onlyDefaultPsmCutoffsAllSearches = true;
		
		Yes_No__NOT_APPLICABLE_Enum defaultPeptideCutoffsAllSearches = Yes_No__NOT_APPLICABLE_Enum.NOT_APPLICABLE;
		
		


		//   Check if all Psm Cutoffs are default values
		
		for ( CutoffsPerSearchHolder item : cutoffsPerSearchHolderList ) {
			
			if ( ! item.onlyDefaultPsmCutoffs ) {
				
				onlyDefaultPsmCutoffsAllSearches = false;
				break;
			}
		}
		



		//   Check if any Peptide Cutoffs are default values
		
		for ( CutoffsPerSearchHolder item : cutoffsPerSearchHolderList ) {
			
			if ( item.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.YES ) {
				
				defaultPeptideCutoffsAllSearches = Yes_No__NOT_APPLICABLE_Enum.YES;
				break;
			}
		}
		

		//   Check if any Peptide Cutoffs are NOT default values
		
		for ( CutoffsPerSearchHolder item : cutoffsPerSearchHolderList ) {
			
			if ( item.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.NO ) {
				
				defaultPeptideCutoffsAllSearches = Yes_No__NOT_APPLICABLE_Enum.NO;
				break;
			}
		}
		

		String sql = getSQL( 
				cutoffsPerSearchHolderList, 
				SQL_LINKED_PEPTIDES_FIRST_PART,
				SQL_LINKED_PEPTIDES_EACH_UNION_FIRST_PART_PART, 
				SQL_LINKED_PEPTIDES_SUB_PER_UNION_WHERE_START, 
				SQL_LINKED_PEPTIDES_LAST_PART );
		

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		
		try {
						
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.CROSSLINKS );
			
			pstmt = conn.prepareStatement( sql );
			
			setPstmtQueryParameters( crosslink, cutoffsPerSearchHolderList, pstmt );
			
			rs = pstmt.executeQuery();

			while( rs.next() ) {
				reportedPeptides.add(ReportedPeptideDAO.getInstance().getReportedPeptideFromDatabase( rs.getInt( 1 ) ) );
			}

		} catch ( Exception e ) {
			
			String msg = "Exception in getLinkedPeptides( Collection<SearchDTO> searches, ... ), sql: " + sql;
			
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
		
		return reportedPeptides;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	
	private final String SQL_NUM_LINKED_PEPTIDES_FIRST_PART = 

			"SELECT COUNT(DISTINCT reported_peptide_id) " 

		+ "FROM ( ";

	private final String SQL_NUM_LINKED_PEPTIDES_LAST_PART = 
			
		  " ) AS subquery_result  ";
		

	private final String SQL_NUM_LINKED_PEPTIDES_EACH_UNION_FIRST_PART_PART = 

			"SELECT unified_rp__rep_pept__search__generic_lookup.reported_peptide_id " 

		+ "FROM unified_rp__rep_pept__search__generic_lookup  "
		+ "INNER JOIN crosslink ON unified_rp__rep_pept__search__generic_lookup.sample_psm_id = crosslink.psm_id ";



	private final String SQL_NUM_LINKED_PEPTIDES_SUB_PER_UNION_WHERE_START = 

			" WHERE unified_rp__rep_pept__search__generic_lookup.search_id = ? "
					+ " AND crosslink.nrseq_id_1 = ? AND crosslink.nrseq_id_2 = ? "
					+ " AND crosslink.protein_1_position = ? AND crosslink.protein_2_position = ? ";

	
	/**
	 * Get the number of distinct peptides (that is, distinct pair of crosslinked peptides) found that identified the given crosslinked proteins/positions
	 * @param crosslink
	 * @return
	 * @throws Exception
	 */
	public int getNumLinkedPeptides( MergedSearchProteinCrosslink crosslink ) throws Exception {
		
		int count = 0;
		
		Collection<SearchDTO> searchesParam = crosslink.getSearches();
		
		SearcherCutoffValuesRootLevel searcherCutoffValuesRootLevel = crosslink.getSearcherCutoffValuesRootLevel();
		
		
		
		List<SearchDTO> searches = new ArrayList<>( searchesParam );
		
		Collections.sort( searches ); //  ensure in id order
		
		
		//  Copy cutoff values to lists (need to guarantee order since process same objects in multiple places)

		List<CutoffsPerSearchHolder> cutoffsPerSearchHolderList = new ArrayList<>( searches.size() );
		
		//  Process cutoffs per search
		
		for ( SearchDTO searchDTO : searches ) {
			
			int searchId = searchDTO.getId();
		
			SearcherCutoffValuesSearchLevel searcherCutoffValuesSearchLevel = searcherCutoffValuesRootLevel.getPerSearchCutoffs( searchId );
			
			if ( searcherCutoffValuesSearchLevel == null ) {
				
				String msg = "Unable to get cutoffs for search id: " + searchId;
				log.error( msg );
				throw new Exception(msg);
			}


			List<SearcherCutoffValuesAnnotationLevel> peptideCutoffValuesPerSearchList = new ArrayList<>( searcherCutoffValuesSearchLevel.getPeptidePerAnnotationCutoffsList().size() );
			List<SearcherCutoffValuesAnnotationLevel> psmCutoffValuesPerSearchList = new ArrayList<>( searcherCutoffValuesSearchLevel.getPsmPerAnnotationCutoffsList().size() );


			//  All cutoffs for search id are default?
			

			Yes_No__NOT_APPLICABLE_Enum   defaultPeptideCutoffs = Yes_No__NOT_APPLICABLE_Enum.NOT_APPLICABLE;
				
			boolean onlyDefaultPsmCutoffs = true;
			

			
			for ( SearcherCutoffValuesAnnotationLevel entry  : searcherCutoffValuesSearchLevel.getPeptidePerAnnotationCutoffsList() ) {

				peptideCutoffValuesPerSearchList.add( entry );
			}


			for ( SearcherCutoffValuesAnnotationLevel entry  : searcherCutoffValuesSearchLevel.getPsmPerAnnotationCutoffsList() ) {

				psmCutoffValuesPerSearchList.add( entry );
				
				if ( ! entry.annotationValueMatchesDefault() ) {
					
					onlyDefaultPsmCutoffs = false;
					break;
				}
			}
			

			//   Check if any Peptide Cutoffs are default filters
			
			for ( SearcherCutoffValuesAnnotationLevel item : peptideCutoffValuesPerSearchList ) {
				
				if ( item.getAnnotationTypeDTO().getAnnotationTypeFilterableDTO().isDefaultFilter() ) {
					
					defaultPeptideCutoffs = Yes_No__NOT_APPLICABLE_Enum.YES;
					break;
				}
			}
			
			
			//   Check if all Peptide Cutoffs are default values
			
			for ( SearcherCutoffValuesAnnotationLevel item : peptideCutoffValuesPerSearchList ) {
				
				if ( ! item.annotationValueMatchesDefault() ) {
					
					defaultPeptideCutoffs = Yes_No__NOT_APPLICABLE_Enum.NO;
					break;
				}
			}

			
			CutoffsPerSearchHolder cutoffsPerSearchHolder = new CutoffsPerSearchHolder();
			
			cutoffsPerSearchHolder.searchId = searchId;
			
			cutoffsPerSearchHolder.peptideCutoffValuesList = peptideCutoffValuesPerSearchList;
			cutoffsPerSearchHolder.psmCutoffValuesList = psmCutoffValuesPerSearchList;
			
			cutoffsPerSearchHolder.onlyDefaultPsmCutoffs = onlyDefaultPsmCutoffs;
			
			cutoffsPerSearchHolder.defaultPeptideCutoffs = defaultPeptideCutoffs;
			
			cutoffsPerSearchHolderList.add( cutoffsPerSearchHolder );
		}

		////////////
		
		//  All cutoffs are default?

		boolean onlyDefaultPsmCutoffsAllSearches = true;
		
		Yes_No__NOT_APPLICABLE_Enum defaultPeptideCutoffsAllSearches = Yes_No__NOT_APPLICABLE_Enum.NOT_APPLICABLE;
		
		


		//   Check if all Psm Cutoffs are default values
		
		for ( CutoffsPerSearchHolder item : cutoffsPerSearchHolderList ) {
			
			if ( ! item.onlyDefaultPsmCutoffs ) {
				
				onlyDefaultPsmCutoffsAllSearches = false;
				break;
			}
		}
		



		//   Check if any Peptide Cutoffs are default values
		
		for ( CutoffsPerSearchHolder item : cutoffsPerSearchHolderList ) {
			
			if ( item.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.YES ) {
				
				defaultPeptideCutoffsAllSearches = Yes_No__NOT_APPLICABLE_Enum.YES;
				break;
			}
		}
		

		//   Check if any Peptide Cutoffs are NOT default values
		
		for ( CutoffsPerSearchHolder item : cutoffsPerSearchHolderList ) {
			
			if ( item.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.NO ) {
				
				defaultPeptideCutoffsAllSearches = Yes_No__NOT_APPLICABLE_Enum.NO;
				break;
			}
		}
		
		
		String sql = getSQL( 
				cutoffsPerSearchHolderList, 
				SQL_NUM_LINKED_PEPTIDES_FIRST_PART,
				SQL_NUM_LINKED_PEPTIDES_EACH_UNION_FIRST_PART_PART, 
				SQL_NUM_LINKED_PEPTIDES_SUB_PER_UNION_WHERE_START, 
				SQL_NUM_LINKED_PEPTIDES_LAST_PART );
		
		
		
		//////////////////////////////////
		
		
		
		
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		
		try {
						
			conn = DBConnectionFactory.getConnection( DBConnectionFactory.CROSSLINKS );
			
			pstmt = conn.prepareStatement( sql );
			
			setPstmtQueryParameters( crosslink, cutoffsPerSearchHolderList, pstmt );

			rs = pstmt.executeQuery();
			if( rs.next() )
				count = rs.getInt( 1 );

		} catch ( Exception e ) {
			
			String msg = "Exception in getNumLinkedPeptides( Collection<SearchDTO> searches, ... ), sql: " + sql;
			
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
		
		
		return count;
	}

	
	
	
	/**
	 * @param cutoffsPerSearchHolderList
	 * @param sqlFirstPart
	 * @param sqlEachUnionFirstPart
	 * @param sqlEachUnionWhereStart
	 * @param sqlLastPart
	 * @return
	 */
	private String getSQL(
			List<CutoffsPerSearchHolder> cutoffsPerSearchHolderList,
			String sqlFirstPart, String sqlEachUnionFirstPart,
			String sqlEachUnionWhereStart, String sqlLastPart) {
		
		
		//////////////////////
		
		/////   Start building the SQL
		
		

		
		StringBuilder sqlSB = new StringBuilder( 1000 );
		
		


		sqlSB.append( sqlFirstPart );
		
		boolean firstCutoffsPerSearchHolder = true;
		

		for ( CutoffsPerSearchHolder cutoffsPerSearchHolder : cutoffsPerSearchHolderList ) {

			if ( firstCutoffsPerSearchHolder ) {
				
				firstCutoffsPerSearchHolder = false;
			} else {
				
				sqlSB.append( " UNION " );
			}
			
			sqlSB.append( sqlEachUnionFirstPart );
			

			{



				if ( ! cutoffsPerSearchHolder.onlyDefaultPsmCutoffs ) {


					//  Non-Default PSM cutoffs so have to query on the cutoffs

					//  Add inner join for each PSM cutoff


					int counter = 0;
					
					for ( int index = 1; index <= cutoffsPerSearchHolder.psmCutoffValuesList.size(); index++ ) {


						counter++;

						sqlSB.append( " INNER JOIN " );


						sqlSB.append( " unified_rp__rep_pept__search__best_psm_value_generic_lookup AS best_psm_value_tbl_" );
						sqlSB.append( Integer.toString( counter ) );

						sqlSB.append( " ON "  );

						
						sqlSB.append( " unified_rp__rep_pept__search__generic_lookup.search_id = "  );

						sqlSB.append( "best_psm_value_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".search_id" );

						sqlSB.append( " AND " );

						sqlSB.append( " unified_rp__rep_pept__search__generic_lookup.reported_peptide_id = "  );

						sqlSB.append( "best_psm_value_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".reported_peptide_id" );
					}
				}

			}

			{
				if ( cutoffsPerSearchHolder.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.NO ) {

					//  Non-Default Peptide cutoffs so have to query on the cutoffs


					//  Add inner join for each Peptide cutoff

					int counter = 0;


					for ( int index = 1; index <= cutoffsPerSearchHolder.peptideCutoffValuesList.size(); index++ ) {

						counter++;

						sqlSB.append( " INNER JOIN " );

						sqlSB.append( " srch__rep_pept__annotation AS srch__rep_pept_fltrbl_tbl_" );
						sqlSB.append( Integer.toString( counter ) );

						sqlSB.append( " ON "  );

						
						sqlSB.append( " unified_rp__rep_pept__search__generic_lookup.search_id = "  );

						sqlSB.append( "srch__rep_pept_fltrbl_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".search_id" );

						sqlSB.append( " AND " );

						sqlSB.append( " unified_rp__rep_pept__search__generic_lookup.reported_peptide_id = "  );

						sqlSB.append( "srch__rep_pept_fltrbl_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".reported_peptide_id" );

					}
				}
			}
		

			//////////

			sqlSB.append( sqlEachUnionWhereStart );

			//////////


			// Process PSM Cutoffs for WHERE

			{


				if ( cutoffsPerSearchHolder.onlyDefaultPsmCutoffs ) {

					//   Only Default PSM Cutoffs chosen so criteria simply the Peptides where the PSM count for the default cutoffs is > zero


					sqlSB.append( " AND " );


					sqlSB.append( " unified_rp__rep_pept__search__generic_lookup.psm_num_at_default_cutoff > 0 " );


				} else {


					//  Non-Default PSM cutoffs so have to query on the cutoffs

					int counter = 0; 

					for ( SearcherCutoffValuesAnnotationLevel searcherCutoffValuesPsmAnnotationLevel : cutoffsPerSearchHolder.psmCutoffValuesList ) {


						AnnotationTypeDTO srchPgmFilterablePsmAnnotationTypeDTO = searcherCutoffValuesPsmAnnotationLevel.getAnnotationTypeDTO();

						counter++;

						sqlSB.append( " AND " );

						sqlSB.append( " ( " );


						sqlSB.append( "best_psm_value_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".search_id = ? AND " );

						sqlSB.append( "best_psm_value_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".annotation_type_id = ? AND " );

						sqlSB.append( "best_psm_value_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".best_psm_value_for_ann_type_id " );

						if ( srchPgmFilterablePsmAnnotationTypeDTO.getAnnotationTypeFilterableDTO().getFilterDirectionType() 
								== FilterDirectionType.ABOVE ) {

							sqlSB.append( SearcherGeneralConstants.SQL_END_BIGGER_VALUE_BETTER );

						} else {

							sqlSB.append( SearcherGeneralConstants.SQL_END_SMALLER_VALUE_BETTER );

						}

						sqlSB.append( " ? " );

						sqlSB.append( " ) " );
					}
				}
			}

			//  Process Peptide Cutoffs for WHERE

			{
				

				if ( cutoffsPerSearchHolder.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.NOT_APPLICABLE ) {

					//  No WHERE criteria for defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.NOT_APPLICABLE
					
					//     There are no Peptide cutoffs to apply
					
					
				
				} else if ( cutoffsPerSearchHolder.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.YES ) {

					//   Only Default Peptide Cutoffs chosen so criteria simply the Peptides where the defaultPeptideCutoffs is yes

					sqlSB.append( " AND " );

					sqlSB.append( " unified_rp__rep_pept__search__generic_lookup.peptide_meets_default_cutoffs = '" );
					sqlSB.append( Yes_No__NOT_APPLICABLE_Enum.YES.value() );
					sqlSB.append( "' " );

					
				} else if ( cutoffsPerSearchHolder.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.NO ) {

					
					//  Non-Default Peptide cutoffs so have to query on the cutoffs

					int counter = 0; 

					for ( SearcherCutoffValuesAnnotationLevel searcherCutoffValuesReportedPeptideAnnotationLevel : cutoffsPerSearchHolder.peptideCutoffValuesList ) {

						AnnotationTypeDTO srchPgmFilterableReportedPeptideAnnotationTypeDTO = searcherCutoffValuesReportedPeptideAnnotationLevel.getAnnotationTypeDTO();

						counter++;

						sqlSB.append( " AND " );

						sqlSB.append( " ( " );


						sqlSB.append( "srch__rep_pept_fltrbl_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".search_id = ? AND " );

						sqlSB.append( "srch__rep_pept_fltrbl_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".annotation_type_id = ? AND " );

						sqlSB.append( "srch__rep_pept_fltrbl_tbl_" );
						sqlSB.append( Integer.toString( counter ) );
						sqlSB.append( ".value_double " );

						if ( srchPgmFilterableReportedPeptideAnnotationTypeDTO.getAnnotationTypeFilterableDTO().getFilterDirectionType() 
								== FilterDirectionType.ABOVE ) {

							sqlSB.append( SearcherGeneralConstants.SQL_END_BIGGER_VALUE_BETTER );

						} else {

							sqlSB.append( SearcherGeneralConstants.SQL_END_SMALLER_VALUE_BETTER );

						}

						sqlSB.append( "? " );

						sqlSB.append( " ) " );
					}
				}
			}
		}
		
		sqlSB.append( sqlLastPart );
		
		
		
		
		String sql = sqlSB.toString();
		return sql;
	}
	
	
	/**
	 * @param crosslink
	 * @param cutoffsPerSearchHolderList
	 * @param pstmt
	 * @throws SQLException
	 */
	private void setPstmtQueryParameters(MergedSearchProteinCrosslink crosslink,
			List<CutoffsPerSearchHolder> cutoffsPerSearchHolderList,
			PreparedStatement pstmt) throws SQLException {
		
		
		
		int paramCounter = 0;
		


		for ( CutoffsPerSearchHolder cutoffsPerSearchHolder : cutoffsPerSearchHolderList ) {

			paramCounter++;
			pstmt.setInt( paramCounter, cutoffsPerSearchHolder.searchId );
			
			paramCounter++;
			pstmt.setInt( paramCounter, crosslink.getProtein1().getNrProtein().getNrseqId() );
			paramCounter++;
			pstmt.setInt( paramCounter, crosslink.getProtein2().getNrProtein().getNrseqId() );
			paramCounter++;
			pstmt.setInt( paramCounter, crosslink.getProtein1Position() );
			paramCounter++;
			pstmt.setInt( paramCounter, crosslink.getProtein2Position() );

			// Process PSM Cutoffs for WHERE


			{

				if ( ! cutoffsPerSearchHolder.onlyDefaultPsmCutoffs ) {

					//  PSM Cutoffs are not the default 

					for ( SearcherCutoffValuesAnnotationLevel searcherCutoffValuesPsmAnnotationLevel : cutoffsPerSearchHolder.psmCutoffValuesList ) {

						AnnotationTypeDTO srchPgmFilterablePsmAnnotationTypeDTO = searcherCutoffValuesPsmAnnotationLevel.getAnnotationTypeDTO();

						paramCounter++;
						pstmt.setInt( paramCounter, cutoffsPerSearchHolder.searchId );

						paramCounter++;
						pstmt.setInt( paramCounter, srchPgmFilterablePsmAnnotationTypeDTO.getId() );

						paramCounter++;
						pstmt.setDouble( paramCounter, searcherCutoffValuesPsmAnnotationLevel.getAnnotationCutoffValue() );
					}

				}
			}




			// Process Peptide Cutoffs for WHERE


			{

				if ( cutoffsPerSearchHolder.defaultPeptideCutoffs == Yes_No__NOT_APPLICABLE_Enum.NO ) {
					
					//  Non-Default Peptide cutoffs so have to query on the cutoffs

					for ( SearcherCutoffValuesAnnotationLevel searcherCutoffValuesReportedPeptideAnnotationLevel : cutoffsPerSearchHolder.peptideCutoffValuesList ) {

						AnnotationTypeDTO srchPgmFilterableReportedPeptideAnnotationTypeDTO = searcherCutoffValuesReportedPeptideAnnotationLevel.getAnnotationTypeDTO();

						paramCounter++;
						pstmt.setInt( paramCounter, cutoffsPerSearchHolder.searchId );

						paramCounter++;
						pstmt.setInt( paramCounter, srchPgmFilterableReportedPeptideAnnotationTypeDTO.getId() );

						paramCounter++;
						pstmt.setDouble( paramCounter, searcherCutoffValuesReportedPeptideAnnotationLevel.getAnnotationCutoffValue() );
					}

				}
			}
		}
	}
	
	
	//////////////////////////////////////////////////////
	
	
	
	/**
	 * Get the number of peptides (pair of peptides) that UNIQUELY identified the pair of proteins+positions represented by this
	 * crosslink in the context of the merged searches
	 * @param crosslink
	 * @return
	 * @throws Exception
	 */
	public int getNumUniqueLinkedPeptides( MergedSearchProteinCrosslink crosslink ) throws Exception {
		
		try {

			int count = 0;


			Collection<SearchDTO> searches = crosslink.getSearches();

			// iterate over each peptide, see which are unique in the contest of the FASTAs represented by
			// this merged search set
			for( ReportedPeptideDTO reportedPeptide : getLinkedPeptides( crosslink ) ) {

				PsmDTO psm = null;

				for ( SearchDTO search : searches ) {

					psm = PsmDAO.getInstance().getOnePsmDTOForSearchIdAndReportedPeptideId( reportedPeptide.getId(), search.getId() );

					if ( psm != null ) {

						break;
					}
				}

				if ( psm == null ) {


					String msg = "Skipping Reported Peptide:  No PSMs found for reportedPeptide.getId(): " + reportedPeptide.getId();

					log.warn( msg );

					continue;
				}


				CrosslinkDTO crosslinkDTO = CrosslinkDAO.getInstance().getCrosslinkDTOByPsmId( psm.getId() );

				if ( crosslinkDTO == null ) {

					String msg = "No Crosslink found for psm.getId(): " + psm.getId();

					log.error( msg );

					throw new Exception( msg );
				}

				Collection<Integer> peptideIds = new ArrayList<>();

				peptideIds.add( crosslinkDTO.getPeptide1Id() );

				if ( crosslinkDTO.getPeptide1Id() != crosslinkDTO.getPeptide2Id() ) {

					peptideIds.add( crosslinkDTO.getPeptide2Id() );
				}


				if( ReportedPeptideSearcher.getInstance().isUnique( reportedPeptide, peptideIds, crosslink.getSearches() ) ) {

					count++;
				}
			}		

			return count;

		} catch ( Exception e ) {
			
			String msg = "Exception in getNumUniqueLinkedPeptides( MergedSearchProteinCrosslink crosslink ): " 
					+ " crosslink.getProtein1().getNrProtein().getNrseqId(): " + crosslink.getProtein1().getNrProtein().getNrseqId()
					+ " crosslink.getProtein2().getNrProtein().getNrseqId(): " + crosslink.getProtein2().getNrProtein().getNrseqId();
			
			log.error( msg, e );
			
			throw e;
		}
	}
	

	


	/**
	 * Internal class for holding the cutoffs for a specific search
	 *
	 */
	private static class CutoffsPerSearchHolder {
		
		int searchId;
		
		List<SearcherCutoffValuesAnnotationLevel> psmCutoffValuesList;
		List<SearcherCutoffValuesAnnotationLevel> peptideCutoffValuesList;
		
		boolean onlyDefaultPsmCutoffs;

		Yes_No__NOT_APPLICABLE_Enum   defaultPeptideCutoffs = Yes_No__NOT_APPLICABLE_Enum.NOT_APPLICABLE;
		
	}
	
}
