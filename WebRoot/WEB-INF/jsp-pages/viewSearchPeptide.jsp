<%@page import="org.apache.log4j.Logger"%>
<%@page import="org.yeastrc.xlink.www.webapp_timing.WebappTiming"%>
<%@ include file="/WEB-INF/jsp-includes/pageEncodingDirective.jsp" %>
<%@page import="org.yeastrc.xlink.www.constants.PeptideViewLinkTypesConstants"%>

<%@ include file="/WEB-INF/jsp-includes/strutsTaglibImport.jsp" %>
<%@ include file="/WEB-INF/jsp-includes/jstlTaglibImport.jsp" %>

 <c:set var="pageTitle">View Search</c:set>

 <c:set var="pageBodyClass" >project-page</c:set>

 <c:set var="helpURLExtensionForSpecificPage" >en/latest/using/peptide.html</c:set>

 <c:set var="headerAdditions">

		<script type="text/javascript" src="${ contextPath }/js/handleServicesAJAXErrors.js"></script> 
		
		
		<script type="text/javascript" src="${ contextPath }/js/executeAfter_HTML_AboveMainTableRendered_Registry.js"></script> 

		<script type="text/javascript" src="${ contextPath }/js/libs/jquery.tablesorter.min.js"></script> 
		<script type="text/javascript" src="${ contextPath }/js/libs/jquery.qtip.min.js"></script>
		
		
		
<%--  Start of Lorikeet Core Parts --%>		

		<script src="${contextPath}/js/libs/jquery-ui-1.10.4.min.js"></script>
		
		<%--  Only load the excanvas.min.js if it is IE 8 or lower.  IE 8 does not support HTML5 so this is a way to have HTML5 canvas support --%>
		<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="${contextPath}/js/lorikeet_google_code/excanvas.min.js"></script><![endif]-->
		
		<script src="${contextPath}/js/lorikeet/jquery.flot.js"></script>
		<script src="${contextPath}/js/lorikeet/jquery.flot.selection.js"></script>
		
		<script src="${contextPath}/js/lorikeet/specview.js"></script>
		<script src="${contextPath}/js/lorikeet/peptide.js"></script>
		<script src="${contextPath}/js/lorikeet/aminoacid.js"></script>
		<script src="${contextPath}/js/lorikeet/ion.js"></script>		
		
<%--  End of Lorikeet Core Parts --%>		
				

		
		<!-- Handlebars templating library   -->
		
		<%--  
		<script type="text/javascript" src="${ contextPath }/js/libs/handlebars-v2.0.0.js"></script>
		--%>
		
		<!-- use minimized version  -->
		<script type="text/javascript" src="${ contextPath }/js/libs/handlebars-v2.0.0.min.js"></script>

		
					
		<script type="text/javascript" src="${ contextPath }/js/libs/snap.svg-min.js"></script> <%--  Used by lorikeetPageProcessing.js --%>

		<script type="text/javascript" src="${ contextPath }/js/lorikeetPageProcessing.js"></script>

				<%-- 
					The Struts Action for this page must call GetProteinNamesTooltipConfigData
					This input is required on this page:
					<input type="hidden" id="protein_listing_webservice_base_url" value="<c:out value="${ protein_listing_webservice_base_url }"></c:out>">
				  --%>
		<script type="text/javascript" src="${ contextPath }/js/createTooltipForProteinNames.js"></script>
		
		
		<script type="text/javascript" src="${ contextPath }/js/defaultPageView.js"></script>
		
		<script type="text/javascript" src="${ contextPath }/js/toggleVisibility.js"></script>
		
		<script type="text/javascript" src="${ contextPath }/js/psmPeptideCutoffsCommon.js"></script>
		
		<script type="text/javascript" src="${ contextPath }/js/viewPsmsLoadedFromWebServiceTemplate.js"></script>
		
		<script type="text/javascript" src="${ contextPath }/js/viewSearchPeptide.js"></script>
		
		
		<link rel="stylesheet" href="${ contextPath }/css/tablesorter.css" type="text/css" media="print, projection, screen" />
		<link type="text/css" rel="stylesheet" href="${ contextPath }/css/jquery.qtip.min.css" />


		<%--  some classes in this stylesheet collide with some in the lorikeet file since they are set to specific values for lorikeet drag and drop --%>
		<%-- 
		<link REL="stylesheet" TYPE="text/css" HREF="${contextPath}/css/jquery-ui-1.10.2-Themes/ui-lightness/jquery-ui.min.css">
		--%>

		<link REL="stylesheet" TYPE="text/css" HREF="${contextPath}/css/lorikeet.css">



