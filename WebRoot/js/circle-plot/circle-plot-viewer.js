"use strict";

///////////////////////////////////////////

var circlePlotViewer = function() {	
	this.initialize();
};


circlePlotViewer.prototype.initialize  = function(  ) {
	
};

circlePlotViewer.prototype.CONSTANTS = { 

		_DEFAULT_VIEWPORT_HEIGHT : 750,
		_DEFAULT_VIEWPORT_WIDTH : 750,
		
		_GAP_BETWEEN_BARS : 2,			// gap between proteins bars in degrees
		_HEIGHT_OF_PROTEIN_BARS : 40,	// height of protein bars in pixels
		_GAP_BETWEEN_BARS_AND_LINKS : 2, // gap in pixels between protein bars and start of drawn links
		_MINIMUM_CROSSLINK_LOOP_HEIGHT: 60, // minimum height of crosslink loops in pixels
		_MONOLINK_HEIGHT:15,			// height of monolinks in pixels
		
		_LINKABLE_POSITION_COLOR:"#ffffff",	// color to use for linkable positions
		_LINKABLE_POSITION_OPACITY:0.5,	// opacity to use for linkable positions
		
		_SCALE_BAR_HEIGHT:10,		// height of scale bar in pixels
		_SCALE_BAR_FONT_HEIGHT:12,	// height of scale bar font in pixles
		_GAP_BETWEEN_SCALE_BAR_AND_TEXT:5,	// gap (in pixels) between scale bar and scale bar label
		_GAP_BETWEEN_SCALE_BAR_AND_PROTEIN_BAR:5,	// gap (in pixels) between scale bar and protein bar
};

/**
 * Draw the diagram
 */
circlePlotViewer.prototype.draw  = function(  ) {

	// our SVG object onto which we're drawing
	var svgRootSnapSVGObject = this.inializeSVGObject();
	
	// if this object is undefined or empty, we couldn't draw so do nothing
	if( !svgRootSnapSVGObject ) {
		return;
	}
	
	this.setViewerDimensions( svgRootSnapSVGObject );
	
	this.drawProteinBars( svgRootSnapSVGObject );
	
	if ( $( "input#show-scalebar" ).is( ':checked' ) ) {
		this.drawScaleBars( svgRootSnapSVGObject );
	}
	
	if ( $( "input#show-linkable-positions" ).is( ':checked' ) ) {
		this.drawLinkablePositions( svgRootSnapSVGObject );
	}
	
	if ( $( "input#show-self-crosslinks" ).is( ':checked' ) ) {
		this.drawSelfCrosslinks( svgRootSnapSVGObject );
	}

	if ( $( "input#show-crosslinks" ).is( ':checked' ) ) {
		this.drawCrosslinks( svgRootSnapSVGObject );
	}
	
	if ( $( "input#show-looplinks" ).is( ':checked' ) ) {
		this.drawLooplinks( svgRootSnapSVGObject );
	}
	
	if ( $( "input#show-monolinks" ).is( ':checked' ) ) {
		this.drawMonolinks( svgRootSnapSVGObject );
	}
	
	if ( $( "input#show-tryptic-cleavage-positions" ).is( ':checked' ) ) {
		this.drawTrypticPositions( svgRootSnapSVGObject );
	}
	
	
	
};



