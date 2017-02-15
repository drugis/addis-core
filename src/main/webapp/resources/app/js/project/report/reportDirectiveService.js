'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var ReportStubstitutionService = function() {

    var DIRECTIVES = {
      'network-plot': {
        tag: 'network-plot',
        regex: /\[\[\[(network-plot\s+analysis-id=\&\#34;\d+\&\#34;\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<div style="max-width:500px"><' + replaceQuotes(p1) + '></network-plot></div>';
        },
        builder: function(analysisId) {
          return '[[[network-plot analysis-id="' + analysisId + '"]]]';
        }
      },
      'result-comparison': {
        tag: 'result-comparison',
        regex: /\[\[\[(comparison-result\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+t1=\&\#34;\d+\&\#34;\s+t2=\&\#34;\d+\&\#34;\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></comparison-result>';
        },
        builder: function(analysisId, modelId, t1, t2) {
          return '[[[comparison-result' +
            ' analysis-id="' + analysisId + '"' +
            ' model-id="' + modelId + '"' +
            ' t1="' + t1 + '"' +
            ' t2="' + t2 + '"]]]';
        }
      },
      'relative-effects-table': {
        tag: 'relative-effects-table',
        regex: /\[\[\[(relative-effects-table\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></relative-effects-table>';
        },
        builder: function(analysisId, modelId, regressionLevel) {
          return regressionLevel !== undefined ? '[[[relative-effects-table' + ' analysis-id="' + analysisId + '"' + ' model-id="' +
            modelId + '" regression-level=' + '"' + regressionLevel + '"]]]' : '[[[relative-effects-table' + ' analysis-id="' +
            analysisId + '"' + ' model-id="' + modelId + '"]]]';
        }
      },
      'relative-effects-plot': {
        tag: 'relative-effects-plot',
        regex: /\[\[\[(relative-effects-plot\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+baseline-treatment-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></relative-effects-plot>';
        },
        builder: function(analysisId, modelId, baselineTreatmentId, regressionLevel) {
          return regressionLevel !== undefined && regressionLevel.indexOf('centering') < 0 ?
            '[[[relative-effects-plot' + ' analysis-id="' + analysisId + '"' + ' model-id="' + modelId + '" baseline-treatment-id=' + '"' +
            baselineTreatmentId + '" regression-level=' + '"' + regressionLevel + '"]]]' :
            '[[[relative-effects-plot' + ' analysis-id="' + analysisId + '"' + ' model-id="' + modelId + '" baseline-treatment-id=' + '"' +
            baselineTreatmentId + '"]]]';
        }
      },
      'rank-probabilities-table': {
        tag: 'rank-probabilities-table',
        regex: /\[\[\[(rank-probabilities-table\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></rank-probabilities-table>';
        },
        builder: function(analysisId, modelId, regressionLevel) {
          return regressionLevel !== undefined && regressionLevel.indexOf('centering') < 0 ?
            '[[[rank-probabilities-table' + ' analysis-id="' + analysisId + '"' + ' model-id="' + modelId + '" regression-level=' + '"' + regressionLevel + '"]]]' :
            '[[[rank-probabilities-table' + ' analysis-id="' + analysisId + '"' + ' model-id="' + modelId + '"]]]';
        }
      },
      'rank-probabilities-plot': {
        tag: 'rank-probabilities-plot',
        regex: /\[\[\[(rank-probabilities-plot\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+baseline-treatment-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></rank-probabilities-plot>';
        },
        builder: function(analysisId, modelId, baselineTreatmentId, regressionLevel) {
          return regressionLevel !== undefined && regressionLevel.indexOf('centering') < 0 ?
            '[[[rank-probabilities-plot' + ' analysis-id="' + analysisId + '"' + ' model-id="' + modelId + '" baseline-treatment-id=' + '"' +
            baselineTreatmentId + '" regression-level=' + '"' + regressionLevel + '"]]]' :
            '[[[rank-probabilities-plot' + ' analysis-id="' + analysisId + '"' + ' model-id="' + modelId + '" baseline-treatment-id=' + '"' +
            baselineTreatmentId + '"]]]';
        }
      }
    };

    function replaceQuotes(input) {
      return input.replace(/\&\#34;/g, '"');
    }

    function inlineDirectives(input) {
      _.forEach(DIRECTIVES, function(directive) {
        input = input.replace(directive.regex, directive.replacer);
      });
      return input;
    }

    function getDirectiveBuilder(directiveName) {
      return DIRECTIVES[directiveName].builder;
    }

    function getNonNodeSplitModels(models) {
      return _.chain(models)
        .reject('archived')
        .reject(['modelType.type', 'node-split'])
        .map(function(model) {
          if (model.modelType.type === 'regression' && model.regressor.levels.length) {
            model.regressor.levels = ['centering'].concat(model.regressor.levels);
          }
          return model;
        })
        .value();
    }

    function getDecoratedSyntheses(analyses, models, interventions) {
      return _.chain(analyses)
        .reject('archived')
        .filter(['analysisType', 'Evidence synthesis'])
        .map(function(analysis) {
          analysis.models = _.filter(models, ['analysisId', analysis.id]);
          return analysis;
        })
        .filter('models.length')
        .map(function(analysis) {
          analysis.interventions = _.filter(interventions, function(intervention) {
            return _.find(analysis.interventionInclusions, ['interventionId', intervention.id]);
          });
          return analysis;
        })
        .value();
    }

    return {
      inlineDirectives: inlineDirectives,
      getDirectiveBuilder: getDirectiveBuilder,
      getNonNodeSplitModels: getNonNodeSplitModels,
      getDecoratedSyntheses: getDecoratedSyntheses
    };
  };
  return dependencies.concat(ReportStubstitutionService);
});