</c:set>



<%@ include file="/WEB-INF/jsp-includes/header_main.jsp" %>

			<%--  protein name data webservice base URL, used by createTooltipForProteinNames.js --%>
	<input type="hidden" id="protein_listing_webservice_base_url" value="<c:out value="${ protein_listing_webservice_base_url }"></c:out>">

		
		<%@ include file="/WEB-INF/jsp-includes/defaultPageViewFragment.jsp" %>
		
		<%@ include file="/WEB-INF/jsp-includes/viewPsmsLoadedFromWebServiceTemplateFragment.jsp" %>
		
			<%@ include file="/WEB-INF/jsp-includes/lorikeet_overlay_section.jsp" %>	
		
			
		<div class="overall-enclosing-block">
	
			<h2 style="margin-bottom:5px;">List search peptides:</h2>
	
			<div style="margin-bottom:20px;">
				[<a class="tool_tip_attached_jq" data-tooltip="View proteins" 
					href="${ contextPath }/<proxl:defaultPageUrl pageName="/crosslinkProtein" searchId="${ viewSearchPeptidesPageDataRoot.searchId }"
							>crosslinkProtein.do?<bean:write name="queryString" /></proxl:defaultPageUrl>"
					>Protein View</a>]
					
					
					
				[<a class="tool_tip_attached_jq" data-tooltip="View protein coverage report" 
					href="${ contextPath }/<proxl:defaultPageUrl pageName="/proteinCoverageReport" searchId="${ viewSearchPeptidesPageDataRoot.searchId }"
							>proteinCoverageReport.do?<bean:write name="queryString" /></proxl:defaultPageUrl>"
													
					>Coverage Report</a>]

				<%-- Navigation links to Merged Image and Merged Structure --%>
				
				<%@ include file="/WEB-INF/jsp-includes/imageAndStructureNavLinks.jsp" %>

				
				
			</div>
	
			<%-- query JSON in field outside of form for input to Javascript --%>
				
			<input type="hidden" id="query_json_field_outside_form" value="<c:out value="${ viewSearchPeptidesPageDataRoot.queryJSONToForm }" ></c:out>" > 
	
			<%--  A block outside any form for PSM Peptide cutoff JS code --%>
			<%@ include file="/WEB-INF/jsp-includes/psmPeptideCutoffBlock_outsideAnyForm.jsp" %>



			<html:form action="peptide" method="get" styleId="form_get_for_updated_parameters"> <%-- id="form_get_for_updated_parameters" --%>
		
						
				<html:hidden property="searchId"/>
				
				<html:hidden property="queryJSON" styleId="query_json_field" />
			
				<%--  A block in the submitted form for PSM Peptide cutoff JS code --%>
				<%@ include file="/WEB-INF/jsp-includes/psmPeptideCutoffBlock_inSubmitForm.jsp" %>
			
			</html:form>

			<%-- WAS			
			<html:form action="viewSearchPeptide" method="get" styleId="form_get_for_updated_parameters">
			

				<html:hidden property="project_id"/>
						
				<html:hidden property="searchId"/>
						
			--%>
			