/**
 * Draw scale bar outside of each protein bar
 * 
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawScaleBars = function( svgRootSnapSVGObject ) {
	
	var center = this.getCenterCoords();
	var selectedProteins = getAllSelectedProteins();
	var degreesPerResidue = this.getDegreesPerResidue();
	
	// determine frequency and factor of tic marks
	var testFactors = [ 1, 5, 10, 25, 50, 100, 250, 500, 1000, 2000, 5000, 10000 ];
	
	var factor;				// number of residues per tick mark
	var degreesPerTick;		// number of degrees between tick marks
	
	for( var i = 0; i < testFactors.length; i++ ) {
		degreesPerTick = testFactors[ i ] * degreesPerResidue;
		
		if( degreesPerTick >= 5 ) {
			factor = testFactors[ i ];
			break;
		}
	}

	// loop over all proteins, draw their scale bars
	for( var i = 0; i < selectedProteins.length; i++ ) {

		var proteinId = selectedProteins[ i ];
		
		// group to hold the scale bar
		var group = svgRootSnapSVGObject.g();

		
		var startAngle = this.getAngleForProteinPosition( i, 1 );
		var endAngle = this.getAngleForProteinPosition( i, _proteinLengths[ proteinId ] );
		
		
		
		
		// draw base line of scale bar
		var radius = this.radius;
		radius -= this.CONSTANTS._SCALE_BAR_FONT_HEIGHT;
		radius -= this.CONSTANTS._GAP_BETWEEN_SCALE_BAR_AND_TEXT;
		radius -= this.CONSTANTS._SCALE_BAR_HEIGHT / 2;
		
		var startPoint = this.polarToCartesian( center.x, center.y, radius , startAngle)
		var endPoint = this.polarToCartesian( center.x, center.y, radius , endAngle)
		
		
	    var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";
	    
	    var d = [
	             "M", endPoint.x, endPoint.y, 
	             "A", radius, radius, 0, arcSweep, 0, startPoint.x, startPoint.y
	         ].join(" ");
		
		var path = svgRootSnapSVGObject.path( d );
		path.attr({
			stroke:this.getColorForIndex( i ),
			fill:"none"
		});
		
		group.add( path );
		
		
		
		// draw start tick
		var radius1 = this.radius;
		radius1 -= this.CONSTANTS._SCALE_BAR_FONT_HEIGHT;
		radius1 -= this.CONSTANTS._GAP_BETWEEN_SCALE_BAR_AND_TEXT;
		
		var radius2 = radius1 - this.CONSTANTS._SCALE_BAR_HEIGHT;
		
		startPoint = this.polarToCartesian( center.x, center.y, radius1 , startAngle)
		endPoint = this.polarToCartesian( center.x, center.y, radius2 , startAngle)
		
		d = [
             "M", startPoint.x, startPoint.y, 
             "L", endPoint.x, endPoint.y
         ].join(" ");
		
		path = svgRootSnapSVGObject.path( d );
		path.attr({
			stroke:this.getColorForIndex( i ),
			fill:"none"
		});
		group.add( path );
		
		
		
		// draw end tick
		startPoint = this.polarToCartesian( center.x, center.y, radius1 , endAngle)
		endPoint = this.polarToCartesian( center.x, center.y, radius2 , endAngle)
		
		d = [
             "M", startPoint.x, startPoint.y, 
             "L", endPoint.x, endPoint.y
         ].join(" ");
		
		path = svgRootSnapSVGObject.path( d );
		path.attr({
			stroke:this.getColorForIndex( i ),
			fill:"none"
		});
		group.add( path );
		
		

		// draw interval ticks
		for( var k = factor; k < _proteinLengths[ proteinId ]; k += factor ) {
			
			var angle = startAngle + ( k * degreesPerResidue );
			
			startPoint = this.polarToCartesian( center.x, center.y, radius1 , angle)
			endPoint = this.polarToCartesian( center.x, center.y, radius2 , angle)
			
			d = [
	             "M", startPoint.x, startPoint.y, 
	             "L", endPoint.x, endPoint.y
	         ].join(" ");
			
			path = svgRootSnapSVGObject.path( d );
			path.attr({
				stroke:this.getColorForIndex( i ),
				fill:"none"
			});
			group.add( path );
			
			
		}
		
		// draw labels, start with 2nd tick and label every other tick
		// draw interval ticks
		
		radius = this.radius;
		radius -= this.CONSTANTS._SCALE_BAR_FONT_HEIGHT;
		
		for( var k = factor; k < _proteinLengths[ proteinId ]; k += factor * 2 ) {
			
			// create the path on which we are drawing our label
			
			angle = startAngle + ( k * degreesPerResidue );
			
			var startAdjustment = 0.5;
			var endAdjustment = 1;
			
			if( k >= 10 ) {
				startAdjustment = 1;
				endAdjustment = 2;
			}
			if( k >= 100 ) {
				var startAdjustment = 1.5;
				var endAdjustment = 2;
			}
			if( k > 1000 ) {
				var startAdjustment = 2;
				var endAdjustment = 3;
			}
			
			startPoint = this.polarToCartesian( center.x, center.y, radius , angle - startAdjustment);
			endPoint = this.polarToCartesian( center.x, center.y, radius , angle + endAdjustment );
			
			d = [
	             "M", startPoint.x, startPoint.y, 
	             "A", radius, radius, 0, 0, 1, endPoint.x, endPoint.y
	         ].join(" ");
		
			var path = svgRootSnapSVGObject.path( d );
			path.attr({
				stroke:"none",
				fill:"none",
				"startOffset":"50%",
			});
		
			group.add( path );
			
			var text = svgRootSnapSVGObject.text( 0, 0, k + "" );
			
			text.attr( {
				"textpath" : path,
				"font-size": this.CONSTANTS._SCALE_BAR_FONT_HEIGHT + "px",
				stroke:this.getColorForIndex( i ),
				fill:"none",
				"stroke-opacity":"0.9",
			});
			
			group.add( text );

		}
		
		
	}//end looping over proteins
};



/**
 * Draw all tryptic positions in all proteins
 * 
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawTrypticPositions = function( svgRootSnapSVGObject ) {
	var selectedProteins = getAllSelectedProteins();
	var radius = this.getProteinBarRadii();
	var center = this.getCenterCoords();
	
	for( var i = 0; i < selectedProteins.length; i++ ) {
		var proteinId = selectedProteins[ i ];
		
		var positions = _proteinSequenceTrypsinCutPoints[ proteinId ];
		if( positions != undefined ) {
			
			for( var p = 0; p < positions.length; p++ ) {
				var angle = this.getAngleForProteinPosition( i, positions[ p ] );
				var start = this.polarToCartesian( center.x, center.y, radius.inner, angle );
				var end   = this.polarToCartesian( center.x, center.y, radius.outer, angle );
				
				var pathString = "M" + start.x + "," + start.y;
				pathString +=    "L" + end.x + "," + end.y;
				
				var path = svgRootSnapSVGObject.path( pathString );
				
				path.attr( {
					stroke:this.CONSTANTS._LINKABLE_POSITION_COLOR,
					"stroke-opacity":this.CONSTANTS._LINKABLE_POSITION_OPACITY,
					"stroke-dasharray":"4,2"
				})
			}
			
		}
	}
};

/**
 * Draw all linkable positions in all proteins
 * 
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawLinkablePositions = function( svgRootSnapSVGObject ) {
	var selectedProteins = getAllSelectedProteins();
	var radius = this.getProteinBarRadii();
	var center = this.getCenterCoords();
	
	for( var i = 0; i < selectedProteins.length; i++ ) {
		var proteinId = selectedProteins[ i ];
		
		var positions = _linkablePositions[ proteinId ];
		if( positions != undefined ) {
			
			for( var p = 0; p < positions.length; p++ ) {
				var angle = this.getAngleForProteinPosition( i, positions[ p ] );
				var start = this.polarToCartesian( center.x, center.y, radius.inner, angle );
				var end   = this.polarToCartesian( center.x, center.y, radius.outer, angle );
				
				var pathString = "M" + start.x + "," + start.y;
				pathString +=    "L" + end.x + "," + end.y;
				
				var path = svgRootSnapSVGObject.path( pathString );
				
				path.attr( {
					stroke:this.CONSTANTS._LINKABLE_POSITION_COLOR,
					"stroke-opacity":this.CONSTANTS._LINKABLE_POSITION_OPACITY
				})
			}
			
		}
	}
};

/**
 * Draw all monolinks
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawMonolinks = function( svgRootSnapSVGObject ) {
	var selectedProteins = getAllSelectedProteins();
	
	for( var i = 0; i < selectedProteins.length; i++ ) {
		var proteinId = selectedProteins[ i ];
		
		if ( _proteinMonolinkPositions[ proteinId ] == undefined ) { 
			continue; //  skip processing this selected protein 
		}

		var positions = Object.keys( _proteinMonolinkPositions[ selectedProteins[ i ] ] );
		for( var k = 0; k < positions.length; k++ ) {
			var position = positions[ k ];
			
			var link = { };
			link.type = "monolink";
			link.protein1 = proteinId;
			link.position1 = position;
			
			
			this.drawMonolink( i, link, svgRootSnapSVGObject );
		}
	
	}
};

/**
 * Draw a monolink
 * @param index
 * @param link
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawMonolink = function( index, link, svgRootSnapSVGObject ) {
	
	var angle = this.getAngleForProteinPosition( index, link.position1 );
	var center = this.getCenterCoords();
	
	var radius1 = this.getProteinBarRadii().inner - this.CONSTANTS._GAP_BETWEEN_BARS_AND_LINKS;
	var radius2 = radius1 - this.CONSTANTS._MONOLINK_HEIGHT;
	
	var point1 = this.polarToCartesian( center.x, center.y, radius1, angle );
	var point2 = this.polarToCartesian( center.x, center.y, radius2, angle );
	
	var pathString = "M" + point1.x + "," + point1.y;
	pathString += "L" + point2.x + "," + point2.y;
	
	var path = svgRootSnapSVGObject.path( pathString );
	
	var opacity = getOpacityForIndexAndLink( index, link );
	
    path.attr( {
		stroke:this.getColorForIndex( index ),
		"stroke-dasharray":"4,2",
		fill:"none",
		"stroke-opacity":opacity,
	});
}


/**
 * Draw all looplinks
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawLooplinks = function( svgRootSnapSVGObject ) {
	var selectedProteins = getAllSelectedProteins();
	
	for( var i = 0; i < selectedProteins.length; i++ ) {
		var proteinId = selectedProteins[ i ];
		
		var drawnLinks = { };
		
		if ( _proteinLooplinkPositions[ proteinId ] == undefined ) { continue; }
		if ( _proteinLooplinkPositions[ proteinId ][ proteinId ] == undefined ) { continue; }
		
		var fromPositions = Object.keys( _proteinLooplinkPositions[ proteinId ][ proteinId ] );
		
		for( var fromPositionIndex = 0; fromPositionIndex < fromPositions.length; fromPositionIndex++ ) {
			var fromPosition = parseInt( fromPositions[ fromPositionIndex ] );
			
			var toPositions = Object.keys( _proteinLooplinkPositions[ proteinId ][ proteinId ][ fromPosition ] );
			
			for( var toPositionIndex = 0; toPositionIndex < toPositions.length; toPositionIndex++ ) {
				var toPosition = parseInt( toPositions [ toPositionIndex ] );
				
				// prevent drawing the same links twice
				var tmp;
				if( toPosition > fromPosition ) {
					tmp = toPosition;
					toPosition = fromPosition;
					fromPosition = tmp;
				}
				
				var key = toPosition + "-" + fromPosition;
				if( key in drawnLinks ) { continue; }
				else { drawnLinks[ key ] = 1; }
				
				
				var link = { };
				link.type = "looplink",
				link.protein1 = proteinId;
				link.position1 = toPosition;
				link.position2 = fromPosition;
				
				var looplink = this.drawCrosslink( i, fromPosition, i, toPosition, link, svgRootSnapSVGObject );
				looplink.attr( { "stroke-dasharray":"4,2" });
			}
			
		}		
	}
};


/**
 * Draw all inter-protein crosslinks
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawCrosslinks = function( svgRootSnapSVGObject ) {
	var selectedProteins = getAllSelectedProteins();
	
	for( var i = 0; i < selectedProteins.length; i++ ) {
		var proteinId1 = selectedProteins[ i ];
		
		if ( _proteinLinkPositions[ proteinId1 ] == undefined ) { continue; }
		
		for( var j = 0; j < selectedProteins.length; j++ ) {
			if( j <= i ) { continue; }
			
			var proteinId2 = selectedProteins[ j ]
			
			if ( _proteinLinkPositions[ proteinId1 ][ proteinId2 ] == undefined ) { continue; }
			
			var fromPositions = Object.keys( _proteinLinkPositions[ proteinId1 ][ proteinId2 ] );
			
			for( var fromPositionIndex = 0; fromPositionIndex < fromPositions.length; fromPositionIndex++ ) {
				var fromPosition = parseInt( fromPositions[ fromPositionIndex ] );
				
				var toPositions = Object.keys( _proteinLinkPositions[ proteinId1 ][ proteinId2 ][ fromPosition ] );
				
				for( var toPositionIndex = 0; toPositionIndex < toPositions.length; toPositionIndex++ ) {
					var toPosition = parseInt( toPositions [ toPositionIndex ] );

					var link = { };
					link.type = "crosslink";
					link.protein1 = proteinId1;
					link.position1 = fromPosition;
					link.protein2 = proteinId2;
					link.position2 = toPosition;
					
					this.drawCrosslink( i, fromPosition, j, toPosition, link, svgRootSnapSVGObject );				
				}
				
			}
			
			
		}
	}
	
}


/**
 * Draw all the self-crosslinks
 * 
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawSelfCrosslinks = function( svgRootSnapSVGObject ) {
	var selectedProteins = getAllSelectedProteins();
		
	for( var i = 0; i < selectedProteins.length; i++ ) {
		var proteinId = selectedProteins[ i ];
				
		var drawnLinks = { };
		
		if ( _proteinLinkPositions[ proteinId ] == undefined ) { continue; }
		if ( _proteinLinkPositions[ proteinId ][ proteinId ] == undefined ) { continue; }
		
		
		
		var fromPositions = Object.keys( _proteinLinkPositions[ proteinId ][ proteinId ] );		
		
		for( var fromPositionIndex = 0; fromPositionIndex < fromPositions.length; fromPositionIndex++ ) {
			var fromPosition = parseInt( fromPositions[ fromPositionIndex ] );
						
			var toPositions = Object.keys( _proteinLinkPositions[ proteinId ][ proteinId ][ fromPosition ] );
						
			for( var toPositionIndex = 0; toPositionIndex < toPositions.length; toPositionIndex++ ) {
				var toPosition = parseInt( toPositions [ toPositionIndex ] );
								
				// prevent drawing the same links twice
				var key;
				if( toPosition >= fromPosition ) {
					key = toPosition + "-" + fromPosition;
				} else {
					key = fromPosition + "-" + toPosition;
				}
				
				if( key in drawnLinks ) { continue; }
				else { drawnLinks[ key ] = 1; }
				
				var link = { };
				link.type = "crosslink",
				link.protein1 = proteinId;
				link.protein2 = proteinId;
				link.position1 = fromPosition;
				link.position2 = toPosition;
				
				this.drawCrosslink( i, fromPosition, i, toPosition, link, svgRootSnapSVGObject );				
			}
			
		}		
	}
};


/**
 * Draw a cross link on the SVG object
 * @param fromIndex
 * @param fromPosition
 * @param toIndex
 * @param toPosition
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawCrosslink = function( fromIndex, fromPosition, toIndex, toPosition, link, svgRootSnapSVGObject ) {
	
	var center = this.getCenterCoords();
	
	var fromCoords = this.getCrosslinkTerminus( fromIndex, fromPosition );
	var toCoords = this.getCrosslinkTerminus( toIndex, toPosition );

	var fromDegrees = this.getAngleForProteinPosition( fromIndex, fromPosition );
	var toDegrees = this.getAngleForProteinPosition( toIndex, toPosition );
			
	var diffAngle = Math.abs( fromDegrees - toDegrees );
	if( diffAngle > 180 ) { diffAngle = 360 - diffAngle; }
	
	var midAngle;
	if( Math.abs( fromDegrees - toDegrees ) <= 180 ) {
		midAngle = ( fromDegrees + toDegrees ) / 2;
	} else {
		midAngle = ( fromDegrees + toDegrees ) / 2;
		if( midAngle < 180 ) { midAngle += 180; }
		else { midAngle -= 180; }
	}
	
	var ratio = diffAngle / 180;
	
	var radius = this.getProteinBarRadii().inner - this.CONSTANTS._GAP_BETWEEN_BARS_AND_LINKS - this.CONSTANTS._MINIMUM_CROSSLINK_LOOP_HEIGHT;
	radius -= radius * ratio;
		
	var loopPeakCoordinates = this.polarToCartesian( center.x, center.y, radius, midAngle );
	
	var opacity = getOpacityForIndexAndLink( fromIndex, link );
	
    var pathString = "M" + fromCoords.x + "," + fromCoords.y;
    pathString += "Q" + loopPeakCoordinates.x + "," + loopPeakCoordinates.y;
    pathString += " " + toCoords.x + "," + toCoords.y;
        
    var path = svgRootSnapSVGObject.path( pathString );
    path.attr( {
		stroke:this.getColorForIndex( fromIndex ),
		fill:"none",
		"stroke-opacity":opacity,
	});
    
    return path;
}



/**
 * Given an index among selectedProteins and a position in that protein, find the X,Y coordinate for where to
 * terminate (start or end) a crosslink for that protein and position
 * 
 * @param proteinIndex
 * @param position
 * @returns
 */
