package org.yeastrc.xlink.www.file_import_proxl_xml_scans.webservices;

import java.io.File;
import java.io.FileWriter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.yeastrc.xlink.base.file_import_proxl_xml_scans.utils.Proxl_XML_ImporterWrkDirAndSbDrsCmmn;
import org.yeastrc.xlink.www.constants.WebServiceErrorMessageConstants;
import org.yeastrc.xlink.www.dao.ProjectDAO;
import org.yeastrc.xlink.www.dto.ProjectDTO;
import org.yeastrc.xlink.www.exceptions.ProxlWebappDataException;
import org.yeastrc.xlink.www.exceptions.ProxlWebappFileUploadFileSystemException;
import org.yeastrc.xlink.www.objects.AuthAccessLevel;
import org.yeastrc.xlink.www.file_import_proxl_xml_scans.constants.ProxlXMLFileUploadSubmitterPgmSameMachineConstants;
import org.yeastrc.xlink.www.file_import_proxl_xml_scans.constants.ProxlXMLFileUploadWebConstants;
import org.yeastrc.xlink.www.file_import_proxl_xml_scans.utils.IsProxlXMLFileImportFullyConfigured;
import org.yeastrc.xlink.www.file_import_proxl_xml_scans.utils.Proxl_XML_Importer_Work_Directory_And_SubDirs_Web;
import org.yeastrc.xlink.www.user_account.UserSessionObject;
import org.yeastrc.xlink.www.user_web_utils.AccessAndSetupWebSessionResult;
import org.yeastrc.xlink.www.user_web_utils.GetAccessAndSetupWebSession;

/**
 * Initialization to receive files.  
 * 
 * Called when user opens the Upload Proxl XML for Import overlay
 *
 */
@Path("/file_import_proxl_xml_scans")
public class ProxlXMLFileImportUploadInitService {
	
	private static final Logger log = Logger.getLogger(ProxlXMLFileImportUploadInitService.class);
	
	private static final String SUBMITTER_SAME_MACHINE_TRUE = Boolean.TRUE.toString();
	
