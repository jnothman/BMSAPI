package org.generationcp.ibpworkbench.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.util.TraitsAndMeansCSVUtil2;
import org.generationcp.middleware.v2.domain.DataSet;
import org.generationcp.middleware.v2.domain.DataSetType;
import org.generationcp.middleware.v2.domain.DatasetReference;
import org.generationcp.middleware.v2.domain.DatasetValues;
import org.generationcp.middleware.v2.domain.ExperimentType;
import org.generationcp.middleware.v2.domain.ExperimentValues;
import org.generationcp.middleware.v2.domain.FactorType;
import org.generationcp.middleware.v2.domain.StandardVariable;
import org.generationcp.middleware.v2.domain.Stock;
import org.generationcp.middleware.v2.domain.Stocks;
import org.generationcp.middleware.v2.domain.Term;
import org.generationcp.middleware.v2.domain.TermId;
import org.generationcp.middleware.v2.domain.TrialEnvironment;
import org.generationcp.middleware.v2.domain.TrialEnvironments;
import org.generationcp.middleware.v2.domain.Variable;
import org.generationcp.middleware.v2.domain.VariableList;
import org.generationcp.middleware.v2.domain.VariableType;
import org.generationcp.middleware.v2.domain.VariableTypeList;
import org.generationcp.middleware.v2.manager.OntologyDataManagerImpl;
import org.generationcp.middleware.v2.manager.StudyDataManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.rits.cloning.Cloner;

/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

public class BreedingViewServiceImpl implements BreedingViewService {
    @Autowired
    private TraitsAndMeansCSVUtil2 traitsAndMeansCSVUtil2;
    @Autowired
    private StudyDataManagerImpl studyDataManagerV2;
    @Autowired
    private OntologyDataManagerImpl ontologyDataManagerV2;
    @Autowired
    private Cloner cloner;
    
    private boolean meansDataSetExists = false;

