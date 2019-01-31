'use strict';

define(['angular'], function(angular) {
  var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
  var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';
  var ONTOLOGY_BASE = 'http://trials.drugis.org/ontology#';
  var ARM_LEVEL_TYPE = 'ontology:arm_level_data';
  var CONTRAST_TYPE = 'ontology:contrast_data';
  var DICHOTOMOUS_TYPE = 'ontology:dichotomous';
  var CONTINUOUS_TYPE = 'ontology:continuous';
  var SURVIVAL_TYPE = 'ontology:survival';

  var ARM_RESULT_PROPERTY_TYPES = [
    'sample_size',
    'mean',
    'median',
    'geometric_mean',
    'log_mean',
    'least_squares_mean',
    'quantile_0.05',
    'quantile_0.95',
    'quantile_0.025',
    'quantile_0.975',
    'min',
    'max',
    'geometric_coefficient_of_variation',
    'first_quartile',
    'third_quartile',
    'standard_deviation',
    'standard_error',
    'count',
    'event_count',
    'percentage',
    'proportion',
    'exposure',
    'hazard_ratio'
  ];

  var CONTRAST_RESULT_PROPERTY_TYPES = [
    'hazard_ratio',
    'odds_ratio',
    'risk_ratio',
    'continuous_mean_difference',
    'standardized_mean_difference',
    'hazard_ratio',
    'standard_error',
    'confidence_interval_lower_bound',
    'confidence_interval_upper_bound'
  ];

  var RESULT_PROPERTY_TYPES = {
    'ontology:arm_level_data': ARM_RESULT_PROPERTY_TYPES,
    'ontology:contrast_data': CONTRAST_RESULT_PROPERTY_TYPES
  };

  var ARM_RESULT_PROPERTY_TYPE_DETAILS = {
    sample_size: {
      type: 'sample_size',
      label: 'N',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'sample_size',
      dataType: INTEGER_TYPE,
      variableTypes: [CONTINUOUS_TYPE, DICHOTOMOUS_TYPE],
      category: 'Sample size',
      lexiconKey: 'sample-size',
      analysisReady: true,
      isAlwaysPositive: true
    },
    mean: {
      type: 'mean',
      label: 'mean',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'mean',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Central tendency',
      lexiconKey: 'mean',
      analysisReady: true
    },
    median: {
      type: 'median',
      label: 'median',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'median',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Central tendency',
      lexiconKey: 'median',
      analysisReady: false
    },
    geometric_mean: {
      type: 'geometric_mean',
      label: 'geometric mean',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'geometric_mean',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Central tendency',
      lexiconKey: 'geometric-mean',
      analysisReady: false
    },
    log_mean: {
      type: 'log_mean',
      label: 'log mean',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'log_mean',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Central tendency',
      lexiconKey: 'log-mean',
      analysisReady: false
    },
    least_squares_mean: {
      type: 'least_squares_mean',
      label: 'least squares mean',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'least_squares_mean',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Central tendency',
      lexiconKey: 'least-squares-mean',
      analysisReady: false
    },
    hazard_ratio: {
      type: 'hazard_ratio',
      label: 'hazard ratio',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'hazard_ratio',
      dataType: DOUBLE_TYPE,
      variableTypes: [SURVIVAL_TYPE],
      lexiconKey: 'hazard-ratio',
      analysisReady: false
    },
    'quantile_0.05': {
      type: 'quantile_0.05',
      label: '5% quantile',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'quantile_0.05',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Quantiles',
      lexiconKey: 'quantile-0.05',
      analysisReady: false
    },
    'quantile_0.95': {
      type: 'quantile_0.95',
      label: '95% quantile',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'quantile_0.95',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Quantiles',
      lexiconKey: 'quantile-0.95',
      analysisReady: false
    },
    'quantile_0.025': {
      type: 'quantile_0.025',
      label: '2.5% quantile',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'quantile_0.025',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE, SURVIVAL_TYPE],
      category: 'Quantiles',
      lexiconKey: 'quantile-0.025',
      analysisReady: false
    },
    'quantile_0.975': {
      type: 'quantile_0.975',
      label: '97.5% quantile',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'quantile_0.975',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE, SURVIVAL_TYPE],
      category: 'Quantiles',
      lexiconKey: 'quantile-0.975',
      analysisReady: false
    },
    min: {
      type: 'min',
      label: 'min',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'min',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Dispersion',
      lexiconKey: 'min',
      analysisReady: false
    },
    max: {
      type: 'max',
      label: 'max',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'max',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Dispersion',
      lexiconKey: 'max',
      analysisReady: false
    },
    geometric_coefficient_of_variation: {
      type: 'geometric_coefficient_of_variation',
      label: 'geometric coefficient of variation',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'geometric_coefficient_of_variation',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Dispersion',
      lexiconKey: 'geometric-coefficient-of-variation',
      analysisReady: false
    },
    first_quartile: {
      type: 'first_quartile',
      label: 'first quartile',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'first_quartile',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Quantiles',
      lexiconKey: 'first-quartile',
      analysisReady: false
    },
    third_quartile: {
      type: 'third_quartile',
      label: 'third quartile',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'third_quartile',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Quantiles',
      lexiconKey: 'third-quartile',
      analysisReady: false
    },
    standard_deviation: {
      type: 'standard_deviation',
      label: 'standard deviation',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'standard_deviation',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Dispersion',
      lexiconKey: 'standard-deviation',
      analysisReady: true,
      isAlwaysPositive: true
    },
    standard_error: {
      type: 'standard_error',
      label: 'standard error',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'standard_error',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      category: 'Dispersion',
      lexiconKey: 'standard-error',
      analysisReady: true,
      isAlwaysPositive: true
    },
    event_count: {
      type: 'event_count',
      label: 'number of events',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'event_count',
      dataType: INTEGER_TYPE,
      variableTypes: [DICHOTOMOUS_TYPE],
      lexiconKey: 'event-count',
      analysisReady: false,
      isAlwaysPositive: true
    },
    count: {
      type: 'count',
      label: 'subjects with event',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'count',
      dataType: INTEGER_TYPE,
      variableTypes: [DICHOTOMOUS_TYPE, SURVIVAL_TYPE],
      lexiconKey: 'count',
      analysisReady: true,
      isAlwaysPositive: true
    },
    percentage: {
      type: 'percentage',
      label: 'percentage with event',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'percentage',
      dataType: DOUBLE_TYPE,
      variableTypes: [DICHOTOMOUS_TYPE],
      lexiconKey: 'percentage',
      analysisReady: false,
      isAlwaysPositive: true
    },
    proportion: {
      type: 'proportion',
      label: 'proportion with event',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'proportion',
      dataType: DOUBLE_TYPE,
      variableTypes: [DICHOTOMOUS_TYPE],
      lexiconKey: 'proportion',
      analysisReady: false,
      isAlwaysPositive: true
    },
    exposure: {
      type: 'exposure',
      label: 'total observation time',
      armOrContrast: ARM_LEVEL_TYPE,
      uri: ONTOLOGY_BASE + 'exposure',
      dataType: DOUBLE_TYPE,
      variableTypes: [SURVIVAL_TYPE],
      lexiconKey: 'exposure',
      analysisReady: true
    }
  };

  var TIME_SCALE_OPTIONS = [{
    label: 'Days',
    duration: 'P1D'
  }, {
    label: 'Weeks',
    duration: 'P1W'
  }, {
    label: 'Months',
    duration: 'P1M'
  }, {
    label: 'Years',
    duration: 'P1Y'
  }];

  var CONTRAST_RESULT_PROPERTY_TYPE_DETAILS = {
    standard_error: {
      type: 'standard_error',
      label: 'standard error',
      armOrContrast: CONTRAST_TYPE,
      uri: ONTOLOGY_BASE + 'standard_error',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE, DICHOTOMOUS_TYPE, SURVIVAL_TYPE],
      category: 'Dispersion',
      lexiconKey: 'standard-error',
      analysisReady: true,
      isAlwaysPositive: true
    },
    confidence_interval_width: {
      type: 'confidence_interval',
      label: 'confidence interval',
      armOrContrast: CONTRAST_TYPE,
      uri: ONTOLOGY_BASE + 'confidence_interval_width',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE, DICHOTOMOUS_TYPE, SURVIVAL_TYPE],
      category: 'Dispersion',
      lexiconKey: 'confidence-interval',
      analysisReady: true,
      isAlwaysPositive: true
    },
    confidence_interval_lower_bound: {
      type: 'confidence_interval_lower_bound',
      label: 'confidence interval lower bound',
      armOrContrast: CONTRAST_TYPE,
      uri: ONTOLOGY_BASE + 'confidence_interval_lower_bound',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE, DICHOTOMOUS_TYPE, SURVIVAL_TYPE],
      category: 'Dispersion',
      lexiconKey: 'confidence-interval',
      analysisReady: true,
      hiddenSelection: true
    },
    confidence_interval_upper_bound: {
      type: 'confidence_interval_upper_bound',
      label: 'confidence interval upper bound',
      armOrContrast: CONTRAST_TYPE,
      uri: ONTOLOGY_BASE + 'confidence_interval_upper_bound',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE, DICHOTOMOUS_TYPE, SURVIVAL_TYPE],
      category: 'Dispersion',
      lexiconKey: 'confidence-interval',
      analysisReady: true,
      hiddenSelection: true
    }
  };

  var CONTRAST_OPTIONS_DETAILS = {
    odds_ratio: {
      type: 'odds_ratio',
      label: 'log odds ratio',
      armOrContrast: CONTRAST_TYPE,
      isLog: true,
      category: 'Central tendency',
      uri: ONTOLOGY_BASE + 'odds_ratio',
      dataType: DOUBLE_TYPE,
      variableTypes: [DICHOTOMOUS_TYPE],
      lexiconKey: 'odds-ratio',
      analysisReady: true
    },
    risk_ratio: {
      type: 'risk_ratio',
      label: 'log risk ratio',
      armOrContrast: CONTRAST_TYPE,
      isLog: true,
      category: 'Central tendency',
      uri: ONTOLOGY_BASE + 'risk_ratio',
      dataType: DOUBLE_TYPE,
      variableTypes: [DICHOTOMOUS_TYPE],
      lexiconKey: 'risk-ratio',
      analysisReady: true
    },
    continuous_mean_difference: { //continuous as prefix because of JSON-ld tranform replacing mean with the arm based mean.
      type: 'continuous_mean_difference',
      label: 'mean difference',
      armOrContrast: CONTRAST_TYPE,
      category: 'Central tendency',
      uri: ONTOLOGY_BASE + 'continuous_mean_difference',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      lexiconKey: 'mean-difference',
      analysisReady: true
    },
    standardized_mean_difference: {
      type: 'standardized_mean_difference',
      label: 'standardized mean difference',
      armOrContrast: CONTRAST_TYPE,
      category: 'Central tendency',
      uri: ONTOLOGY_BASE + 'standardized_mean_difference',
      dataType: DOUBLE_TYPE,
      variableTypes: [CONTINUOUS_TYPE],
      lexiconKey: 'standardized-mean-difference',
      analysisReady: true
    },
    hazard_ratio: {
      type: 'hazard_ratio',
      label: 'log hazard ratio',
      armOrContrast: CONTRAST_TYPE,
      isLog: true,
      category: 'Central tendency',
      uri: ONTOLOGY_BASE + 'hazard_ratio',
      dataType: DOUBLE_TYPE,
      variableTypes: [SURVIVAL_TYPE],
      lexiconKey: 'hazard-ratio',
      analysisReady: true
    },
  };

  var RESULT_PROPERTY_TYPE_DETAILS = {
    'ontology:arm_level_data': ARM_RESULT_PROPERTY_TYPE_DETAILS,
    'ontology:contrast_data': CONTRAST_RESULT_PROPERTY_TYPE_DETAILS
  };

  var DEFAULT_ARM_RESULT_PROPERTIES = {
    'ontology:continuous': [
      ARM_RESULT_PROPERTY_TYPE_DETAILS.sample_size,
      ARM_RESULT_PROPERTY_TYPE_DETAILS.mean,
      ARM_RESULT_PROPERTY_TYPE_DETAILS.standard_deviation
    ],
    'ontology:dichotomous': [
      ARM_RESULT_PROPERTY_TYPE_DETAILS.sample_size,
      ARM_RESULT_PROPERTY_TYPE_DETAILS.count
    ],
    'ontology:categorical': [],
    'ontology:survival': [
      ARM_RESULT_PROPERTY_TYPE_DETAILS.count,
      ARM_RESULT_PROPERTY_TYPE_DETAILS.exposure
    ]
  };

  var DEFAULT_CONTRAST_RESULT_PROPERTIES = {
    'ontology:dichotomous': [
      CONTRAST_RESULT_PROPERTY_TYPE_DETAILS.standard_error
    ],
    'ontology:continuous': [
      CONTRAST_RESULT_PROPERTY_TYPE_DETAILS.standard_error
    ],
    'ontology:categorical': [],
    'ontology:survival': [
      CONTRAST_RESULT_PROPERTY_TYPE_DETAILS.standard_error
    ]
  };

  var DEFAULT_RESULT_PROPERTIES = {
    'ontology:arm_level_data': DEFAULT_ARM_RESULT_PROPERTIES,
    'ontology:contrast_data': DEFAULT_CONTRAST_RESULT_PROPERTIES
  };

  return angular.module('addis.resultsConstants', [])
    .constant('DOUBLE_TYPE', DOUBLE_TYPE)
    .constant('INTEGER_TYPE', INTEGER_TYPE)

    .constant('ONTOLOGY_BASE', ONTOLOGY_BASE)
    .constant('ARM_LEVEL_TYPE', ARM_LEVEL_TYPE)
    .constant('CONTRAST_TYPE', CONTRAST_TYPE)

    .constant('DICHOTOMOUS_TYPE', DICHOTOMOUS_TYPE)
    .constant('CONTINUOUS_TYPE', CONTINUOUS_TYPE)
    .constant('SURVIVAL_TYPE', SURVIVAL_TYPE)

    .constant('RESULT_PROPERTY_TYPES', RESULT_PROPERTY_TYPES)
    .constant('RESULT_PROPERTY_TYPE_DETAILS', RESULT_PROPERTY_TYPE_DETAILS)
    .constant('CONTRAST_OPTIONS_DETAILS', CONTRAST_OPTIONS_DETAILS)
    .constant('DEFAULT_RESULT_PROPERTIES', DEFAULT_RESULT_PROPERTIES)
    .constant('TIME_SCALE_OPTIONS', TIME_SCALE_OPTIONS)
    ;
});
