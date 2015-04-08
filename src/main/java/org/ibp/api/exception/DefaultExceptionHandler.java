package org.ibp.api.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.FileNotFoundException;
import java.util.Objects;

import org.ibp.api.domain.ontology.ErrorResponse;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

@ControllerAdvice
public class DefaultExceptionHandler {

	@Autowired
	ResourceBundleMessageSource messageSource;

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleUncaughtException(Exception ex) {
		ErrorResponse response = new ErrorResponse();
		if (ex.getCause() != null) {
			response.addError(ex.getCause().getMessage());
		} else {
			response.addError(ex.getMessage());
		}
		return response;
	}

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(BeanCreationException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleBeanCreationException(BeanCreationException ex) {
		ErrorResponse response = new ErrorResponse();
		Throwable rootCause = ex.getRootCause();
		if (rootCause instanceof FileNotFoundException
				&& Objects.equals(rootCause.getMessage(), "selected.crop.not.valid")) {
			response.addError(this.messageSource.getMessage("selected.crop.not.valid", null,
					LocaleContextHolder.getLocale()), "cropname");
		} else {
			response.addError(this.messageSource.getMessage("unknown.error", null,
					LocaleContextHolder.getLocale()));
		}
		return response;
	}

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpMessageNotReadableException(HttpMessageNotReadableException ex) {
		ErrorResponse response = new ErrorResponse();
		Throwable rootCause = ex.getRootCause();
		if (rootCause instanceof UnrecognizedPropertyException) {
			UnrecognizedPropertyException unrecognizedPropertyException = (UnrecognizedPropertyException) ex
					.getCause();
			response.addError(this.messageSource.getMessage("not.recognised.field",
					new Object[] { unrecognizedPropertyException.getPropertyName() },
					LocaleContextHolder.getLocale()));
		} else if (rootCause instanceof JsonParseException) {
			response.addError(this.messageSource.getMessage("invalid.body", null,
					LocaleContextHolder.getLocale()));
		} else if (rootCause instanceof JsonMappingException) {
			response.addError(this.messageSource.getMessage("invalid.body", null,
					LocaleContextHolder.getLocale()));
		}
		return response;
	}

	/**
	 * @param ex
	 */
	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException ex) {
		ErrorResponse response = new ErrorResponse();
		response.addError(this.messageSource.getMessage("request.method.not.supported", null,
				LocaleContextHolder.getLocale()));
		return response;
	}

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(ApiRequestValidationException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleValidationException(ApiRequestValidationException ex) {

		ErrorResponse response = new ErrorResponse();

		for (ObjectError error : ex.getErrors()) {
			String message = this.messageSource.getMessage(error.getCode(), error.getArguments(),
					LocaleContextHolder.getLocale());
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				response.addError(message, fieldError.getField());
			} else {
				response.addError(message);
			}
		}
		return response;
	}
}
