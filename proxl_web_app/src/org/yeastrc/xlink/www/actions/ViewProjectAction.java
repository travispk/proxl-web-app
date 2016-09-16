package org.yeastrc.xlink.www.actions;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.yeastrc.xlink.www.objects.AuthAccessLevel;
import org.yeastrc.xlink.www.objects.ProjectPublicAccessData;
import org.yeastrc.xlink.www.objects.SearchDTODetailsDisplayWrapper;
import org.yeastrc.xlink.www.proxl_xml_file_import.searchers.ProxlXMLFileImportTracking_PendingCount_Searcher;
import org.yeastrc.xlink.www.proxl_xml_file_import.utils.IsProxlXMLFileImportFullyConfigured;
import org.yeastrc.xlink.www.searcher.NoteSearcher;
import org.yeastrc.xlink.www.searcher.SearchSearcher;
import org.yeastrc.xlink.www.constants.StrutsGlobalForwardNames;
import org.yeastrc.xlink.www.constants.WebConstants;
import org.yeastrc.xlink.www.user_account.UserSessionObject;
import org.yeastrc.xlink.www.user_web_utils.AccessAndSetupWebSessionResult;
import org.yeastrc.xlink.www.user_web_utils.GetAccessAndSetupWebSession;
import org.yeastrc.xlink.www.web_utils.AnyPDBFilesForProjectId;
import org.yeastrc.xlink.www.web_utils.GetPageHeaderData;
import org.yeastrc.xlink.www.web_utils.GetProjectPublicAccessData;
import org.yeastrc.xlink.www.constants.AuthAccessLevelConstants;
import org.yeastrc.xlink.www.dao.ProjectDAO;
import org.yeastrc.xlink.dto.NoteDTO;
import org.yeastrc.xlink.www.dto.SearchDTO;
import org.yeastrc.xlink.www.dto.ProjectDTO;


/**
 * 
 * 
 */
public class ViewProjectAction extends Action {
	
	private static final Logger log = Logger.getLogger(ViewProjectAction.class);