circlePlotViewer.prototype.getCrosslinkTerminus = function( proteinIndex, position ) {
	var selectedProteins = getAllSelectedProteins();
	
	var center = this.getCenterCoords();
	
	var totalGapDegrees = selectedProteins.length * this.CONSTANTS._GAP_BETWEEN_BARS;
	var workingDegrees = 360 - totalGapDegrees;
	
	var totalProteinLength = 0;
	for( var i = 0; i < selectedProteins.length; i++ ) {
		totalProteinLength += _proteinLengths[ selectedProteins[ i ] ];
	}
	
	// number of degrees per protein residue
	var degreesPerResidue = workingDegrees / totalProteinLength;
	
	var currentStartDegrees = 0;
	for( var i = 0; i < selectedProteins.length; i++ ) {
		
		
		if( i == proteinIndex ) {

			var degrees = currentStartDegrees + (position * degreesPerResidue - (0.5 * degreesPerResidue ) );
			
			return this.polarToCartesian( center.x,
										  center.y,
										  this.getProteinBarRadii().inner - this.CONSTANTS._GAP_BETWEEN_BARS_AND_LINKS,
										  degrees
										 );
			 

		} else {
			var totalLengthProportion = _proteinLengths[ selectedProteins[ i ] ] / totalProteinLength;
			var rotationDegrees = workingDegrees * totalLengthProportion;
			currentStartDegrees += this.CONSTANTS._GAP_BETWEEN_BARS + rotationDegrees;
		}
		
	}
	
};

