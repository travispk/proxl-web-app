package org.yeastrc.xlink.www.protein_coverage;

import java.util.Set;

import org.yeastrc.xlink.www.objects.ProteinSequenceObject;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class ProteinSequenceCoverage {

	public ProteinSequenceCoverage( ProteinSequenceObject protein ) {
		this.protein = protein;
	}
	
	
	/**
	 * Added the supplied peptide sequence to the peptides being used to determine
	 * this protein's sequence coverage
	 * @param sequence
	 * @throws Exception
	 */
	public void addPeptide( String peptideSequence ) throws Exception {
		
		// find all locations that this peptide maps onto the sequence and add those ranges to the RangeSet
		if( this.ranges == null )
			this.ranges = TreeRangeSet.create();
		
        // iterate over all matches of the peptide sequence in the protein sequence
        for (int i = -1; (i = this.protein.getSequence().indexOf(peptideSequence, i + 1)) != -1; ) {
        	Range<Integer> r = Range.closed( i + 1, i + peptideSequence.length() );
        	this.ranges.add( r );
        }
		
	}
	
	/**
	 * Add the supplied start and end coordinates as a sequence coverage range
	 * @param start
	 * @param end
	 * @throws Exception
	 */
	public void addStartEndBoundary( int start, int end ) throws Exception {

		if( this.ranges == null )
			this.ranges = TreeRangeSet.create();
		
		Range<Integer> r = Range.closed( start, end );
		this.ranges.add( r );
		
	}
	
	/**
	 * Add another protein sequence coverage object's ranges to this one's
	 * 
	 * @param coverageToAdd
	 */
	public void addSequenceCoverageObject( ProteinSequenceCoverage coverageToAdd ) throws Exception {
		
		if( this.ranges == null )
			this.ranges = TreeRangeSet.create();
		
		if( this.getProtein().getProteinSequenceId() != coverageToAdd.getProtein().getProteinSequenceId() )
			throw new Exception( "Attempted to add two coverage objects that do not describe the same protein." );
		
		if( coverageToAdd.getRanges() == null )
			return;
		
		
		for( Range<Integer> r : coverageToAdd.getRanges() ) {
			this.ranges.add( r );
		}
		
	}
	
	/**
	 * Get the ranges of this protein's sequence that are covered by the
	 * peptides that have been added
	 * @return
	 */
	public Set<Range<Integer>> getRanges() {
		
		if( this.ranges == null )
			this.ranges = TreeRangeSet.create();
		
		return ranges.asRanges();
	}

	/**
	 * Get the sequence coverage of this protein given the peptides that have
	 * been added
	 * @return
	 */
	public Double getSequenceCoverage() throws Exception {
		int totalResidues = 0;

		if( this.ranges == null )
			this.ranges = TreeRangeSet.create();
		
		for( Range<Integer> r : this.ranges.asRanges() ) {
			totalResidues += r.upperEndpoint() - r.lowerEndpoint() + 1;
		}
		
		return (double)totalResidues / (double)this.getProtein().getSequence().length();
	}
	
	/**
	 * Get the protein covered by this sequence coverage
	 * @return
	 */
	public ProteinSequenceObject getProtein() {
		return protein;
	}


	private final ProteinSequenceObject protein;
	private RangeSet<Integer> ranges;
}
