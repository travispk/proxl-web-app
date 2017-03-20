<%@ include file="/WEB-INF/jsp-includes/pageEncodingDirective.jsp" %>


<%@ include file="/WEB-INF/jsp-includes/strutsTaglibImport.jsp" %>
<%@ include file="/WEB-INF/jsp-includes/jstlTaglibImport.jsp" %>


<html>
<head>


 <%@ include file="/WEB-INF/jsp-includes/head_section_include_every_page.jsp" %>

 	<title>ProXL DB</title>
 
 <link REL="stylesheet" TYPE="text/css" HREF="${ contextPath }/css/global.css?x=${cacheBustValue}">

	<%--  Loaded in head_section_include_every_page.jsp   --%>
	<%-- <script type="text/javascript" src="${ contextPath }/js/jquery-1.11.0.min.js"></script>  --%>
	 
	
	
</head>

<body class="reset-password-code-fail-page inset-page"> <%-- "inset-page" is for pages with an 'inset' look --%>

 <%@ include file="/WEB-INF/jsp-includes/body_section_start_include_every_page.jsp" %>


<div class="inset-page-main-outermost-div"> <%--  Closed in footer_main.jsp --%>


<div class="page-content-outer-container" >	
 <div class="page-content-container" >	
  <div class="page-content" >	

	<div class="logo-large-container" >
		<img src="${ contextPath }/images/logo-large.png" />
	</div>
  	
  	<div  style="position: relative;" class="page-label">
  	
  		<div style="font-weight: bold; padding-left: 10px; padding-right: 10px; ">Insufficient access privilege to access or update this data.</div>

  		
  		<div >
		  	<logic:messagesPresent message="false">
		  		
			     <html:messages id="message" >
			     	<div>
			         <bean:write name="message" filter="false"/>
			        </div>
			     </html:messages>
		 	</logic:messagesPresent>
  		</div>

	  <c:if test="${ userLoggedIn }">
  		<br>
  		<a href="home.do" >return to home page</a>
  	  </c:if>
  		
	</div>
	
  </div>
  <div class="bottom-tab">
		<a href="http://www.yeastrc.org/proxl_docs/" >Get Help</a>  		
  </div>
  <div class="bottom-tab" style="border-right-width: 0px;">
		<a href="user_loginPage.do?useDefaultURL=true" >Sign In</a>
  </div>
  
 </div>
</div>

  
<%@ include file="/WEB-INF/jsp-includes/footer_main.jsp" %>