<%--
		Moved JS call to the "Update" button
		 			
			<form action="javascript:viewSearchPeptidePageCode.updatePageForFormParams()" method="get" 

				--%>	 <%-- id="form_get_for_updated_parameters" --%>
		
			
			<table style="border-width:0px;">

					
				<%--  Set to false to not show color block before search for key --%>
				<c:set var="showSearchColorBlock" value="${ false }" />
						
				<%--  Include file is dependent on containing loop having varStatus="searchVarStatus"  --%>
				<%@ include file="/WEB-INF/jsp-includes/searchDetailsBlock.jsp" %>


				
				<tr>
					<td>Type Filter:</td>
					<td colspan="2">
						<%--  Update TestAllWebLinkTypesSelected if add another option --%>

					  <label >
						<input type="checkbox" class=" link_type_jq " 
							onchange="defaultPageView.searchFormChanged_ForDefaultPageView();"
							value="<%= PeptideViewLinkTypesConstants.CROSSLINK_PSM %>"   >
						crosslinks
					  </label>
					  <label >
						<input type="checkbox" class=" link_type_jq " 
							onchange="defaultPageView.searchFormChanged_ForDefaultPageView();"
							value="<%= PeptideViewLinkTypesConstants.LOOPLINK_PSM %>" >
						looplinks
					  </label> 
					  <label >
						<input type="checkbox" class=" link_type_jq " 
							onchange="defaultPageView.searchFormChanged_ForDefaultPageView();"
							value="<%= PeptideViewLinkTypesConstants.UNLINKED_PSM %>" >
						 unlinked
					  </label>

					</td>
				</tr>
				<tr>
					<td valign="top" >Modification Filter:</td>
					<td colspan="2">
					  <label >
						<input type="checkbox" class=" mod_mass_filter_jq " 
							onchange="defaultPageView.searchFormChanged_ForDefaultPageView();"
							value="" >
						No modifications
					  </label>
					  
						<logic:iterate id="modMassFilter" name="viewSearchPeptidesPageDataRoot" property="modMassFilterList">
						
						 <label style="white-space: nowrap" >
							<input type="checkbox" class=" mod_mass_filter_jq " 
								onchange="defaultPageView.searchFormChanged_ForDefaultPageView();"
						  		value="<bean:write name="modMassFilter" />" > 
						   <bean:write name="modMassFilter" />
						 </label>
						  
						</logic:iterate>				
					</td>
				</tr>				
				
				
				<tr>
					<td>&nbsp;</td>
					<td colspan="5">
					
						<c:set var="UpdateButtonText" value="Update"/>
						
						<input type="button" value="${ UpdateButtonText }"  onclick="viewSearchPeptidePageCode.updatePageForFormParams()" >
						
						<c:set var="searchId" value="${ viewSearchPeptidesPageDataRoot.searchId }"/>	

						<c:set var="page_JS_Object" value="viewSearchPeptidePageCode"/>
						
						<%@ include file="/WEB-INF/jsp-includes/defaultPageViewButtonFragment.jsp" %>

					</td>
				</tr>
			
			</table>
			
<%-- 			
			</form>
