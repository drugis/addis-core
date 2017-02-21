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
        builder: function(selections) {
          return '[[[network-plot analysis-id="' + selections.analysis.id + '"]]]';
        },
        showSettings: {
          showSelectModel: false,
          showSelectRegression: false,
          showSelectBaseline: false,
          showSelectInterventions: false,
          showSelectSorting: false
        },
        allowedModelTypes: ['network', 'pairwise', 'node-split', 'regression']
      },
      'comparison-result': {
        tag: 'comparison-result',
        regex: /\[\[\[(comparison-result\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+t1=\&\#34;\d+\&\#34;\s+t2=\&\#34;\d+\&\#34;\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></comparison-result>';
        },
        builder: function(selections) {
          return '[[[comparison-result' +
            ' analysis-id="' + selections.analysis.id + '"' +
            ' model-id="' + selections.model.id + '"' +
            ' t1="' + selections.t1.id + '"' +
            ' t2="' + selections.t2.id + '"]]]';
        },
        showSettings: {
          showSelectModel: true,
          showSelectRegression: false,
          showSelectBaseline: false,
          showSelectInterventions: true,
          showSelectSorting: false
        },
        allowedModelTypes: ['network', 'pairwise']

      },
      'relative-effects-table': {
        tag: 'relative-effects-table',
        regex: /\[\[\[(relative-effects-table\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></relative-effects-table>';
        },
        builder: function(selections) {
          return selections.regressionLevel !== undefined && Number.isInteger(selections.regressionLevel) ? '[[[relative-effects-table' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' +
            selections.model.id + '" regression-level=' + '"' + selections.regressionLevel + '"]]]' : '[[[relative-effects-table' + ' analysis-id="' +
            selections.analysis.id + '"' + ' model-id="' + selections.model.id + '"]]]';
        },
        showSettings: {
          showSelectModel: true,
          showSelectRegression: true,
          showSelectBaseline: false,
          showSelectInterventions: false,
          showSelectSorting: false
        },
        allowedModelTypes: ['network', 'pairwise', 'regression']

      },
      'relative-effects-plot': {
        tag: 'relative-effects-plot',
        regex: /\[\[\[(relative-effects-plot\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+baseline-treatment-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></relative-effects-plot>';
        },
        builder: function(selections) {
          return selections.regressionLevel !== undefined && Number.isInteger(selections.regressionLevel) ?
            '[[[relative-effects-plot' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '" baseline-treatment-id=' + '"' +
            selections.baselineIntervention.id + '" regression-level=' + '"' + selections.regressionLevel + '"]]]' :
            '[[[relative-effects-plot' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '" baseline-treatment-id=' + '"' +
            selections.baselineIntervention.id + '"]]]';
        },
        showSettings: {
          showSelectModel: true,
          showSelectRegression: true,
          showSelectBaseline: true,
          showSelectInterventions: false,
          showSelectSorting: false
        },
        allowedModelTypes: ['network', 'regression']

      },
      'rank-probabilities-table': {
        tag: 'rank-probabilities-table',
        regex: /\[\[\[(rank-probabilities-table\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></rank-probabilities-table>';
        },
        builder: function(selections) {
          return selections.regressionLevel !== undefined && Number.isInteger(selections.regressionLevel) ?
            '[[[rank-probabilities-table' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '" regression-level=' + '"' + selections.regressionLevel + '"]]]' :
            '[[[rank-probabilities-table' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '"]]]';
        },
        showSettings: {
          showSelectModel: true,
          showSelectRegression: true,
          showSelectBaseline: false,
          showSelectInterventions: false,
          showSelectSorting: false
        },
        allowedModelTypes: ['network', 'pairwise', 'regression']

      },
      'rank-probabilities-plot': {
        tag: 'rank-probabilities-plot',
        regex: /\[\[\[(rank-probabilities-plot\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></rank-probabilities-plot>';
        },
        builder: function(selections) {
          return selections.regressionLevel !== undefined && Number.isInteger(selections.regressionLevel) ?
            '[[[rank-probabilities-plot' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '" regression-level=' + '"' + selections.regressionLevel + '"]]]' :
            '[[[rank-probabilities-plot' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '"]]]';
        },
        showSettings: {
          showSelectModel: true,
          showSelectRegression: true,
          showSelectBaseline: false,
          showSelectInterventions: false,
          showSelectSorting: false
        },
        allowedModelTypes: ['network', 'pairwise', 'regression']

      },
      'forest-plot': {
        tag: 'forest-plot',
        regex: /\[\[\[(forest-plot\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></forest-plot>';
        },
        builder: function(selections) {
          return '[[[forest-plot' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '"]]]';
        },
        showSettings: {
          showSelectModel: true,
          showSelectRegression: false,
          showSelectBaseline: false,
          showSelectInterventions: false,
          showSelectSorting: false
        },
        allowedModelTypes: ['pairwise']
      },
      'treatment-effects': {
        tag: 'treatment-effects',
        regex: /\[\[\[(treatment-effects\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+baseline-treatment-id=\&\#34;\d+\&\#34;\s+sorting-type=\&\#34;('alphabetical'|'point-estimate')\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></treatment-effects>';
        },
        builder: function(selections) {
          return selections.regressionLevel !== undefined && Number.isInteger(selections.regressionLevel) ?
            '[[[treatment-effects' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '" baseline-treatment-id=' + '"' +
            selections.baselineIntervention.id + '" sorting-type=' + '"\'' + selections.sortingType + '\'" regression-level=' + '"' + selections.regressionLevel + '"]]]' :
            '[[[treatment-effects' + ' analysis-id="' + selections.analysis.id + '"' + ' model-id="' + selections.model.id + '" baseline-treatment-id=' + '"' +
            selections.baselineIntervention.id + '" sorting-type=' + '"\'' + selections.sortingType + '\'"]]]';
        },
        showSettings: {
          showSelectModel: true,
          showSelectRegression: true,
          showSelectBaseline: true,
          showSelectInterventions: false,
          showSelectSorting: true
        },
        allowedModelTypes: ['network', 'pairwise', 'regression']
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

    function getShowSettings(directiveName) {
      return DIRECTIVES[directiveName].showSettings;
    }

    function getAllowedModels(models, directiveName) {
      return _.chain(models)
        .reject('archived')
        .filter(['runStatus', 'done'])
        .filter(function(model) {
          return _.includes(DIRECTIVES[directiveName].allowedModelTypes, model.modelType.type);
        })
        .map(function(model) {
          if (model.modelType.type === 'regression' && model.regressor.levels && !_.includes(model.regressor.levels, 'centering')) {
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
      getAllowedModels: getAllowedModels,
      getDecoratedSyntheses: getDecoratedSyntheses,
      getShowSettings: getShowSettings
    };
  };
  return dependencies.concat(ReportStubstitutionService);
});