/**
 * Find the degrees of rotation to the given protein's position
 * @param proteinIndex
 * @param position
 * @returns {Number}
 */
circlePlotViewer.prototype.getAngleForProteinPosition = function( proteinIndex, position ) {
	var selectedProteins = getAllSelectedProteins();
	
	var totalGapDegrees = selectedProteins.length * this.CONSTANTS._GAP_BETWEEN_BARS;
	var workingDegrees = 360 - totalGapDegrees;
	
	var totalProteinLength = 0;
	for( var i = 0; i < selectedProteins.length; i++ ) {
		totalProteinLength += _proteinLengths[ selectedProteins[ i ] ];
	}
	
	// number of degrees per protein residue
	var degreesPerResidue = workingDegrees / totalProteinLength;
	
	var currentStartDegrees = 0;
	for( var i = 0; i < selectedProteins.length; i++ ) {
		
		
		if( i == proteinIndex ) {

			var degrees = currentStartDegrees + (position * degreesPerResidue - (0.5 * degreesPerResidue ) );
			
			return degrees;

		} else {
			var totalLengthProportion = _proteinLengths[ selectedProteins[ i ] ] / totalProteinLength;
			var rotationDegrees = workingDegrees * totalLengthProportion;
			currentStartDegrees += this.CONSTANTS._GAP_BETWEEN_BARS + rotationDegrees;
		}
		
	}
};