--%>
			
	
			<h3 style="display:inline;">Peptides (<c:out value="${ viewSearchPeptidesPageDataRoot.peptideListSize }"></c:out>):</h3>
			
			<div style="display:inline;">
				[<a class="tool_tip_attached_jq" data-tooltip="Download data as tab-delimited text" 
						href="${ contextPath }/downloadMergedPeptides.do?<bean:write name="mergedQueryString" />"
					>Download Data</a>]
					
				[<a class="tool_tip_attached_jq" data-tooltip="Download PSM data as tab-delimited text" 
						href="${ contextPath }/downloadMergedPSMsForPeptides.do?<bean:write name="mergedQueryString" />"
					>Download PSM Data</a>]
			</div>

			<%--  Create via javascript the parts that will be above the main table --%>
			<script type="text/javascript">
			
			executeAfter_HTML_AboveMainTableRendered_Registry.createPartsAboveMainTable();
				
			</script>
			
  
			  
				<table style="" id="crosslink-table" class="tablesorter  top_data_table_jq ">
				
					<thead>
					<tr>
						<th data-tooltip="Type of peptide (e.g., crosslink, looplink, or unlinked)" class="tool_tip_attached_jq" style="text-align:left;width:10%;font-weight:bold;">Type</th>
						<th data-tooltip="The peptide as reported by search program (e.g., Kojak or XQuest)" class="tool_tip_attached_jq" style="text-align:left;width:10%;font-weight:bold;">Reported peptide</th>
						<th data-tooltip="Sequence of matched peptide (or first peptide in case of crosslinks)" class="tool_tip_attached_jq" style="text-align:left;width:10%;font-weight:bold;">Peptide 1</th>
						<th data-tooltip="Linked position in first peptide (or starting position of looplink)" class="tool_tip_attached_jq integer-number-column-header" style="width:5%;font-weight:bold;">Pos</th>
						<th data-tooltip="Sequenced of second matched peptide in crosslink" class="tool_tip_attached_jq" style="text-align:left;width:10%;font-weight:bold;">Peptide 2</th>
						<th data-tooltip="Linked position in second peptide (or ending position of looplink)" class="tool_tip_attached_jq integer-number-column-header" style="width:5%;font-weight:bold;">Pos</th>
						<th data-tooltip="Proteins (and positions) matched by first peptide and position" class="tool_tip_attached_jq" style="text-align:left;width:5%;font-weight:bold;">Protein 1</th>
						<th data-tooltip="Proteins (and positions) matched by second peptide and position" class="tool_tip_attached_jq" style="text-align:left;width:5%;font-weight:bold;">Protein 2</th>

						<c:forEach var="peptideAnnotationDisplayNameDescription" items="${ viewSearchPeptidesPageDataRoot.peptideAnnotationDisplayNameDescriptionList }">

								<%--  Consider displaying the description somewhere   peptideAnnotationDisplayNameDescription.description --%>
							<th data-tooltip="Peptide-level <c:out value="${ peptideAnnotationDisplayNameDescription.displayName }"></c:out> for this peptide (or linked pair)" 
									class="tool_tip_attached_jq" 
									style="width:10%;font-weight:bold;">
								<span style="white-space: nowrap"><c:out value="${ peptideAnnotationDisplayNameDescription.displayName }"></c:out></span>
							</th>
							
						</c:forEach>
															
						
						<th data-tooltip="Number of PSMs matched to this peptide (or linked pair)" class="tool_tip_attached_jq integer-number-column-header" style="width:10%;font-weight:bold; white-space: nowrap;"># PSMs</th>
						
						<c:if test="${ viewSearchPeptidesPageDataRoot.showNumberUniquePSMs }">
							<th data-tooltip="Number of scans that uniquely matched to this reported peptide" class="tool_tip_attached_jq integer-number-column-header" style="width:10%;font-weight:bold; white-space: nowrap;"># Unique</th>
						</c:if>
						
						<c:forEach var="psmAnnotationDisplayNameDescription" items="${ viewSearchPeptidesPageDataRoot.psmAnnotationDisplayNameDescriptionList }">

								<%--  Consider displaying the description somewhere   psmAnnotationDisplayNameDescription.description --%>
						  <th data-tooltip="Best PSM-level <c:out value="${ psmAnnotationDisplayNameDescription.displayName }"></c:out> for PSMs matched to peptides that show this link" class="tool_tip_attached_jq" style="width:10%;font-weight:bold;"
							><span style="white-space: nowrap">Best PSM</span> 
								<span style="white-space: nowrap"><c:out value="${ psmAnnotationDisplayNameDescription.displayName }"></c:out></span></th>

						</c:forEach>
									
					</tr>
					</thead>
						
					<c:forEach var="peptideEntry" items="${ viewSearchPeptidesPageDataRoot.peptideList }">

							<tr id="reported-peptide-<bean:write name="peptideEntry" property="reportedPeptide.id"/>"
								style="cursor: pointer; "
								onclick="viewPsmsLoadedFromWebServiceTemplate.showHidePsms( { clickedElement : this } )"
								reported_peptide_id="${ peptideEntry.reportedPeptide.id }"
								search_id="${ viewSearchPeptidesPageDataRoot.searchId }"
								project_id="${ projectId }"
								>
								
								<td>
									<c:choose>
										<c:when test="${ not empty peptideEntry.searchPeptideCrosslink }">Crosslink</c:when>
										<c:when test="${ not empty peptideEntry.searchPeptideLooplink }">Looplink</c:when>
<%-- 										
										<c:when test="${ not empty peptideEntry.searchPeptideUnlinked or not empty peptideEntry.searchPeptideDimer }"
											>Unlinked</c:when>