    public void execute(Map<String, String> params, List<String> errors) throws Exception {
        String fileName = params.get(WebAPIConstants.FILENAME.getParamValue());
        String workbenchProjectId = params.get(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue());
        Map<String, ArrayList<String>> traitsAndMeans = traitsAndMeansCSVUtil2.csvToMap(fileName);
        System.out.println("Traits and Means: " + traitsAndMeans);

        int ndLocationId;

        //call middleware api and save
        if(!traitsAndMeans.isEmpty()) {
        	DataSet meansDataSet = null;
            String[] csvHeader = traitsAndMeans.keySet().toArray(new String[0]) ;  //csv header
            int studyId = Integer.valueOf(params.get(WebAPIConstants.STUDY_ID.getParamValue()));
            int outputDataSetId = Integer.valueOf(params.get(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue()));
            int inputDatasetId = Integer.valueOf(params.get(WebAPIConstants.INPUT_DATASET_ID.getParamValue()));
            
            	
            	List<DataSet> ds = studyDataManagerV2.getDataSetsByType(studyId, DataSetType.MEANS_DATA);
            	if (ds != null){
            		if (ds.size() > 0){
            			meansDataSet = ds.get(0);
            		}else if (outputDataSetId != 0){
                    	meansDataSet = studyDataManagerV2.getDataSet(outputDataSetId);
                    }
            		
            		if (meansDataSet != null) {
	            		if (this.checkColumnsChanged(csvHeader, meansDataSet)){
	            				studyDataManagerV2.deleteDataSet(meansDataSet.getId());
	            				meansDataSet = null;
	            		}else{
	            			meansDataSetExists = true;
	            		}
	            		
	            	}
            	}
            	
            
            TrialEnvironments trialEnvironments = studyDataManagerV2.getTrialEnvironmentsInDataset(inputDatasetId);
            //environment, env value
            TrialEnvironment trialEnv = trialEnvironments.findOnlyOneByLocalName(csvHeader[0], traitsAndMeans.get(csvHeader[0]).get(0));
            ndLocationId = trialEnv.getId();
            Stocks stocks = studyDataManagerV2.getStocksInDataset(inputDatasetId);

            DataSet dataSet = studyDataManagerV2.getDataSet(inputDatasetId);
            VariableTypeList variableTypeList = new VariableTypeList();
            
            //Get only the trial environment and germplasm factors
            
            for (VariableType factorFromDataSet : dataSet.getVariableTypes().getFactors().getVariableTypes()){
            	if (factorFromDataSet.getStandardVariable().getFactorType() == FactorType.TRIAL_ENVIRONMENT
            			|| factorFromDataSet.getStandardVariable().getFactorType() == FactorType.GERMPLASM) {
            		variableTypeList.makeRoom(1);
            		factorFromDataSet.setRank(1);
            		variableTypeList.add(factorFromDataSet);
            	}
            }
            //get variates only
            VariableTypeList variableTypeListVariates = dataSet.getVariableTypes().getVariates();
            VariableType originalVariableType = null;
            VariableType meansVariableType = null;
            VariableType unitErrorsVariableType = null;
            
            Integer numOfFactorsAndVariates = variableTypeList.getFactors().getVariableTypes().size()+variableTypeList.getVariates().getVariableTypes().size()+1;
            for(int i = 4; i < csvHeader.length; i += 2) {   //means and errors are in pair, so just get the word before _
                String root = csvHeader[i] != null ? csvHeader[i].split("_")[0] : "";
                if(!"".equals(root)) {
                    //Means
                    originalVariableType = variableTypeListVariates.findByLocalName(root);
                    meansVariableType = cloner.deepClone(originalVariableType);
                    meansVariableType.setLocalName(root + "_Means");
                    Term termLSMean = ontologyDataManagerV2.findMethodByName("LS MEAN");
                    if(termLSMean == null) {
                        String definitionMeans = meansVariableType.getStandardVariable().getMethod().getDefinition();
                        termLSMean = ontologyDataManagerV2.addMethod("LS MEAN", definitionMeans);
                    }
                    
                    Integer stdVariableId = ontologyDataManagerV2.getStandadardVariableIdByPropertyScaleMethod(
                            meansVariableType.getStandardVariable().getProperty().getId()
                    		,meansVariableType.getStandardVariable().getScale().getId()
                    		,termLSMean.getId()
                    		);
                    
                    if (stdVariableId == null){
                    	StandardVariable stdVariable = new StandardVariable();
                        stdVariable = cloner.deepClone(meansVariableType.getStandardVariable());
                        stdVariable.setId(0);
                        stdVariable.setName(meansVariableType.getLocalName());
                        stdVariable.setMethod(termLSMean);
                        
                        ontologyDataManagerV2.addStandardVariable(stdVariable);
                        meansVariableType.setStandardVariable(stdVariable);
                    	
                    }else{
                        meansVariableType.setStandardVariable(ontologyDataManagerV2.getStandardVariable(stdVariableId));
                    }
                    
                    variableTypeList.makeRoom(numOfFactorsAndVariates);
                    meansVariableType.setRank(numOfFactorsAndVariates);
                    variableTypeList.add(meansVariableType);
                    
                    stdVariableId = null;
                    //Unit Errors
                    unitErrorsVariableType = cloner.deepClone(originalVariableType);
                    unitErrorsVariableType.setLocalName(root + "_UnitErrors");
                    Term termErrorEstimate = ontologyDataManagerV2.findMethodByName("ERROR ESTIMATE");
                    if(termErrorEstimate == null) {
                        String definitionUErrors = unitErrorsVariableType.getStandardVariable().getMethod().getDefinition();
                        termErrorEstimate = ontologyDataManagerV2.addMethod("ERROR ESTIMATE", definitionUErrors);
                    }
                    
                     stdVariableId = ontologyDataManagerV2.getStandadardVariableIdByPropertyScaleMethod(
                             unitErrorsVariableType.getStandardVariable().getProperty().getId()
                    		,unitErrorsVariableType.getStandardVariable().getScale().getId()
                    		,termErrorEstimate.getId()
                    		);
                    
                    if (stdVariableId == null){
                    	StandardVariable stdVariable = new StandardVariable();
                        stdVariable = cloner.deepClone(unitErrorsVariableType.getStandardVariable());
                        stdVariable.setId(0);
                        stdVariable.setName(unitErrorsVariableType.getLocalName());
                        stdVariable.setMethod(termErrorEstimate);
                        
                        ontologyDataManagerV2.addStandardVariable(stdVariable);
                        unitErrorsVariableType.setStandardVariable(stdVariable);
                    	
                    }else{
                        unitErrorsVariableType.setStandardVariable(ontologyDataManagerV2.getStandardVariable(stdVariableId));
                    }
                   
                    variableTypeList.makeRoom(numOfFactorsAndVariates);
                    unitErrorsVariableType.setRank(numOfFactorsAndVariates);
                    variableTypeList.add(unitErrorsVariableType);
                }
            }

            

            fileName = new File(fileName).getName();
            //please make sure that the study name is unique and does not exist in the db.
            VariableList variableList = new VariableList();
            Variable variable = createVariable(TermId.DATASET_NAME.getId(), "RESULTS_TRAIT_MEANS" + "_" + workbenchProjectId + "_" + studyId , 1);
            variableTypeList.makeRoom(1);
            variable.getVariableType().setRank(1);
            variableTypeList.add(variable.getVariableType());
            
            //name of dataset Results_trait_means_<wproject_id>_<study_id>
            updateVariableType(variable.getVariableType(), "RESULTS_TRAIT_MEANS" + "_" + workbenchProjectId + "_" + studyId, "Dataset name (local)");
            variableList.add(variable);

            variable = createVariable(TermId.DATASET_TITLE.getId(), "My Dataset Description", 2);
            variableTypeList.makeRoom(1);
            variable.getVariableType().setRank(1);
            variableTypeList.add(variable.getVariableType());
            updateVariableType(variable.getVariableType(), "DATASET_TITLE", "Dataset title (local)");
            variableList.add(variable);

            variable = createVariable(TermId.DATASET_TYPE.getId(), "10070", 3);
            variableTypeList.makeRoom(1);
            variable.getVariableType().setRank(1);
            variableTypeList.add(variable.getVariableType());
            updateVariableType(variable.getVariableType(), "DATASET_TYPE", "Dataset type (local)");
            variableList.add(variable);
            DatasetValues datasetValues = new DatasetValues();
            datasetValues.setVariables(variableList);
            
       
            
            DatasetReference datasetReference = null;
            if (meansDataSet == null){
            	//save data
                //get dataset using new datasetid
            	datasetReference = studyDataManagerV2.addDataSet(studyId, variableTypeList, datasetValues);
            	meansDataSet = studyDataManagerV2.getDataSet(datasetReference.getId());
            }
            
            
            if (meansDataSetExists){
            	//TrialEnvironment env = studyDataManagerV2.getTrialEnvironmentsInDataset(meansDataSet.getId()).findOnlyOneByLocalName(csvHeader[0], traitsAndMeans.get(csvHeader[0]).get(0));
            	if (studyDataManagerV2.getDataSet(meansDataSet.getId()).getLocationIds().contains(ndLocationId)){
                		studyDataManagerV2.deleteExperimentsByLocation(meansDataSet.getId(), ndLocationId);
            	}
            }
            
   
            ArrayList<String> environments = traitsAndMeans.get(csvHeader[0]);
            for(int i = 0; i < environments.size(); i++) {
               
            	Stock stock = stocks.findOnlyOneByLocalName(csvHeader[2], traitsAndMeans.get(csvHeader[2]).get(i));
            	if (stock != null){
	            	ExperimentValues experimentRow = new ExperimentValues();
	            	experimentRow.setGermplasmId(stock.getId());
	            	experimentRow.setLocationId(ndLocationId);
	            		
		            	List<Variable> list = new ArrayList<Variable>();
		                for(int j = 2; j < csvHeader.length; j++) {
		                	Variable var = new Variable( meansDataSet.getVariableTypes().findByLocalName(csvHeader[j]), traitsAndMeans.get(csvHeader[j]).get(i));
		                	list.add(var);
		                }
		            VariableList variableList1 = new VariableList();
		            variableList1.setVariables(list);
	                experimentRow.setVariableList(variableList1);
	                
	                studyDataManagerV2.addExperiment(meansDataSet.getId(), ExperimentType.AVERAGE, experimentRow);
            	}
            }

        } else {
            throw new Exception("Input data is empty. No data was processed.");
        }
    }