/**
 * Draw all the protein bars, and associated items (such as labels, linkable positions, and scale bars)
 * @param svgRootSnapSVGObject
 */
circlePlotViewer.prototype.drawProteinBars = function( svgRootSnapSVGObject ) {
	
	var selectedProteins = getAllSelectedProteins();
	var totalGapDegrees = selectedProteins.length * this.CONSTANTS._GAP_BETWEEN_BARS;
	var workingDegrees = 360 - totalGapDegrees;
	
	var totalProteinLength = 0;
	for( var i = 0; i < selectedProteins.length; i++ ) {
		totalProteinLength += _proteinLengths[ selectedProteins[ i ] ];
	}
	
	var currentStartDegrees = 0;
	for( var i = 0; i < selectedProteins.length; i++ ) {
		var totalLengthProportion = _proteinLengths[ selectedProteins[ i ] ] / totalProteinLength;
		var rotationDegrees = workingDegrees * totalLengthProportion;
		
		this.drawProteinBar( svgRootSnapSVGObject, selectedProteins[ i ], i, currentStartDegrees, rotationDegrees );
		
		currentStartDegrees += this.CONSTANTS._GAP_BETWEEN_BARS + rotationDegrees;
		
	}
	
};



/**
 * Draw the bar for a given protein
 * @param svgRootSnapSVGObject
 * @param proteinId
 * @param startDegrees
 * @param rotationDegrees
 */
