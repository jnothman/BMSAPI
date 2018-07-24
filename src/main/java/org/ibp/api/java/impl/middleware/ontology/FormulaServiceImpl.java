package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.FormulaValidator;
import org.ibp.api.java.ontology.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class FormulaServiceImpl implements FormulaService {

	@Autowired
	private org.generationcp.middleware.service.api.derived_variables.FormulaService formulaService;

	@Autowired
	private FormulaValidator formulaValidator;


	@Override
	public FormulaDto save(final FormulaDto formulaDto) {

		this.extractInputs(formulaDto);

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), FormulaDto.class.getName());
		this.formulaValidator.validate(formulaDto, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		return this.formulaService.save(formulaDto);
	}

	private void extractInputs(final FormulaDto formulaDto) {
		final String definition = formulaDto.getDefinition();
		if (definition == null) {
			return;
		}
		final List<String> inputs = DerivedVariableUtils.extractInputs(definition);
		final List<FormulaVariable> formulaInputs = new ArrayList<>();
		for (final String input : inputs) {
			final FormulaVariable formulaVariable = new FormulaVariable();
			formulaVariable.setName(input);
			formulaInputs.add(formulaVariable);
		}
		formulaDto.setInputs(formulaInputs);
	}
}