	public ActionForward execute( ActionMapping mapping,
			  ActionForm form,
			  HttpServletRequest request,
			  HttpServletResponse response )
					  throws Exception {
		

		try {


			// Get the session first.  
//			HttpSession session = request.getSession();



			//  First try to get project id from request as passed from another struts action
			

			int projectId = 0;
			
			Integer projectIdInteger = (Integer)request.getAttribute( WebConstants.REQUEST_PROJECT_ID );
			
			if ( projectIdInteger != null ) {
				
				projectId = projectIdInteger;
				
			} else {
				
				//  get project id from query string
				
				String projectIdString = request.getParameter( WebConstants.PARAMETER_PROJECT_ID );

				try {
					projectId = Integer.parseInt( projectIdString );

				} catch ( Exception ex ) {


					throw ex;
				}
			
			}

			//  Confirm projectId is in database
			
			Integer authShareableObjectId =	ProjectDAO.getInstance().getAuthShareableObjectIdForProjectId( projectId );
			
			if ( authShareableObjectId == null ) {
				
				// should never happen
				
				String msg = "Project id is not in database: " + projectId;
				
				log.error( msg );

				return mapping.findForward( StrutsGlobalForwardNames.INVALID_REQUEST_DATA );
			}

			AccessAndSetupWebSessionResult accessAndSetupWebSessionResult =
					GetAccessAndSetupWebSession.getInstance().getAccessAndSetupWebSessionWithProjectId( projectId, request, response );
			
			UserSessionObject userSessionObject = accessAndSetupWebSessionResult.getUserSessionObject();
			

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

			
			
			request.setAttribute( "project_id", projectId );
			

			ProjectDAO projectDAO = ProjectDAO.getInstance();
			
			
			ProjectDTO projectDTO = projectDAO.getProjectDTOForProjectId( projectId );
			
			if ( projectDTO == null || ( ! projectDTO.isEnabled() ) || ( projectDTO.isMarkedForDeletion() )  ) {
				
				
				GetPageHeaderData.getInstance().getPageHeaderDataWithoutProjectId(request);

				return mapping.findForward( StrutsGlobalForwardNames.PROJECT_NOT_FOUND );
			}
			
			
			ProjectPublicAccessData projectPublicAccessData =
					GetProjectPublicAccessData.getInstance().getProjectPublicAccessData( projectId );
			
			if ( projectPublicAccessData == null ) {
				
				
				GetPageHeaderData.getInstance().getPageHeaderDataWithoutProjectId(request);

				return mapping.findForward( StrutsGlobalForwardNames.PROJECT_NOT_FOUND );
			}
			
			
			request.setAttribute( WebConstants.REQUEST_AUTH_ACCESS_LEVEL, authAccessLevel );

			
			GetPageHeaderData.getInstance().getPageHeaderDataWithProjectId( projectId, request );
			
			
			boolean showStructureLink = true;
			
			if ( authAccessLevel.isAssistantProjectOwnerAllowed()
					|| authAccessLevel.isAssistantProjectOwnerIfProjectNotLockedAllowed() ) {
				
				
			} else {
				
				//  Public access user:
				
				showStructureLink = AnyPDBFilesForProjectId.getInstance().anyPDBFilesForProjectId( projectId );
			}
			
			
			
			request.setAttribute( WebConstants.REQUEST_SHOW_STRUCTURE_LINK, showStructureLink );
			

			
			List<NoteDTO> notes = NoteSearcher.getInstance().getSearchsForProjectId( projectId );

			List<SearchDTO> searches = SearchSearcher.getInstance().getSearchsForProjectId( projectId );


			List<SearchDTODetailsDisplayWrapper> SearchDTODetailsDisplayWrapperList = new ArrayList<>( searches.size() );
			
			for ( SearchDTO search : searches ) {
			
				SearchDTODetailsDisplayWrapper searchDTODetailsDisplayWrapper = new SearchDTODetailsDisplayWrapper();
				
				searchDTODetailsDisplayWrapper.setSearchDTO(search);
				SearchDTODetailsDisplayWrapperList.add(searchDTODetailsDisplayWrapper);
			}
			
			

			//  If user is Researcher or better and Proxl XML File Import is Fully Configured, 
			//  get submitted Proxl XML files

			if ( authAccessLevel.isAssistantProjectOwnerAllowed() 
					&& IsProxlXMLFileImportFullyConfigured.getInstance().isProxlXMLFileImportFullyConfigured() ) {
			

				int pendingCount = 
						ProxlXMLFileImportTracking_PendingCount_Searcher.getInstance().getPendingCountForProject( projectId );
				
				request.setAttribute( "proxlXMLFileImportTrackingPendingCount", pendingCount );
				
			}
			

			request.setAttribute( "project", projectDTO );
			
			request.setAttribute( "projectPublicAccessData", projectPublicAccessData );

			request.setAttribute( "notes", notes );

			request.setAttribute( "SearchDTODetailsDisplayWrapperList", SearchDTODetailsDisplayWrapperList );

			//  If user is project owner or better, get other projects this user has rights to

			if ( authAccessLevel.isProjectOwnerAllowed() ) {
				
				List<ProjectDTO> otherProjectList = null;

				if ( authAccessLevel.isAdminAllowed() ) {
					
					// Get all projects excluding current one
					
					otherProjectList = projectDAO.getAllExcludingProjectId( projectId );
				
				} else {
			
					//  Get projects this user is owner for
					
					int authUserId = userSessionObject.getUserDBObject().getAuthUser().getId();
					
					int maxAuthLevel = AuthAccessLevelConstants.ACCESS_LEVEL_PROJECT_OWNER;
					
					otherProjectList = projectDAO.getForAuthUserExcludingProjectId( authUserId, maxAuthLevel, projectId );
			
				}
				
				request.setAttribute( "otherProjectList", otherProjectList );
			}
			
			
			ActionForward actionForward =  mapping.findForward( "Success" );
			
			return actionForward;
			
		} catch ( Exception e ) {
			
			String msg = "Exception caught: " + e.toString();
			
			log.error( msg, e );
			
			throw e;
		}
	}

		
}