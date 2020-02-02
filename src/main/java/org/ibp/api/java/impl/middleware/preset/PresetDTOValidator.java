package org.ibp.api.java.impl.middleware.preset;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.SubObservationDatasetLabelPrinting;
import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.ibp.api.rest.preset.domain.PresetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

/**
 * Created by clarysabel on 2/19/19.
 */
@Component
public class PresetDTOValidator {

	private static final Integer FIELDBOOK_TOOL_ID = 23;

	@Autowired
	private PresetService presetService;

	@Autowired
	private VariableService variableService;

	private BindingResult errors;

	public void validate(final String crop, final PresetDTO presetDTO) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());

		if (presetDTO.getToolId() == null || presetDTO.getToolId()!= FIELDBOOK_TOOL_ID) {
			this.errors.reject("preset.invalid.tool", "");
		}

		//Validate toolSection
		//As of now, this will only support DATASET_LABEL_PRINTING_PRESET
		if (!ToolSection.DATASET_LABEL_PRINTING_PRESET.name().equals(presetDTO.getToolSection())) {
			this.errors.reject("preset.invalid.tool.section", "");
		}

		//Validate type
		if (StringUtils.isEmpty(presetDTO.getType()) || PresetType.getEnum(presetDTO.getType()) == null) {
			this.errors.reject("preset.invalid.type", "");
		}

		final String programUUID = presetDTO.getProgramUUID();
		if (StringUtils.isEmpty(programUUID)) {
			this.errors.reject("preset.program.uuid.required", "");
		// It is assumed that programUUID was set in ContextHolder. Check that it is the same specified in DTO
		} else if (!programUUID.equals(ContextHolder.getCurrentProgram())) {
			this.errors.reject("preset.invalid.program.uuid", "");
		}

		if (StringUtils.isEmpty(presetDTO.getName())) {
			this.errors.reject("preset.name.required", "");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<ProgramPreset> presets = this.presetService
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), programUUID, presetDTO.getToolId(),
						presetDTO.getToolSection());
		if (!presets.isEmpty()) {
			this.errors.reject("preset.name.invalid", "");
			throw new ConflictException(this.errors.getAllErrors());
		}

		if (!PresetType.LABEL_PRINTING_PRESET.equals(PresetType.getEnum(presetDTO.getType()))) {
			this.errors.reject("preset.type.not.supported", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}

		//Cast according type and call specific validations
		if (presetDTO instanceof LabelPrintingPresetDTO) {
			final LabelPrintingPresetDTO labelPrintingPresetDTO = (LabelPrintingPresetDTO) presetDTO;
			this.validateLabelPrintingPreset(crop, labelPrintingPresetDTO);
		}
	}

	private void validateLabelPrintingPreset(final String crop, final LabelPrintingPresetDTO labelPrintingPresetDTO) {
		if (labelPrintingPresetDTO.getFileConfiguration() == null) {
			this.errors.reject("label.printing.preset.file.configuration.required", "");
		}
		if (labelPrintingPresetDTO.getBarcodeSetting() == null) {
			this.errors.reject("label.printing.preset.barcode.setting.required", "");
		}
		if (labelPrintingPresetDTO.getSelectedFields() == null) {
			this.errors.reject("label.printing.preset.selected.fields.required", "");
		}
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		labelPrintingPresetDTO.getSelectedFields().forEach(list -> {
			list.forEach(fieldId -> {
				if (!LabelPrintingStaticField.getAvailableStaticFields().contains(fieldId)
						&& this.variableService.getVariableById(crop, labelPrintingPresetDTO.getProgramUUID(), String.valueOf(fieldId))
						== null) {
					this.errors.reject("label.printing.preset.invalid.field.in.selected.field", "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				}
			});
		});

		if (labelPrintingPresetDTO.getBarcodeSetting().isBarcodeNeeded()) {
			if (labelPrintingPresetDTO.getBarcodeSetting().isAutomaticBarcode()) {
					if (labelPrintingPresetDTO.getBarcodeSetting()
					.getBarcodeFields() != null && !labelPrintingPresetDTO.getBarcodeSetting()
					.getBarcodeFields().isEmpty()) {
						this.errors.reject("label.printing.preset.inconsistent.barcode.setting", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
					}
			} else {
				if (labelPrintingPresetDTO.getBarcodeSetting().getBarcodeFields() == null || labelPrintingPresetDTO.getBarcodeSetting()
						.getBarcodeFields().isEmpty()) {
					this.errors.reject("label.printing.preset.inconsistent.barcode.setting", "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				}
				labelPrintingPresetDTO.getBarcodeSetting().getBarcodeFields().forEach(fieldId -> {
					if (!LabelPrintingStaticField.getAvailableStaticFields().contains(fieldId)
							&& this.variableService.getVariableById(crop, labelPrintingPresetDTO.getProgramUUID(), String.valueOf(fieldId))
							== null) {
						this.errors.reject("label.printing.preset.invalid.field.in.barcode.field", "");
						throw new ApiRequestValidationException(this.errors.getAllErrors());
					}
				});
			}
		} else {
			if (labelPrintingPresetDTO.getBarcodeSetting().isAutomaticBarcode()
					|| (labelPrintingPresetDTO.getBarcodeSetting().getBarcodeFields() != null && !labelPrintingPresetDTO.getBarcodeSetting()
					.getBarcodeFields().isEmpty())) {
				this.errors.reject("label.printing.preset.inconsistent.barcode.setting", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

		final FileType fileType = FileType.getEnum(labelPrintingPresetDTO.getFileConfiguration().getOutputType());
		if (fileType == null) {
			this.errors.reject("preset.invalid.file.type", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!SubObservationDatasetLabelPrinting.SUPPORTED_FILE_TYPES.contains(fileType)) {
			this.errors.reject("preset.not.supported.file.type", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}

	}

}