	@POST
	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/uploadInit")
	public UploadInitResponse  uploadInit( 
			@FormParam( "project_id" ) Integer projectId,
			@FormParam( "submitter_same_machine" ) String submitterSameMachineString,
			@Context HttpServletRequest request ) throws Exception {
		boolean submitterSameMachine = false;
		try {
			if ( projectId == null ) {
				String msg = "missing project_id ";
				log.error( msg );
			    throw new WebApplicationException(
			    	      Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)  //  return 400 error
			    	        .entity( msg )
			    	        .build()
			    	        );
			}
			if ( projectId == 0 ) {
				String msg = "Provided project_id is zero, is = " + projectId;
				log.error( msg );
			    throw new WebApplicationException(
			    	      Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)  //  return 400 error
			    	        .entity( msg )
			    	        .build()
			    	        );
			}
			if ( SUBMITTER_SAME_MACHINE_TRUE.equals( submitterSameMachineString ) ) {
				submitterSameMachine = true;
			}
			// Get the session first.  
//			HttpSession session = request.getSession();
			//   Get the project id for this search
			AccessAndSetupWebSessionResult accessAndSetupWebSessionResult =
					GetAccessAndSetupWebSession.getInstance().getAccessAndSetupWebSessionWithProjectId( projectId, request );
//			UserSessionObject userSessionObject = accessAndSetupWebSessionResult.getUserSessionObject();
			if ( accessAndSetupWebSessionResult.isNoSession() ) {
				//  No User session 
				throw new WebApplicationException(
						Response.status( WebServiceErrorMessageConstants.NO_SESSION_STATUS_CODE )  //  Send HTTP code
						.entity( WebServiceErrorMessageConstants.NO_SESSION_TEXT ) // This string will be passed to the client
						.build()
						);
			}
			//  Test access to the project id
			AuthAccessLevel authAccessLevel = accessAndSetupWebSessionResult.getAuthAccessLevel();
			if ( ! authAccessLevel.isProjectOwnerAllowed() ) {
				//  No Access Allowed for this project id
				throw new WebApplicationException(
						Response.status( WebServiceErrorMessageConstants.NOT_AUTHORIZED_STATUS_CODE )  //  Send HTTP code
						.entity( WebServiceErrorMessageConstants.NOT_AUTHORIZED_TEXT ) // This string will be passed to the client
						.build()
						);
			}
			//  If NOT Proxl XML File Import is Fully Configured, 
			if ( ! IsProxlXMLFileImportFullyConfigured.getInstance().isProxlXMLFileImportFullyConfigured() ) {
				String msg = "Proxl XML File Import is NOT Fully Configured ";
				log.warn( msg );
			    throw new WebApplicationException(
			    	      Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)  //  return 400 error
			    	        .entity( msg )
			    	        .build()
			    	        );
			}
			//  Confirm projectId is in database
			ProjectDTO projectDTO =	ProjectDAO.getInstance().getProjectDTOForProjectId( projectId );
			if ( projectDTO == null ) {
				// should never happen
				String msg = "Project id is not in database " + projectId;
				log.warn( msg );
			    throw new WebApplicationException(
			    	      Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)  //  return 400 error
			    	        .entity( msg )
			    	        .build()
			    	        );
			}
			if ( ( ! projectDTO.isEnabled() ) || ( projectDTO.isMarkedForDeletion() ) ) {
				String msg = "Project id is disabled or marked for deletion: " + projectId;
				log.warn( msg );
			    throw new WebApplicationException(
			    	      Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)  //  return 400 error
			    	        .entity( msg )
			    	        .build()
			    	        );
			}
			if ( ( projectDTO.isProjectLocked() ) ) {
				String msg = "Project id is locked: " + projectId;
				log.warn( msg );
				UploadInitResponse uploadInitResponse = new UploadInitResponse();
				uploadInitResponse.setStatusSuccess(false);
				uploadInitResponse.setProjectLocked(true);
				return uploadInitResponse;  //  EARLY EXIT
			}
			UserSessionObject userSessionObject = accessAndSetupWebSessionResult.getUserSessionObject();
			int authUserId = userSessionObject.getUserDBObject().getAuthUser().getId();
			File importer_Work_Directory = Proxl_XML_ImporterWrkDirAndSbDrsCmmn.getInstance().get_Proxl_XML_Importer_Work_Directory();
			//  Get the File object for the Base Subdir used to first store the files in this request 
			String uploadFileTempDirString =
					Proxl_XML_Importer_Work_Directory_And_SubDirs_Web.getInstance().getDirForUploadFileTempDir();
			File uploadFileTempDir = new File( importer_Work_Directory, uploadFileTempDirString );
			if ( ! uploadFileTempDir.exists() ) {
//				boolean mkdirResult = 
				uploadFileTempDir.mkdir();
			}
			if ( ! uploadFileTempDir.exists() ) {
				String msg = "uploadFileTempDir does not exist after testing for it and attempting to create it.  uploadFileTempDir: " 
						+ uploadFileTempDir.getAbsolutePath();
				log.error( msg );
				throw new ProxlWebappFileUploadFileSystemException(msg);
			}
			UploadInitResponse uploadInitResponse = new UploadInitResponse();
			long uploadKey = System.currentTimeMillis();
			File createdSubDir = null;
			int retryCreateSubdirCount = 0;
			while ( createdSubDir == null ) {
				retryCreateSubdirCount++;
				if ( retryCreateSubdirCount > 4 ) {
					String msg = "Failed to create subdir after 4 attempts.";
					log.error( msg );
					throw new ProxlWebappFileUploadFileSystemException(msg);
				}
				int uploadKeyIncrement = (int) ( Math.random() * 10 ) + 5;
				uploadKey += uploadKeyIncrement;
				createdSubDir =
						Proxl_XML_Importer_Work_Directory_And_SubDirs_Web.getInstance()
						.createSubDirForUploadFileTempDir( authUserId, uploadKey, uploadFileTempDir );
			}
			//  Create a file in the directory to track the create date/time of the directory
			File createdDirFile = new File( createdSubDir, ProxlXMLFileUploadWebConstants.UPLOAD_FILE_TEMP_SUB_DIR_CREATE_TRACKING_FILE );
			if ( ! createdDirFile.createNewFile() ) {
				String msg = "Failed to create file in subdir: " + createdDirFile.getCanonicalPath();
				log.error( msg );
				throw new ProxlWebappFileUploadFileSystemException(msg);
			}
			String uploadKeyString = Long.toString( uploadKey );
			if ( submitterSameMachine ) {
				//  A program running on the same machine is submitting:
				//   Pass it the upload directory
				//   Pass it the filename that will contain the key required when 
				//        the submitter passes full filename paths to the files to process.
				//  Generate a random string
				StringBuilder submitterKeySB = new StringBuilder( 200 );
				for ( int i = 0; i < 15; i++ ) {
					double submitterKeyMultiplier = Math.random();
					if ( submitterKeyMultiplier < 0.5 ) {
						submitterKeyMultiplier += 0.5;
					}
					long submitterKeyLong = (long) ( System.currentTimeMillis() * submitterKeyMultiplier );
					submitterKeySB.append( Long.toHexString( submitterKeyLong ) );
				}
				String submitterKey = submitterKeySB.toString();
				//   Write the string to the file
				File submitterKeyFile = new File( 
						createdSubDir, 
						ProxlXMLFileUploadSubmitterPgmSameMachineConstants.SUBMITTER_KEY_FILENAME );
				FileWriter fileWriter = null;
				try {
					fileWriter = new FileWriter( submitterKeyFile );
					fileWriter.write( submitterKey );
				} catch ( Exception e ) {
				} finally {
					try {
						if ( fileWriter != null ) {
							fileWriter.close();
						}
					} catch ( Exception e ) {
					}
				}
				//  Update the response with the subdirectory name 
				uploadInitResponse.uploadTempSubdir = createdSubDir.getName();
			}
			uploadInitResponse.statusSuccess = true;
			uploadInitResponse.uploadKey = uploadKeyString;
			return uploadInitResponse;
		} catch ( WebApplicationException e ) {
			throw e;
		} catch ( ProxlWebappDataException e ) {
			String msg = "Exception processing request data, msg: " + e.toString();
			log.error( msg, e );
		    throw new WebApplicationException(
		    	      Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)  //  return 400 error
		    	        .entity( msg )
		    	        .build()
		    	        );			
		} catch ( Exception e ) {
			String msg = "Exception caught: " + e.toString();
			log.error( msg, e );
			throw new WebApplicationException(
					Response.status( WebServiceErrorMessageConstants.INTERNAL_SERVER_ERROR_STATUS_CODE )  //  Send HTTP code
					.entity( WebServiceErrorMessageConstants.INTERNAL_SERVER_ERROR_TEXT ) // This string will be passed to the client
					.build()
					);
		}
	}
	
	/**
	 * 
	 *
	 */
	public static class UploadInitResponse {
		private String uploadKey;
		private boolean statusSuccess;
		private boolean projectLocked; 
		//  Added for processing submit from same machine
		private String uploadTempSubdir;
		public String getUploadKey() {
			return uploadKey;
		}
		public void setUploadKey(String uploadKey) {
			this.uploadKey = uploadKey;
		}
		public boolean isStatusSuccess() {
			return statusSuccess;
		}
		public void setStatusSuccess(boolean statusSuccess) {
			this.statusSuccess = statusSuccess;
		}
		public boolean isProjectLocked() {
			return projectLocked;
		}
		public void setProjectLocked(boolean projectLocked) {
			this.projectLocked = projectLocked;
		}
		public String getUploadTempSubdir() {
			return uploadTempSubdir;
		}
		public void setUploadTempSubdir(String uploadTempSubdir) {
			this.uploadTempSubdir = uploadTempSubdir;
		}
	}
}
