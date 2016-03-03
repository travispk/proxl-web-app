package org.yeastrc.xlink.www.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.yeastrc.auth.dao.AuthSharedObjectDAO;
import org.yeastrc.auth.dto.AuthSharedObjectDTO;
import org.yeastrc.xlink.dao.SearchDAO;
import org.yeastrc.xlink.dto.SearchDTO;
import org.yeastrc.xlink.www.constants.ConfigSystemsKeysConstants;
import org.yeastrc.xlink.www.constants.StrutsGlobalForwardNames;
import org.yeastrc.xlink.www.constants.WebConstants;
import org.yeastrc.xlink.www.cutoff_processing_web.GetDefaultPsmPeptideCutoffs;
import org.yeastrc.xlink.www.dao.ConfigSystemDAO;
import org.yeastrc.xlink.www.dao.ProjectDAO;
import org.yeastrc.xlink.www.dto.ProjectDTO;
import org.yeastrc.xlink.www.form_query_json_objects.CutoffValuesRootLevel;
import org.yeastrc.xlink.www.forms.MergedSearchViewProteinsForm;
import org.yeastrc.xlink.www.objects.AuthAccessLevel;
import org.yeastrc.xlink.www.searcher.ProjectIdsForSearchIdsSearcher;
import org.yeastrc.xlink.www.user_web_utils.AccessAndSetupWebSessionResult;
import org.yeastrc.xlink.www.user_web_utils.GetAccessAndSetupWebSession;
import org.yeastrc.xlink.www.web_utils.GetPageHeaderData;
import org.yeastrc.xlink.www.web_utils.GetProteinListingTooltipConfigData;
import org.yeastrc.xlink.www.web_utils.GetSearchDetailsData;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handle showing the user a web page for viewing and analyzing how
 * crosslinks, looplinks, and monolinks map to a PDB containing one
 * or more of the proteins found in the experiment.
 * 
 * @author Michael Riffle
 *
 */
public class ViewMergedStructureAction extends Action {
	
	private static final Logger log = Logger.getLogger(ViewMergedStructureAction.class);

