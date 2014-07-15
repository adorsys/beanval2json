var app = angular.module('beanval2json', []);

app.factory('Validator', function($http, $q) {
	
	var url = '../js/constraints.json';
	
	var deferred = $q.defer();
	
	/**
	 * loads the constraints only once
	 */
	$http.get(url).success(function(constraints) {
		deferred.resolve(constraints);
	});
	
	/**
	 * Registers a constraint. If validator functions are found
	 * for the given element they will be passed to the callback-function.
	 */
	var register = function(scope, elem, attrs, ctrl, cb) {
		deferred.promise.then(function(constraints) {
			var validators = registerConstraint(scope, elem, attrs, ctrl, constraints);
			return validators.length > 0 ? cb(validators) : undefined;
		});
	};
	
	/**
	 * Registers a constraint mapped by
	 * beanval="key.of.constraint" or if not present
	 * ng-model="key.of.constraint".
	 */
	var registerConstraint = function(scope, elem, attrs, ctrl, constraints) {
		var key = attrs['beanval'] || attrs['ngModel']; 
		var constraint = constraints[key];
		if (!constraint) {
			return [];
		}
		var validators = [];
		for (var rule in constraint) {
			if (!rules[rule]) {
				continue;
			}
			var validator = function(r, c) {
				return function(value) {
					return rules[r](value, c);
				}
			}(rule, constraint[rule]);
			validators.unshift(validator);
			registerNotNullEvents(rule, ctrl, elem, scope);
		}
		return validators;
	};
	
	/**
	 * HACK: Validation is triggered on a change event which
	 * will not be fired if you don't enter a value...
	 * We 'change' the value also on blur and form submit.
	 */
	var registerNotNullEvents = function(rule, ctrl, elem, scope) {
		var change = function() {
			ctrl.$setViewValue(ctrl.$viewValue);
	    }
		elem.on('blur', change);
		scope.$on('validateNotNull', change);
	}
	
	var validationResult = function(rule, valid, constraint) {
		var result = {};
		result[rule] = valid;
		if (constraint.message) {
			result['message'] = constraint.message;
		}
		return result;
	};
	
	/**
	 * The Rules defined by the javax.validation.constraints annotations.
	 * Note that {@code null} elements are considered valid for most of them.
	 * Please be aware that this is a simple example implementation which
	 * is not tested in a real-life application.
	 */
	var rules = {
		"assertFalse" : function(value, constraint) {
			// as {@code null} elements are considered valid
			// so no JS-like check for !value
			return validationResult('assertFalse', value === false, constraint);
		},
		"assertTrue" : function(value, constraint) {
			// as {@code null} elements are considered valid
			// so no JS-like check for value
			return validationResult('assertFalse', value === true, constraint);
		},
		"decimalMin" : function(value, constraint) {
			var valid = true;
			if (value) {
				var inclusive = constraint.inclusive || true; // By default it's inclusive
				var min = parseFloat(constraint.value);
				valid = !isNaN(value) && inclusive ? parseFloat(value) >= min : parseFloat(value) > min;
			}
			return validationResult('decimalMin', valid, constraint);
		},
		"decimalMax" : function(value, constraint) {
			var valid = true;
			if (value) {
				var inclusive = constraint.inclusive || true; // By default it's inclusive
				var min = parseFloat(constraint.value);
				valid = !isNaN(value) && inclusive ? parseFloat(value) <= min : parseFloat(value) < min;
			}
			return validationResult('decimalMax', valid, constraint);
		},
		"digits" : function(value, constraint) {
			var valid =  true;
			if (value && !isNaN(value)) {
				var pattern = '^\\d{1,' + constraint.integer + '}(\\.\\d{1,' + constraint.fraction + '})?$';
				valid = new RegExp(pattern).test(value + '');
			}
			return validationResult('digits', valid, constraint);
		},
		"future" : function(value, constraint) {
			var valid = true;
			if (value) {
				var today = new Date(new Date().setHours(0, 0, 0, 0));
				var parsedValue = new Date(new Date(value).setHours(0, 0, 0, 0));
				valid = parsedValue > today;
			}
			return validationResult('future', valid, constraint);
		},
		"max" : function(value, constraint) {
			var valid = true;
			if (value) {
				valid = !isNaN(value) && parseInt(value, 10) <= parseInt(constraint.value, 10);
			}
			return validationResult('max', valid, constraint);
		},		
		"min" : function(value, constraint) {
			var valid = true;
			if (value) {
				valid = !isNaN(value) && parseInt(value, 10) >= parseInt(constraint.value, 10);
			}
			return validationResult('min', valid, constraint);
		},
		"notNull" : function(value, constraint) {
			return validationResult('notNull', value ? true : false, constraint);
		},
		"null" : function(value, constraint) {
			return validationResult('null', value ? false : true, constraint);
		},
		"past" : function(value, constraint) {
			var valid = true;
			if (value) {
				var today = new Date(new Date().setHours(0, 0, 0, 0));
				var parsedValue = new Date(new Date(value).setHours(0, 0, 0, 0));
				valid = parsedValue < today;
			}
			return validationResult('past', valid, constraint);
		},		
		"pattern" : function(value, constraint) {
			var valid = true;
			if (value) {
				// we are just using the regexp right now. flags are ignored.
				valid = new RegExp('^(?:' + constraint.regexp + ')$').test(value.trim());
			}
			return validationResult('pattern', valid, constraint);
		},
		"size" : function(value, constraint) {
			var valid = true;
			if (value) {
				// @Size should check CharSequences, Arrays, Collections and Maps
				var length = value instanceof Array ? value.length
						: typeof(value) === 'object' ? Object.keys(value).length 
						: value.trim().length;
				if (valid && constraint.min) {
					valid = constraint.min <= length;
				}
				if (valid && constraint.max) {
					valid = constraint.max >= length;
				}
						
			}
			return validationResult('size', valid, constraint);
		}
	};
	
	return {
		register : register
	}

});

/**
 * The beanval-directive. Adds a validation-function to 
 * each beanval-marked element as described here:
 * https://code.angularjs.org/1.2.0-rc.3/docs/guide/forms
 * The Mapping of constraints.json to the input elements
 * can be done by adding the json-key as ng-model or
 * beanval-attribute.
 * You can control the key as described here
 * https://github.com/adorsys/beanval2json#options
 */
app.directive('beanval', function(Validator) {

	var link = function(scope, elem, attrs, ctrl) {
		Validator.register(scope, elem, attrs, ctrl, function(validators) {
			ctrl.$parsers.unshift(function(viewValue) {
				return validate(validators, viewValue, ctrl);
			});
		});
	};
	
	var validate = function(validators, viewValue, ctrl) {
		ctrl.beanvalErrors = [];
		for (var i = 0; i < validators.length; i++) {
			var valid = validators[i](viewValue);
			var rule = Object.keys(valid)[0];
			if (valid['message'] && !valid[rule]) {
				ctrl.beanvalErrors.push(valid['message']);
			}
			ctrl.$setValidity(rule, valid[rule]);
		}
		return ctrl.$valid ? viewValue : undefined;
	};
		
	return {
		restrict: 'A',
		require : 'ngModel',
		link : link		
	};
	
});

app.controller('MainCtrl', function($scope) {
	$scope.submitForm = function() {
		$scope.$broadcast('validateNotNull');
	}
});