--%>											
										<c:when test="${ not empty peptideEntry.searchPeptideUnlinked }"
											>Unlinked</c:when>
										<c:when test="${ not empty peptideEntry.searchPeptideDimer }"
											>Dimer</c:when>
										<c:otherwise>Unknown</c:otherwise>
									</c:choose>
								
								</td>
								
								<td><bean:write name="peptideEntry" property="reportedPeptide.sequence" /></td>
								<td><bean:write name="peptideEntry" property="peptide1.sequence" /></td>
								<td class="integer-number-column" ><bean:write name="peptideEntry" property="peptide1Position" /></td>
								<td><c:if test="${ not empty peptideEntry.peptide2 }" ><bean:write name="peptideEntry" property="peptide2.sequence" /></c:if></td>
								<td class="integer-number-column" ><bean:write name="peptideEntry" property="peptide2Position" /></td>
								<td>
								  <c:if test="${ not empty peptideEntry.peptide1ProteinPositions }">
									<logic:iterate id="pp" name="peptideEntry" property="peptide1ProteinPositions">
										<span class="proteinName" id="protein-id-<bean:write name="pp" property="protein.nrProtein.nrseqId" />">
											<bean:write name="pp" property="protein.name" 
												/><c:if test="${ not empty pp.position1 }"
														>(<bean:write name="pp" property="position1" 
													/><c:if test="${ not empty pp.position2 }"
														>, <bean:write name="pp" property="position2" 
														/></c:if>)</c:if><br>
										</span>
									</logic:iterate>
								  </c:if>
								</td>
								<td>
								  <c:if test="${ not empty peptideEntry.peptide2ProteinPositions }">
									<logic:iterate id="pp" name="peptideEntry" property="peptide2ProteinPositions">
										<span class="proteinName" id="protein-id-<bean:write name="pp" property="protein.nrProtein.nrseqId" />">
											<bean:write name="pp" property="protein.name" 
												/><c:if test="${ not empty pp.position1 }"
														>(<bean:write name="pp" property="position1" 
													/><c:if test="${ not empty pp.position2 }"
														>, <bean:write name="pp" property="position2" 
														/></c:if>)</c:if><br>
										</span>
									</logic:iterate>
								  </c:if>
								</td>

								<c:forEach var="annotationValue" items="${ peptideEntry.peptideAnnotationValueList }">
			
									<td style="white-space: nowrap"><c:out  value="${ annotationValue }" /></td>
								</c:forEach>								
								

								<td class="integer-number-column" ><a class="show-child-data-link  " 
										href="javascript:"

										><bean:write name="peptideEntry" property="numPsms" 
											/><span class="toggle_visibility_expansion_span_jq" 
												><img src="${contextPath}/images/icon-expand-small.png" 
													class=" icon-expand-contract-in-data-table "
													></span><span class="toggle_visibility_contraction_span_jq" 
														style="display: none;" 
														><img src="${contextPath}/images/icon-collapse-small.png"
															class=" icon-expand-contract-in-data-table "
															></span>
									</a>
								</td>

								<c:if test="${ viewSearchPeptidesPageDataRoot.showNumberUniquePSMs }">
								
									<td class="integer-number-column" 
										><bean:write name="peptideEntry" property="numUniquePsms" />
									</td>
								</c:if>					
			
								<c:forEach var="annotationValue" items="${ peptideEntry.psmAnnotationValueList }">
			
									<td><c:out  value="${ annotationValue }" /></td>
								</c:forEach>
								
							</tr>

							<tr class="expand-child" style="display:none;">
							 
								<%--  Adjust colspan for number of columns in current table --%>
								
								
								<%--  Init to zero --%>
								<c:set var="colspanPeptidesAdded" value="${ 0 }" />
								
								<%--   Now add 1 for each column being displayed --%>
								
								<c:forEach var="annotationValue" items="${ peptideEntry.peptideAnnotationValueList }">
			
									<c:set var="colspanPeptidesAdded" value="${ colspanPeptidesAdded + 1 }" />
								</c:forEach>								
													
								<c:forEach var="annotationValue" items="${ peptideEntry.psmAnnotationValueList }">
			
									<c:set var="colspanPeptidesAdded" value="${ colspanPeptidesAdded + 1 }" />
								</c:forEach>
											
															 
							 <td colspan="<c:out value="${ 10 + colspanPeptidesAdded }"></c:out>" align="center" class=" child_data_container_jq ">
									
									<div style="color: green; font-size: 16px; padding-top: 10px; padding-bottom: 10px;" >
										Loading...
									</div>
							 </td>
							</tr>

					</c:forEach>
				</table>
	
			

		</div>
	

<%@ include file="/WEB-INF/jsp-includes/footer_main.jsp" %>



<%

WebappTiming webappTiming = (WebappTiming)request.getAttribute( "webappTiming" );

if ( webappTiming != null ) {
		
	webappTiming.markPoint( "At end of JSP" );
	
	webappTiming.logTiming();
}



%>

