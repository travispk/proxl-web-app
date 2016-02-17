package org.yeastrc.xlink.www.objects;

import java.util.List;

/**
 * One entry in the map of proteins per search for the Merged Protein Pages and Image and Structure
 *
 */
public class ProteinCommonDataForPerSearchIdsProteinIdsPositionsResultEntry {


	private List<AnnotationDisplayNameDescription> peptideAnnotationDisplayNameDescriptionList;

	private List<AnnotationDisplayNameDescription> psmAnnotationDisplayNameDescriptionList;


	List<ProteinCommonDataForPerSearchIdsProteinIdsPositionsResult>  proteins;
	
	
	
	
	public List<AnnotationDisplayNameDescription> getPeptideAnnotationDisplayNameDescriptionList() {
		return peptideAnnotationDisplayNameDescriptionList;
	}

	public void setPeptideAnnotationDisplayNameDescriptionList(
			List<AnnotationDisplayNameDescription> peptideAnnotationDisplayNameDescriptionList) {
		this.peptideAnnotationDisplayNameDescriptionList = peptideAnnotationDisplayNameDescriptionList;
	}

	public List<AnnotationDisplayNameDescription> getPsmAnnotationDisplayNameDescriptionList() {
		return psmAnnotationDisplayNameDescriptionList;
	}

	public void setPsmAnnotationDisplayNameDescriptionList(
			List<AnnotationDisplayNameDescription> psmAnnotationDisplayNameDescriptionList) {
		this.psmAnnotationDisplayNameDescriptionList = psmAnnotationDisplayNameDescriptionList;
	}
	

	public List<ProteinCommonDataForPerSearchIdsProteinIdsPositionsResult> getProteins() {
		return proteins;
	}

	public void setProteins(
			List<ProteinCommonDataForPerSearchIdsProteinIdsPositionsResult> proteins) {
		this.proteins = proteins;
	}


	
}
