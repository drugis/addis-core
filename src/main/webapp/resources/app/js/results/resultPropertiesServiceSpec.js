'use strict';
define([
  'lodash',
  'angular-mocks',
  './results',
  '../util/resultsConstants'
], function(_) {
  describe('the resultPropertiesService', function() {
    var resultPropertiesService;

    var ARM_LEVEL = 'ontology:arm_level_data';
    var CONTRAST = 'ontology:contrast_data';
    var DICHOTOMOUS = 'ontology:dichotomous';
    var CONTINUOUS = 'ontology:continuous';
    var SURVIVAL = 'ontology:survival';
    var variableTypeDetails;
    var contrastOptions;

    beforeEach(function() {
      angular.mock.module('trialverse.variable');
    });

    beforeEach(inject(function(
      ResultPropertiesService,
      RESULT_PROPERTY_TYPE_DETAILS,
      CONTRAST_OPTIONS_DETAILS
    ) {
      resultPropertiesService = ResultPropertiesService;
      variableTypeDetails = RESULT_PROPERTY_TYPE_DETAILS;
      contrastOptions = CONTRAST_OPTIONS_DETAILS;
    }));

    describe('getDefaultResultProperties', function() {
      it('should return the correct default resultproperties for continuous variables of arm level data', function() {
        var continuousArmLevelProperties = [
          variableTypeDetails[ARM_LEVEL].sample_size,
          variableTypeDetails[ARM_LEVEL].mean,
          variableTypeDetails[ARM_LEVEL].standard_deviation
        ];
        var result = resultPropertiesService.getDefaultResultProperties(CONTINUOUS, ARM_LEVEL);
        expect(result).toEqual(continuousArmLevelProperties);
      });

      it('should return the correct default resultproperties for dichotomous variables of arm level data', function() {
        var dichotomousArmLevelProperties = [
          variableTypeDetails[ARM_LEVEL].sample_size,
          variableTypeDetails[ARM_LEVEL].count
        ];
        var result = resultPropertiesService.getDefaultResultProperties(DICHOTOMOUS, ARM_LEVEL);
        expect(result).toEqual(dichotomousArmLevelProperties);
      });

      it('should return the correct default resultproperties for survival variables of arm level data', function() {
        var survivalArmLevelProperties = [
          variableTypeDetails[ARM_LEVEL].count,
          variableTypeDetails[ARM_LEVEL].exposure
        ];
        var result = resultPropertiesService.getDefaultResultProperties('ontology:survival', ARM_LEVEL);
        expect(result).toEqual(survivalArmLevelProperties);
      });

      it('should return the correct default resultproperties for dichotomous variables of contrast data', function() {
        var dichotomousContrastProperties = [
          variableTypeDetails[CONTRAST].standard_error
        ];
        var result = resultPropertiesService.getDefaultResultProperties(DICHOTOMOUS, CONTRAST);
        expect(result).toEqual(dichotomousContrastProperties);
      });

      it('should return the correct default resultproperties for continuous variables of contrast data', function() {
        var continuousContrastProperties = [
          variableTypeDetails[CONTRAST].standard_error
        ];
        var result = resultPropertiesService.getDefaultResultProperties(CONTINUOUS, CONTRAST);
        expect(result).toEqual(continuousContrastProperties);
      });

      it('should return the correct default resultproperties for survival variables of contrast data', function() {
        var survivalContrastProperties = [
          variableTypeDetails[CONTRAST].standard_error
        ];
        var result = resultPropertiesService.getDefaultResultProperties('ontology:survival', CONTRAST);
        expect(result).toEqual(survivalContrastProperties);
      });

      it('should send an error to the console if the measurement type is unknown', function() {
        spyOn(console, 'error');
        resultPropertiesService.getDefaultResultProperties('ontology:something', ARM_LEVEL);
        expect(console.error).toHaveBeenCalledWith('unknown measurement type ontology:something');
      });
    });

    describe('buildPropertyCategories', function() {
      it('should build the categories for a continuous variable', function() {
        var varDetails = _.reduce(variableTypeDetails[ARM_LEVEL], function(accum, varType) {
          accum[varType.type] = _.extend({}, varType, {
            isSelected: false
          });
          return accum;
        }, {});
        var variable = {
          measurementType: CONTINUOUS,
          selectedProperties: [],
          armOrContrast: ARM_LEVEL
        };

        var result = resultPropertiesService.buildPropertyCategories(variable);
        var expectedResult = {
          'Sample size': {
            categoryLabel: 'Sample size',
            properties: [varDetails.sample_size],
          },
          'Central tendency': {
            categoryLabel: 'Central tendency',
            properties: [varDetails.mean,
            varDetails.median,
            varDetails.geometric_mean,
            varDetails.log_mean,
            varDetails.least_squares_mean
            ]
          },
          Quantiles: {
            categoryLabel: 'Quantiles',
            properties: [
              varDetails['quantile_0.05'],
              varDetails['quantile_0.95'],
              varDetails['quantile_0.025'],
              varDetails['quantile_0.975'],
              varDetails.first_quartile,
              varDetails.third_quartile,

            ]
          },
          Dispersion: {
            categoryLabel: 'Dispersion',
            properties: [
              varDetails.min,
              varDetails.max,
              varDetails.geometric_coefficient_of_variation,
              varDetails.standard_deviation,
              varDetails.standard_error
            ]
          }
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getResultPropertiesForType', function() {
      it('should get the right variable details for continuous type for arm level data', function() {
        var varTypes = variableTypeDetails[ARM_LEVEL];
        var continuousResult = resultPropertiesService.getResultPropertiesForType(CONTINUOUS, ARM_LEVEL);
        var expectedContinuousResult = [
          varTypes.sample_size,
          varTypes.mean,
          varTypes.median,
          varTypes.geometric_mean,
          varTypes.log_mean,
          varTypes.least_squares_mean,
          varTypes['quantile_0.05'],
          varTypes['quantile_0.95'],
          varTypes['quantile_0.025'],
          varTypes['quantile_0.975'],
          varTypes.min,
          varTypes.max,
          varTypes.geometric_coefficient_of_variation,
          varTypes.first_quartile,
          varTypes.third_quartile,
          varTypes.standard_deviation,
          varTypes.standard_error
        ];
        expect(continuousResult).toEqual(expectedContinuousResult);
      });

      it('should get the right variable details for dichotomous type for arm level data', function() {
        var varTypes = variableTypeDetails[ARM_LEVEL];
        var dichotomousResult = resultPropertiesService.getResultPropertiesForType(DICHOTOMOUS, ARM_LEVEL);
        var expectedDichotomousResult = [
          varTypes.sample_size,
          varTypes.event_count,
          varTypes.count,
          varTypes.percentage,
          varTypes.proportion
        ];
        expect(dichotomousResult).toEqual(expectedDichotomousResult);
      });

      it('should get the right variable details for survival type for arm level data', function() {
        var varTypes = variableTypeDetails[ARM_LEVEL];
        var survivalResult = resultPropertiesService.getResultPropertiesForType(SURVIVAL, ARM_LEVEL);
        var expectedSurvivalResult = [
          varTypes.hazard_ratio,
          varTypes['quantile_0.025'],
          varTypes['quantile_0.975'],
          varTypes.count,
          varTypes.exposure
        ];
        expect(survivalResult).toEqual(expectedSurvivalResult);
      });

      it('should get the right variable details for dichotomous contrast data', function() {
        var dichotomousResult = resultPropertiesService.getResultPropertiesForType(DICHOTOMOUS, CONTRAST);
        var varTypes = variableTypeDetails[CONTRAST];
        var expectedDichotomousResult = [
          varTypes.standard_error,
          varTypes.confidence_interval_width,
          varTypes.confidence_interval_lower_bound,
          varTypes.confidence_interval_upper_bound
        ];
        expect(dichotomousResult).toEqual(expectedDichotomousResult);
      });

      it('should get the right variable details for continuous contrast data', function() {
        var continuousResult = resultPropertiesService.getResultPropertiesForType(CONTINUOUS, CONTRAST);
        var varTypes = variableTypeDetails[CONTRAST];
        var expectedContinuousResult = [
          varTypes.standard_error,
          varTypes.confidence_interval_width,
          varTypes.confidence_interval_lower_bound,
          varTypes.confidence_interval_upper_bound
        ];
        expect(continuousResult).toEqual(expectedContinuousResult);
      });

      it('should get the right variable details for survival contrast data', function() {
        var survivalResult = resultPropertiesService.getResultPropertiesForType(SURVIVAL, CONTRAST);
        var varTypes = variableTypeDetails[CONTRAST];
        var expectedSurvivalResult = [
          varTypes.standard_error,
          varTypes.confidence_interval_width,
          varTypes.confidence_interval_lower_bound,
          varTypes.confidence_interval_upper_bound
        ];
        expect(survivalResult).toEqual(expectedSurvivalResult);
      });
    });

    describe('setTimeScaleInput', function() {
      it('should add the default timescale if the variable has exposure, but no time scale set', function() {
        var variable = {
          selectedResultProperties: [{
            uri: 'http://trials.drugis.org/ontology#exposure'
          }]
        };
        var result = resultPropertiesService.setTimeScaleInput(variable);
        var expectedResult = {
          selectedResultProperties: [{
            uri: 'http://trials.drugis.org/ontology#exposure'
          }],
          timeScale: 'P1W'
        };
        expect(result).toEqual(expectedResult);
      });

      it('should return the variable if the variable has exposure, but already has time scale set', function() {
        var variable = {
          selectedResultProperties: [{
            uri: 'http://trials.drugis.org/ontology#exposure'
          }],
          timeScale: 'P12W'
        };
        var result = resultPropertiesService.setTimeScaleInput(variable);
        expect(result).toEqual(variable);
      });

      it('should delete time scale for variables without exposure ', function() {
        var variable = {
          selectedResultProperties: [],
          timeScale: 'P12W'
        };
        var result = resultPropertiesService.setTimeScaleInput(variable);
        var expectedResult = {
          selectedResultProperties: []
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('resetResultProperties', function() {
      it('should set armOrContrast to arm, return the result properties to their default values, and remove categories if needed', function() {
        var varTypes = variableTypeDetails[ARM_LEVEL];
        var variable = {
          armOrContrast: CONTRAST,
          measurementType: 'ontology:continuous',
          categoryList: []
        };
        var arms = [];
        var result = resultPropertiesService.resetResultProperties(variable, arms);
        var expectedResult = {
          armOrContrast: ARM_LEVEL,
          measurementType: 'ontology:continuous',
          selectedResultProperties: [
            varTypes.sample_size,
            varTypes.mean,
            varTypes.standard_deviation
          ],
          resultProperties: varTypes
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('armOrContrastChanged', function() {
      it('should return the result properties to their default values, and if the variable is contrast, set the default reference arm ', function() {
        var varTypes = variableTypeDetails[CONTRAST];
        var variable = {
          armOrContrast: CONTRAST,
          measurementType: 'ontology:survival'
        };
        var arms = [{ armURI: 'arm1' }];
        var result = resultPropertiesService.armOrContrastChanged(variable, arms);
        var expectedResult = {
          armOrContrast: CONTRAST,
          measurementType: 'ontology:survival',
          selectedResultProperties: [
            varTypes.standard_error
          ],
          resultProperties: varTypes,
          contrastOptions: [contrastOptions.hazard_ratio],
          contrastOption: contrastOptions.hazard_ratio,
          referenceArm: 'arm1',
          isLog: true
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getContrastOptions', function() {
      it('should return the contrast options for continuous', function() {
        var result = resultPropertiesService.getContrastOptions(CONTINUOUS);
        var expectedResult = [
          contrastOptions.continuous_mean_difference,
          contrastOptions.standardized_mean_difference
        ];
        expect(result).toEqual(expectedResult);
      });

      it('should return the contrast options for dichotomous', function() {
        var result = resultPropertiesService.getContrastOptions(DICHOTOMOUS);
        var expectedResult = [
          contrastOptions.odds_ratio,
          contrastOptions.risk_ratio
        ];
        expect(result).toEqual(expectedResult);
      });

      it('should return the contrast options for survival', function() {
        var result = resultPropertiesService.getContrastOptions(SURVIVAL);
        var expectedResult = [
          contrastOptions.hazard_ratio
        ];
        expect(result).toEqual(expectedResult);
      });
    });

    describe('logChanged', function() {
      it('should (re-)add the default result properties', function() {
        var variable = {
          isLog: true,
          armOrContrast: CONTRAST
        };
        var result = resultPropertiesService.logChanged(variable);
        var expectedResult = {
          isLog: true,
          armOrContrast: CONTRAST,
          resultProperties: variableTypeDetails[CONTRAST]
        };
        expect(result).toEqual(expectedResult);
      });

      it('should remove the standard error as an option and mark the confidence interval as included at 95%.', function() {
        var variable = {
          isLog: false,
          armOrContrast: CONTRAST,
          resultProperties: variableTypeDetails[CONTRAST]
        };
        var result = resultPropertiesService.logChanged(variable);
        var expectedResult = {
          isLog: false,
          armOrContrast: CONTRAST,
          resultProperties: [
            variableTypeDetails[CONTRAST].confidence_interval_width,
            variableTypeDetails[CONTRAST].confidence_interval_lower_bound,
            variableTypeDetails[CONTRAST].confidence_interval_upper_bound
          ],
          selectedResultProperties: [variableTypeDetails[CONTRAST].confidence_interval_width],
          confidenceIntervalWidth: 95
        };
        expectedResult.resultProperties[0].isSelected = true;
        expectedResult.resultProperties[1].isSelected = false;
        expectedResult.resultProperties[2].isSelected = false;
        expectedResult.selectedResultProperties[0].isSelected = true;
        expect(result).toEqual(expectedResult);
      });
    });
  });
});