    private Variable createVariable(int termId, String value, int rank) throws Exception {
        StandardVariable stVar = ontologyDataManagerV2.getStandardVariable(termId);

        VariableType vtype = new VariableType();
        vtype.setStandardVariable(stVar);
        vtype.setRank(rank);
        Variable var = new Variable();
        var.setValue(value);
        var.setVariableType(vtype);
        return var;
    }

    private void updateVariableType(VariableType type, String name, String description) {
        type.setLocalName(name);
        type.setLocalDescription(description);
    }
    
    private boolean checkColumnsChanged(String[] csvHeader, DataSet ds){
    	
    	String[] csvHeaderTemp = Arrays.copyOf(csvHeader, csvHeader.length);
        for (int i=0; i<csvHeaderTemp.length; ++i)
        	csvHeaderTemp[i] = csvHeaderTemp[i].toLowerCase();
       
    	List<String> header1 = Arrays.asList(Arrays.copyOfRange(csvHeaderTemp, 4, csvHeaderTemp.length));
    	List<String> header2 = new ArrayList<String>();
    	for (VariableType var : ds.getVariableTypes().getVariates().getVariableTypes()){
    		header2.add(var.getLocalName().toLowerCase());
    	}
    	Collections.sort(header1);
    	Collections.sort(header2);
    	
    	return !header2.equals(header1);
    	
    }

	
	public void deleteDataSet(Integer dataSetId) throws Exception {
		// TODO Auto-generated method stub
		studyDataManagerV2.deleteDataSet(dataSetId);
	}
}