circlePlotViewer.prototype.drawProteinBar = function( svgRootSnapSVGObject, proteinId, index, startDegrees, rotationDegrees ) {
	
	var center = this.getCenterCoords();
	
	var path = this.getCurvedBarPath( center.x, 
									  center.y,
									  startDegrees,
									  startDegrees + rotationDegrees
									 );
	
	var svgPath = svgRootSnapSVGObject.path( path );
	
	svgPath.attr( {
		fill:this.getColorForIndex( index ),
	})
	
	//this.drawProteinBarLabel( svgRootSnapSVGObject, svgPath, proteinId );
	this.drawProteinBarLabel( svgRootSnapSVGObject, proteinId, index, startDegrees, rotationDegrees );
}

/**
 * Draw the label for a given protein bar
 * @param svgRootSnapSVGObject
 * @param svgPath
 * @param proteinId
 */
circlePlotViewer.prototype.drawProteinBarLabel = function( svgRootSnapSVGObject, proteinId, index, startDegrees, rotationDegrees ) {
	
	var center = this.getCenterCoords();
	
	var path = this.getCurvedPathForLabel( center.x, 
									  center.y,
									  startDegrees,
									  startDegrees + rotationDegrees
									 );
	
	var svgPath = svgRootSnapSVGObject.path( path );
	
	svgPath.attr( {
		fill:"none",
		stroke:"none",
	})
	
	var proteinLabelTextSnapSVGObject = svgRootSnapSVGObject.text( 0, 0, _proteinNames[ proteinId ] );
	
	proteinLabelTextSnapSVGObject.attr({
		fill:"#ffffff",
		"font-size":"14px",
		textpath: svgPath,
		dy:"-15px",
		dx:"5px",
	});
}

/*
circlePlotViewer.prototype.drawProteinBarLabel = function( svgRootSnapSVGObject, svgPath, proteinId ) {
	
	var proteinLabelTextSnapSVGObject = svgRootSnapSVGObject.text( 0, 0, _proteinNames[ proteinId ] );
	
	proteinLabelTextSnapSVGObject.attr({
		fill:"#ffffff",
		"font-size":"14px",
		textpath: svgPath,
		dy:"-15px",
		dx:"5px",
	});
	
};
*/