	public ActionForward execute( ActionMapping mapping,
			  ActionForm actionForm,
			  HttpServletRequest request,
			  HttpServletResponse response )
					  throws Exception {
		
		
		try {
			

			MergedSearchViewProteinsForm form = (MergedSearchViewProteinsForm) actionForm;
			
		
			/*
			 * HANDLE ALL AUTHENTICATION HERE
			 */
			
			// Get the session first.  
//			HttpSession session = request.getSession();
			


			int[] searchIds = form.getSearchIds();
			
			
			if ( searchIds.length == 0 ) {
				
				return mapping.findForward( StrutsGlobalForwardNames.INVALID_REQUEST_DATA );
			}

			
			//   Get the project id for this search
			
			Collection<Integer> searchIdsCollection = new HashSet<Integer>( );
			
			for ( int searchId : searchIds ) {

				searchIdsCollection.add( searchId );
			}
			
			List<Integer> projectIdsFromSearchIds = ProjectIdsForSearchIdsSearcher.getInstance().getProjectIdsForSearchIds( searchIdsCollection );
			
			if ( projectIdsFromSearchIds.isEmpty() ) {
				
				// should never happen
				
				String msg = "No project ids for search ids: ";
				for ( int searchId : searchIds ) {

					msg += searchId + ", ";
				}
				
				log.error( msg );

				return mapping.findForward( StrutsGlobalForwardNames.INVALID_REQUEST_DATA );
			}
			
			if ( projectIdsFromSearchIds.size() > 1 ) {
				
				//  Invalid request, searches across projects

				return mapping.findForward( StrutsGlobalForwardNames.INVALID_REQUEST_SEARCHES_ACROSS_PROJECTS );
			}
			

			int projectId = projectIdsFromSearchIds.get( 0 );
			
			request.setAttribute( "projectId", projectId ); 
			
			request.setAttribute( "project_id", projectId );
			

			///////////////////////
			
			
			

			AccessAndSetupWebSessionResult accessAndSetupWebSessionResult =
					GetAccessAndSetupWebSession.getInstance().getAccessAndSetupWebSessionWithProjectId( projectId, request, response );

			if ( accessAndSetupWebSessionResult.isNoSession() ) {

				//  No User session 

				return mapping.findForward( StrutsGlobalForwardNames.NO_USER_SESSION );
			}
			
			//  Test access to the project id
			
			AuthAccessLevel authAccessLevel = accessAndSetupWebSessionResult.getAuthAccessLevel();

			if ( ! authAccessLevel.isPublicAccessCodeReadAllowed() ) {

				//  No Access Allowed for this project id

				return mapping.findForward( StrutsGlobalForwardNames.INSUFFICIENT_ACCESS_PRIVILEGE );
			}
			


			request.setAttribute( WebConstants.REQUEST_AUTH_ACCESS_LEVEL, authAccessLevel );

			
			request.setAttribute( "searchIds", searchIds );
			
			if ( searchIds.length == 1 ) {
				int onlySingleSearchId = searchIds[ 0 ];
				request.setAttribute( "onlySingleSearchId", onlySingleSearchId );	
			}
			
			List<SearchDTO> searches = new ArrayList<SearchDTO>();
			for( int searchId : form.getSearchIds() ) {
				
				SearchDTO search = SearchDAO.getInstance().getSearch( searchId );
				
				if ( search == null ) {
					
					String msg = "Percolator search id '" + searchId + "' not found in the database. User taken to home page.";
					
					log.warn( msg );
					
					//  Search not found, the data on the page they are requesting does not exist.
					//  The data on the user's previous page no longer reflects what is in the database.
					//  Take the user to the home page
					
					return mapping.findForward( StrutsGlobalForwardNames.HOME );  //  EARLY EXIT from Method
				}
				
				searches.add( search );
			}



			// sort our searches by ID
			Collections.sort( searches, new Comparator<SearchDTO>() {
				public int compare( SearchDTO r1, SearchDTO r2 ) {
					return r1.getId() - r2.getId();
				}
			});
			
			

			ProjectDAO projectDAO = ProjectDAO.getInstance();


			ProjectDTO projectDTO = projectDAO.getProjectDTOForProjectId( projectId );

			if ( projectDTO == null ) {

				return mapping.findForward( StrutsGlobalForwardNames.PROJECT_NOT_FOUND );
			}

			
			
			request.setAttribute( WebConstants.REQUEST_AUTH_ACCESS_LEVEL, authAccessLevel );



			AuthSharedObjectDTO authSharedObjectDTO = AuthSharedObjectDAO.getInstance().getAuthSharedObjectDTOForSharedObjectId( projectDTO.getAuthShareableObjectId() );

			if ( authSharedObjectDTO == null ) {

				return mapping.findForward( StrutsGlobalForwardNames.PROJECT_NOT_FOUND );
			}

			
			///    Done Processing Auth Check and Auth Level

			
			//////////////////////////////


			//  Jackson JSON Mapper object for JSON deserialization and serialization
			
			ObjectMapper jacksonJSON_Mapper = new ObjectMapper();  //  Jackson JSON library object


			

			GetPageHeaderData.getInstance().getPageHeaderDataWithProjectId( projectId, request );
	
			//  Populate request objects for Protein Name Tooltip JS
			
			GetProteinListingTooltipConfigData.getInstance().getProteinListingTooltipConfigData( request );


			

			String annotation_data_webservice_base_url = 
					ConfigSystemDAO.getInstance().getConfigValueForConfigKey( ConfigSystemsKeysConstants.PROTEIN_ANNOTATION_WEBSERVICE_URL_KEY );
			
//			if ( annotation_data_webservice_base_url == null ) {
//				
//				String msg = "No System configuration found for key: " + ConfigSystemsKeysConstants.PROTEIN_ANNOTATION_WEBSERVICE_URL_KEY;
//				log.error( msg );
//				throw new Exception( msg );
//			}
			
			request.setAttribute( "annotation_data_webservice_base_url", annotation_data_webservice_base_url );
			
			

			/////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////
			
			////////   Generic Param processing
			
			
			CutoffValuesRootLevel cutoffValuesRootLevelCutoffDefaults = 
					GetDefaultPsmPeptideCutoffs.getInstance()
					.getDefaultPsmPeptideCutoffs( searchIdsCollection );


			String cutoffValuesRootLevelCutoffDefaultsJSONString = jacksonJSON_Mapper.writeValueAsString( cutoffValuesRootLevelCutoffDefaults );

			request.setAttribute( "cutoffValuesRootLevelCutoffDefaults", cutoffValuesRootLevelCutoffDefaultsJSONString );


			//  Populate request objects for Standard Search Display
						
			GetSearchDetailsData.getInstance().getSearchDetailsData( searches, request );

		
		} catch ( Exception e ) {

			String msg = "Exception caught: " + e.toString();

			log.error( msg, e );

			throw e;
		}
		
		return mapping.findForward( "Success" );
	}	

}
