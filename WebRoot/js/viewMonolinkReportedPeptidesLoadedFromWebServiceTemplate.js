
//   viewMonolinkReportedPeptidesLoadedFromWebServiceTemplate.js


//  Monolink Reported Peptide

//   Process and load data into the file viewMonolinkReportedPeptidesLoadedFromWebServiceTemplateFragment.jsp


//////////////////////////////////

// JavaScript directive:   all variables have to be declared with "var", maybe other things

"use strict";


//   Class contructor

var ViewMonolinkReportedPeptidesLoadedFromWebServiceTemplate = function() {

	var _DATA_LOADED_DATA_KEY = "dataLoaded";
	
	var _handlebarsTemplate_monolink_peptide_block_template = null;
	var _handlebarsTemplate_monolink_peptide_data_row_entry_template = null;
	var _handlebarsTemplate_monolink_peptide_child_row_entry_template = null;
	

	var _psmPeptideCutoffsRootObject = null;

	//   Currently expect _psmPeptideCriteria = 
//					searches: Object
//						128: Object			
//							peptideCutoffValues: Object
//								238: Object
//									id: 238
//									value: "0.01"
//							psmCutoffValues: Object
//								384: Object
//									id: 384
//									value: "0.01"
//							searchId: 128
	
//           The key to:
//				searches - searchId
//				peptideCutoffValues and psmCutoffValues - annotation type id
	
//			peptideCutoffValues.id and psmCutoffValues.id - annotation type id
	
	
	//////////////
	
	this.setPsmPeptideCriteria = function( psmPeptideCutoffsRootObject ) {
		
		_psmPeptideCutoffsRootObject = psmPeptideCutoffsRootObject;
	};
	
	
	
	// ////////////
	
	
	this.showHideMonolinkReportedPeptides = function( params ) {
		
		var clickedElement = params.clickedElement;
		
		var $clickedElement = $( clickedElement );
		
		var $itemToToggle = $clickedElement.next();



		if( $itemToToggle.is(":visible" ) ) {

			$itemToToggle.hide(); 

			$clickedElement.find(".toggle_visibility_expansion_span_jq").show();
			$clickedElement.find(".toggle_visibility_contraction_span_jq").hide();
		} else { 
			$itemToToggle.show();

			$clickedElement.find(".toggle_visibility_expansion_span_jq").hide();
			$clickedElement.find(".toggle_visibility_contraction_span_jq").show();

			this.loadAndInsertMonolinkReportedPeptidesIfNeeded( { $topTRelement : $itemToToggle, $clickedElement : $clickedElement } );
		}

		return false;  // does not stop bubbling of click event
	};
	
		
	
	////////////////////////
	
	this.loadAndInsertMonolinkReportedPeptidesIfNeeded = function( params ) {

		var objectThis = this;
		
		var $topTRelement = params.$topTRelement;
		var $clickedElement = params.$clickedElement;
		

		var dataLoaded = $topTRelement.data( _DATA_LOADED_DATA_KEY );
		
		if ( dataLoaded ) {
			
			return;  //  EARLY EXIT  since data already loaded. 
		}
		

		
		var search_id = $clickedElement.attr( "search_id" );
		var protein_id = $clickedElement.attr( "protein_id" );
		var protein_position = $clickedElement.attr( "protein_position" );

		
		//  Convert all attributes to empty string if null or undefined
		if ( ! search_id ) {
			search_id = "";
		}

		if ( ! protein_id ) {
			protein_id = "";
		}
		if ( ! protein_position ) {
			protein_position = "";
		}

		


		//   Currently expect _psmPeptideCriteria = 
//						searches: Object
//							128: Object			
//								peptideCutoffValues: Object
//									238: Object
//										id: 238
//										value: "0.01"
//								psmCutoffValues: Object
//									384: Object
//										id: 384
//										value: "0.01"
//								searchId: 128
		
//	           The key to:
//					searches - searchId
//					peptideCutoffValues and psmCutoffValues - annotation type id
		
//				peptideCutoffValues.id and psmCutoffValues.id - annotation type id
		
		if ( _psmPeptideCutoffsRootObject === null || _psmPeptideCutoffsRootObject === undefined ) {
			
			throw "_psmPeptideCutoffsRootObject not initialized";
		} 
		
		var psmPeptideCutoffsForSearchId = _psmPeptideCutoffsRootObject.searches[ search_id ];

		if ( psmPeptideCutoffsForSearchId === undefined || psmPeptideCutoffsForSearchId === null ) {
			
			psmPeptideCutoffsForSearchId = {};
			
//			throw "Getting data.  Unable to get cutoff data for search id: " + search_id;
		}

		var psmPeptideCutoffsForSearchId_JSONString = JSON.stringify( psmPeptideCutoffsForSearchId );

				
		
		var ajaxRequestData = {

				search_id : search_id,
				protein_id : protein_id,
				protein_position : protein_position,
				psmPeptideCutoffsForSearchId : psmPeptideCutoffsForSearchId_JSONString
		};
		
		
		$.ajax({
			url : contextPathJSVar + "/services/data/getMonolinkReportedPeptides",

//			traditional: true,  //  Force traditional serialization of the data sent
//								//   One thing this means is that arrays are sent as the object property instead of object property followed by "[]".
//								//   So searchIds array is passed as "searchIds=<value>" which is what Jersey expects
			
			data : ajaxRequestData,  // The data sent as params on the URL
			dataType : "json",

			success : function( ajaxResponseData ) {
				
				var responseParams = {
						ajaxResponseData : ajaxResponseData, 
						ajaxRequestData : ajaxRequestData,
						$topTRelement : $topTRelement,
						$clickedElement : $clickedElement
				};

				objectThis.loadAndInsertMonolinkReportedPeptidesResponse( responseParams );
				

				$topTRelement.data( _DATA_LOADED_DATA_KEY, true );
				
			},
	        failure: function(errMsg) {
	        	handleAJAXFailure( errMsg );
	        },

			error : function(jqXHR, textStatus, errorThrown) {

				handleAJAXError(jqXHR, textStatus, errorThrown);

			}
		});
		
		
		
	};
	
	
	this.loadAndInsertMonolinkReportedPeptidesResponse = function( params ) {
		
		var ajaxResponseData = params.ajaxResponseData;
		
		var ajaxRequestData = params.ajaxRequestData;

		var $topTRelement = params.$topTRelement;

		var $clickedElement = params.$clickedElement;
		

		var show_children_if_one_row = $clickedElement.attr( "show_children_if_one_row" );
		
		
		
		var peptideAnnotationDisplayNameDescriptionList = ajaxResponseData.peptideAnnotationDisplayNameDescriptionList;
		var psmAnnotationDisplayNameDescriptionList = ajaxResponseData.psmAnnotationDisplayNameDescriptionList;
		
		var monolink_peptides = ajaxResponseData.searchPeptideMonolinkList;

		
		var $monolink_peptide_data_container = $topTRelement.find(".child_data_container_jq");
		
		if ( $monolink_peptide_data_container.length === 0 ) {
			
			throw "unable to find HTML element with class 'child_data_container_jq'";
		}

		$monolink_peptide_data_container.empty();
		
		if ( _handlebarsTemplate_monolink_peptide_block_template === null ) {
			
			var handlebarsSource_monolink_peptide_block_template = $( "#monolink_peptide_block_template" ).html();
			
			if ( handlebarsSource_monolink_peptide_block_template === undefined ) {
				throw "handlebarsSource_monolink_peptide_block_template === undefined";
			}
			if ( handlebarsSource_monolink_peptide_block_template === null ) {
				throw "handlebarsSource_monolink_peptide_block_template === null";
			}
			
			_handlebarsTemplate_monolink_peptide_block_template = Handlebars.compile( handlebarsSource_monolink_peptide_block_template );
		}
	
		if ( _handlebarsTemplate_monolink_peptide_data_row_entry_template === null ) {

			var handlebarsSource_monolink_peptide_data_row_entry_template = $( "#monolink_peptide_data_row_entry_template" ).html();
			
			if ( handlebarsSource_monolink_peptide_data_row_entry_template === undefined ) {
				throw "handlebarsSource_monolink_peptide_data_row_entry_template === undefined";
			}
			if ( handlebarsSource_monolink_peptide_data_row_entry_template === null ) {
				throw "handlebarsSource_monolink_peptide_data_row_entry_template === null";
			}
			
			_handlebarsTemplate_monolink_peptide_data_row_entry_template = Handlebars.compile( handlebarsSource_monolink_peptide_data_row_entry_template );
		}
		
		
		if ( _handlebarsTemplate_monolink_peptide_child_row_entry_template === null ) {

			if ( _handlebarsTemplate_monolink_peptide_child_row_entry_template === null ) {

				var handlebarsSource_monolink_peptide_child_row_entry_template = $( "#monolink_peptide_child_row_entry_template" ).html();

				if ( handlebarsSource_monolink_peptide_child_row_entry_template === undefined ) {
					throw "handlebarsSource_monolink_peptide_child_row_entry_template === undefined";
				}
				if ( handlebarsSource_monolink_peptide_child_row_entry_template === null ) {
					throw "handlebarsSource_monolink_peptide_child_row_entry_template === null";
				}
				
				_handlebarsTemplate_monolink_peptide_child_row_entry_template = Handlebars.compile( handlebarsSource_monolink_peptide_child_row_entry_template );
			}
		}

		//  Search for NumberUniquePSMs being set in any row

		var showNumberUniquePSMs = false;
		
		for ( var monolink_peptideIndex = 0; monolink_peptideIndex < monolink_peptides.length ; monolink_peptideIndex++ ) {
			
			var monolink_peptide = monolink_peptides[ monolink_peptideIndex ];
		
			if ( monolink_peptide.numUniquePsms !== undefined && monolink_peptide.numUniquePsms !== null ) {
				
				showNumberUniquePSMs = true;
				break;
			}
		}
		

		//  create context for header row
		var context = { 
				showNumberUniquePSMs : showNumberUniquePSMs,
				peptideAnnotationDisplayNameDescriptionList : peptideAnnotationDisplayNameDescriptionList,
				psmAnnotationDisplayNameDescriptionList : psmAnnotationDisplayNameDescriptionList
				
		};


		var html = _handlebarsTemplate_monolink_peptide_block_template(context);

		var $monolink_peptide_block_template = $(html).appendTo($monolink_peptide_data_container);
		
		var monolink_peptide_table_jq_ClassName = "monolink_peptide_table_jq";
		
		var $monolink_peptide_table_jq = $monolink_peptide_block_template.find("." + monolink_peptide_table_jq_ClassName );
	
		if ( $monolink_peptide_table_jq.length === 0 ) {
			
			throw "unable to find HTML element with class '" + monolink_peptide_table_jq_ClassName + "'";
		}
		
		
		//  Add monolink_peptide data to the page
	
		for ( var monolink_peptideIndex = 0; monolink_peptideIndex < monolink_peptides.length ; monolink_peptideIndex++ ) {
	
			var monolink_peptide = monolink_peptides[ monolink_peptideIndex ];
			
			//  wrap data in an object to allow adding more fields
			var context = { 
					showNumberUniquePSMs : showNumberUniquePSMs,
					data : monolink_peptide, 
					searchId : ajaxRequestData.search_id
					};
	
			var html = _handlebarsTemplate_monolink_peptide_data_row_entry_template(context);
	
			var $monolink_peptide_entry = 
				$(html).appendTo($monolink_peptide_table_jq);
			

			//  Get the number of columns of the inserted row so can set the "colspan=" in the next row
			//       that holds the child data
			
			var $monolink_peptide_entry__columns = $monolink_peptide_entry.find("td");
			
			var monolink_peptide_entry__numColumns = $monolink_peptide_entry__columns.length;
			
			//  colSpan is used as the value for "colspan=" in the <td>
			var childRowHTML_Context = { colSpan : monolink_peptide_entry__numColumns };
			
			var childRowHTML = _handlebarsTemplate_monolink_peptide_child_row_entry_template( childRowHTML_Context );
			
			//  Add next row for child data
			$( childRowHTML ).appendTo($monolink_peptide_table_jq);	
			

			
			
			
			//  If only one record, click on it to show it's children
			
			if ( show_children_if_one_row === "true" && monolink_peptides.length === 1 ) {
				
				$monolink_peptide_entry.click();
			}
		}
		
		//  Does not seem to work so not run it
//		if ( monolink_peptides.length > 0 ) {
//			
//			try {
//				$monolink_peptide_block_template.tablesorter(); // gets exception if there are no data rows
//			} catch (e) {
//				
//				var z = 0;
//			}
//		}

		
	};
	
};



//Static Singleton Instance of Class

var viewMonolinkReportedPeptidesLoadedFromWebServiceTemplate = new ViewMonolinkReportedPeptidesLoadedFromWebServiceTemplate();