circlePlotViewer.prototype.inializeSVGObject = function() {

	var svgRootSnapSVGObject;  // the Snap SVG object for the SVG

	
	//  First remove and add the "<svg>" element from the template
	
	var $svg_image_template_div = $("#svg_image_template_div");
	var svg_image_template_div__html = $svg_image_template_div.html();
	var $svg_image_inner_container_div = $("#svg_image_inner_container_div");
	
	//  select the <svg> element
	var $merged_image_svg_jq__BeforeEmpty = $svg_image_inner_container_div.find("svg.merged_image_svg_jq");
	
	$svg_image_inner_container_div.empty();  //  remove the <svg> element.  jQuery will also properly remove all the handlers attached to those elements
	$svg_image_inner_container_div.html( svg_image_template_div__html );  // insert new <svg> as copied from the template
	
	//  select the <svg> element
	var $merged_image_svg_jq = $svg_image_inner_container_div.find("svg.merged_image_svg_jq");
	
	//  get the <svg> HTML element to pass to Snap
	var merged_image_svg_element = $merged_image_svg_jq[0];
	
	var selectedProteins = getAllSelectedProteins();
	
	if ( selectedProteins === undefined || selectedProteins.length < 1 ) { 
		
		//  No proteins so remove CSS height attribute
		$svg_image_inner_container_div.css( { height : '' } );	

		return;  //  EARLY EXIT of function if no proteins to display 
	}
		
	svgRootSnapSVGObject = Snap( merged_image_svg_element );  // pass in HTML element
		
	if ( _proteins.length < 1 ) {
		
		//  No Proteins Data loaded
		var newHeightContainingDivEmpty = 4;
		$svg_image_inner_container_div.css( { height : newHeightContainingDivEmpty } );
		return;
	}
	
	return svgRootSnapSVGObject;
	
};



circlePlotViewer.prototype.setViewerDimensions = function(svgRootSnapSVGObject) {
		
	svgRootSnapSVGObject.attr( { width: this.CONSTANTS._DEFAULT_VIEWPORT_WIDTH } );
	svgRootSnapSVGObject.attr( { height: this.CONSTANTS._DEFAULT_VIEWPORT_HEIGHT } );
	
	this.centerX = this.CONSTANTS._DEFAULT_VIEWPORT_WIDTH / 2;
	this.centerY = this.centerX;
	this.radius = this.centerX;

};

/**
 * Find the coordinates of the point corresponding to rotating the given number of degrees
 * (0 being straight up) along a circle of the given radius and center. This method was borrowed
 * from StackOverflow at:
 * http://stackoverflow.com/questions/5736398/how-to-calculate-the-svg-path-for-an-arc-of-a-circle
 * 
 * @param centerX
 * @param centerY
 * @param radius
 * @param angleInDegrees
 * @returns {___anonymous3091_3208}
 */
circlePlotViewer.prototype.polarToCartesian = function(centerX, centerY, radius, angleInDegrees) {
	
	var angleInRadians = (angleInDegrees-90) * Math.PI / 180.0;

	  return {
	    x: centerX + (radius * Math.cos(angleInRadians)),
	    y: centerY + (radius * Math.sin(angleInRadians))
	  };
};


circlePlotViewer.prototype.getCurvedPathForLabel = function(centerX, centerY, startAngle, endAngle){
	
	var radii = this.getProteinBarRadii();
	
	var outerRadius = radii.outer;
	var innerRadius = radii.inner;
	
    var outerStart = this.polarToCartesian(centerX, centerY, outerRadius, endAngle);
    var outerEnd = this.polarToCartesian(centerX, centerY, outerRadius, startAngle);

    var innerStart = this.polarToCartesian(centerX, centerY, innerRadius, endAngle);
    var innerEnd = this.polarToCartesian(centerX, centerY, innerRadius, startAngle);
    
    var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";
    
    var d = [
             "M", innerEnd.x, innerEnd.y, 
             "A", innerRadius, innerRadius, 0, arcSweep, 1, innerStart.x, innerStart.y
         ].join(" ");
    
    return d + "Z";       
}


/**
 * Get the string defining the SVG path for a curved protein bar
 * 
 * @param centerX The center of the diagram
 * @param centerY The center of the diagram
 * @param outerRadius The outer radius of the protein bar
 * @param barHeight The height of the protein bar
 * @param startAngle The start angle in degress (0 is straight up)
 * @param endAngle The end angle in degrees
 * @returns {String}
 */
