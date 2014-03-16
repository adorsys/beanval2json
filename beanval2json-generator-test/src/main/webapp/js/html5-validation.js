(function() {

	/**
	 * Simple example to combine beanvalidation-constraints with HTML5 form-validation:
	 * - @NotNull: required-attribute will be added to the input-field and a custom
	 *   error-message will be shown if defined
	 * - @Size: minlength and maxlength attributes will be added to the input-field.
	 *   NOTE: most browsers will just cut off everything after maxlength is reached and
	 *   won't validate here
	 * - @Pattern: The regexp-value will be added as pattern-attribute and a custom
	 *   error-message will be shown if defined 
	 * - @Min and @Max: value will be used for min/max-Attribute and a custom
	 *   error-message will be shown if defined. 
	 *   NOTE: Will only work on input type=number
	 * - @DecimalMin and @DecimalMax: like @Min and @Max
	 *   NOTE: always handled "inclusive" and as Integers. You can handle numbers as
	 *   floats if you add a fraction via @Digits
	 * - @Digits: If fraction is defined an according step-attribute will be added
	 */
	var Validation = function() {
		
		var self = {};
		
		self.init = function() {
			loadJSON(url, initConstraints);
		};
		
		var url = 'js/constraints.json';
		
		var rules = {
			notNull : function(input, constraint) {
				input.setAttribute('required', 'required');
				addMessage(input, constraint, 'data-err-value-missing');				
			},
			size : function(input, constraint) {
				if (constraint.max) {
					input.setAttribute('maxlength', constraint.max);
				}
				if (constraint.min) {
					input.setAttribute('minlength', constraint.min);
				}
			},
			pattern : function(input, constraint) {
				if (constraint.regexp) {
					input.setAttribute('pattern', constraint.regexp);
				}
				addMessage(input, constraint, 'data-err-pattern-mismatch');
			},
			min : function(input, constraint) {
				numericConstraint(input, constraint, 'min', false);
			},
			max : function(input, constraint) {
				numericConstraint(input, constraint, 'max', false);
			},
			decimalMin : function(input, constraint) {
				numericConstraint(input, constraint, 'min', true);
			},
			decimalMax : function(input, constraint) {
				numericConstraint(input, constraint, 'max', true);
			},
			digits : function(input, constraint) {
				if (constraint.fraction) {
					var step = 1 / Math.pow(10, constraint.fraction);
					input.setAttribute('step', step);
				}
			}
		};
		
		var errorMapping = {
			'valueMissing' : 'data-err-value-missing',
			'patternMismatch' : 'data-err-pattern-mismatch',
			'rangeUnderflow' : 'data-err-range-underflow',
			'rangeOverflow' : 'data-err-range-overflow'
		};
		
		var numericConstraint = function(input, constraint, type, parse) {
			if (constraint.value) {
				input.setAttribute(type, parse ? parseInt(constraint.value, 10) : constraint.value);
			}
			addMessage(input, constraint, type === 'min' ? 'data-err-range-underflow' : 'data-err-range-overflow');
		};
		
		var addMessage = function(input, constraint, attribute) {
			if (constraint.message) {
				input.setAttribute(attribute, constraint.message);
			}
		};
		
		var initConstraints = function(data) {
			for (property in data) {
				var input = document.getElementById(property);
				if (!input) {
					continue;
				}
				var constraints = data[property];				
				for (constraint in constraints) {
					if (rules[constraint]) {
						rules[constraint](input, constraints[constraint]);
					}
				}
				input.oninvalid = function(e) {
					showError(e.target);
				}
			}
		};
		
		var showError = function(input) {	
			input.setCustomValidity('');
			if (input.validity.valid) {
				return;
			}
			for (type in input.validity) {
				var errAttribute = errorMapping[type];
				if (!errAttribute || !input.validity[type]) {
					continue;
				}
				console.log(errAttribute);								
				input.setCustomValidity(input.getAttribute(errAttribute));
			}
		};
		
		var loadJSON = function(url, callback) {
			var xhr = new XMLHttpRequest();
		    xhr.onreadystatechange = function() {
		        if (xhr.readyState === 4 && xhr.status === 200) {
	        		callback(JSON.parse(xhr.responseText));
		        }
		    };
		    xhr.open("GET", url, true);
		    xhr.send();
		};

		return self;
		
	};
	
	window.onload = function() {
		new Validation().init();
	};

})()