circlePlotViewer.prototype.getCurvedBarPath = function(centerX, centerY, startAngle, endAngle){
	
	var radii = this.getProteinBarRadii();
	
	var outerRadius = radii.outer;
	var innerRadius = radii.inner;
	
    var outerStart = this.polarToCartesian(centerX, centerY, outerRadius, endAngle);
    var outerEnd = this.polarToCartesian(centerX, centerY, outerRadius, startAngle);

    var innerStart = this.polarToCartesian(centerX, centerY, innerRadius, endAngle);
    var innerEnd = this.polarToCartesian(centerX, centerY, innerRadius, startAngle);
    
    var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";
    
    var d = [
             "M", innerEnd.x, innerEnd.y, 
             "A", innerRadius, innerRadius, 0, arcSweep, 1, innerStart.x, innerStart.y,
             "L", outerStart.x, outerStart.y,
             "A", outerRadius, outerRadius, 0, arcSweep, 0, outerEnd.x, outerEnd.y,
             "L", innerEnd.x, innerEnd.y
         ].join(" ");
    
    /*
    var d = [
        "M", outerStart.x, outerStart.y, 
        "A", outerRadius, outerRadius, 0, arcSweep, 0, outerEnd.x, outerEnd.y,
        "L", innerEnd.x, innerEnd.y,
        "A", innerRadius, innerRadius, 0, arcSweep, 1, innerStart.x, innerStart.y,
        "L", outerStart.x, outerStart.y
    ].join(" ");
	*/
    
    return d + "Z";       
}

/**
 * Get the radii to use for protein bars
 * 
 * @returns { outer: outer radius, inner: inner radius
 */
circlePlotViewer.prototype.getProteinBarRadii = function() {
	
	var radii = { };
	
	radii.outer = this.radius;
	
	if( this.isScaleBarSelected() ) {
		radii.outer -= ( this.CONSTANTS._SCALE_BAR_HEIGHT + 
						 this.CONSTANTS._SCALE_BAR_FONT_HEIGHT +
						 this.CONSTANTS._GAP_BETWEEN_SCALE_BAR_AND_TEXT +
						 this.CONSTANTS._GAP_BETWEEN_SCALE_BAR_AND_PROTEIN_BAR );
	}
	
	radii.inner = radii.outer - this.CONSTANTS._HEIGHT_OF_PROTEIN_BARS;
	
	return radii;
};

/**
 * Get the center of the circle
 * @returns { x: x coord of center, y: y coord of center }
 */
circlePlotViewer.prototype.getCenterCoords = function() {
	
	var center = { };
	
	center.x = this.centerX;
	center.y = this.centerY;
	
	return center;
};

/**
 * Get the color for the given index, given the total number of proteins
 * @param index
 * @returns
 */
circlePlotViewer.prototype.getColorForIndex = function( index ) {
	var number = getAllSelectedProteins().length;
		
	var hueDivider = 360 / number;
	var saturation = 0.39;
	var brightness = 0.60;

	var hue =  (360 - (hueDivider * index ) - 1) / 360;
		
	var color = Snap.hsb2rgb( hue, saturation, brightness );
	return color.hex;
};

/**
 * Get distance between two Cartesian points
 * @param fromCoords
 * @param toCoords
 * @returns
 */
circlePlotViewer.prototype.getDistanceBetweenPoints = function( fromCoords, toCoords ) {
	return Math.sqrt( Math.pow( fromCoords.x - toCoords.x, 2) + Math.pow( fromCoords.x - toCoords.x, 2 ) );
};

/**
 * Maximum possible drawn crosslink distance
 */
circlePlotViewer.prototype.getMaxCrosslinkDistance = function( ) {
	reutrn ( this.getProteinBarRadii().inner - this.CONSTANTS._GAP_BETWEEN_BARS_AND_LINKS ) * 2;
};

/**
 * Whether or not the scale bars are enabled
 * @returns {Boolean}
 */
circlePlotViewer.prototype.isScaleBarSelected  = function() {
	if ( $( "input#show-scalebar" ).is( ':checked' ) ) {
		return true;
	}
	
	return false;
};

/**
 * Get the number of degrees that represents a single residue in the graphic
 * @returns {Number}
 */
circlePlotViewer.prototype.getDegreesPerResidue = function() {
	var selectedProteins = getAllSelectedProteins();
	
	var totalGapDegrees = selectedProteins.length * this.CONSTANTS._GAP_BETWEEN_BARS;
	var workingDegrees = 360 - totalGapDegrees;
	
	var totalProteinLength = 0;
	for( var i = 0; i < selectedProteins.length; i++ ) {
		totalProteinLength += _proteinLengths[ selectedProteins[ i ] ];
	}
	
	// number of degrees per protein residue
	return workingDegrees / totalProteinLength;
};

