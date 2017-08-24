'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var ReportSubstitutionService = function() {
    var TYPE_STRING = {
      network: 'evidence synthesis',
      pairwise: 'pair-wise meta-analysis',
      'node-split': 'node-splitting analysis',
      regression: 'meta-regression'
    };

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

    function generateDefaultReport(project, outcomes, interventions, covariates, analyses, models) {
      // [✓] outcome list
      // [✓] intervention list
      // [✓] covariate list
      // [~] nmas
      //   [✓] name, link (view details)
      //   [✓] outcome
      //   [✓] network graph
      //   [~] primary model
      //      [✓] name
      //      [✓] settings
      //      [✓] link
      //      [✓] evidence synthesis: rel effects plot
      //      [✓] pairwise analysis: forest plot
      //      [~] nodesplit: direct/indirect effects plot
      //      [~] regression: covariate effects plot
      //   [✓] secondary models links
      // [~] brs
      //   [ ] name
      //   [ ] link
      //   [ ] data sources for each outcome, links
      //   [ ] effects table
      //   [ ] trade-offs and rank acceptability plot for default problem+scenario (link with details)
      //   [ ] subproblems (names, links)
      //   [ ] scenarios (names, linke)
      analyses = _.map(analyses, function(analysis) {
        return _.extend({}, analysis, {
          models: _.filter(models, ['analysisId', analysis.id])
        });
      });
      return generateOutcomeList(outcomes) +
        generateInterventionList(interventions) +
        generateCovariateList(covariates) +
        generateNmaList(project, analyses) +
        generateBrList(project, analyses)
        ;
    }

    function generateOutcomeList(outcomes) {
      var result = '###Outcomes\n';
      if (outcomes.length === 0) {
        return result + '*No outcomes defined*\n';
      }
      return _.reduce(outcomes, function(accum, outcome) {
        return accum + ' - ' + outcome.name + '\n';
      }, result);
    }

    function generateInterventionList(interventions) {
      var result = '###Interventions\n';
      if (interventions.length === 0) {
        return result + '*No interventions defined*\n';
      }
      return _.reduce(interventions, function(accum, intervention) {
        return accum + ' - ' + intervention.name + '\n';
      }, result);
    }

    function generateCovariateList(covariates) {
      var result = '###Covariates\n';
      if (covariates.length === 0) {
        return result + '*No covariates defined*\n';
      }
      return _.reduce(covariates, function(accum, covariate) {
        return accum + ' - ' + covariate.name + '\n';
      }, result);
    }

    function renderNma(project, accum, analysis) {
      var title = '####Analysis: ' + analysis.title + '\n';
      var link = '[Details](#/users/' + project.owner.id + '/projects/' + project.id + '/nma/' + analysis.id + ')  \n';
      var outcome = '**Outcome**: ' + analysis.outcome.name + '  \n';
      var networkPlot = '[[[network-plot analysis-id="' + analysis.id + '"]]]';
      var primaryModelText = renderPrimaryModel(project, analysis);
      var secondaryModelLinks = renderSecondaryModels(project, analysis);
      return accum + title + link + outcome + networkPlot + primaryModelText + secondaryModelLinks;
    }

    function renderSecondaryModels(project, analysis) {
      var secondaryModels = _.reject(analysis.models, ['id', analysis.primaryModel]);
      if (secondaryModels.length) {
        return _.reduce(secondaryModels, function(accum, model) {
          return accum + ' - [' + model.title + '](' + '#/users/' + project.owner.id + '/projects/' +
            project.id + '/nma/' + analysis.id + '/models/' + model.id + ')\n';
        }, '**Secondary models**\n');
      }
      return '';
    }

    function renderModelPlot(analysis, model) {
      switch (model.modelType.type) {
        case 'network':
          //analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
          return '[[[relative-effects-plot analysis-id="' + analysis.id + '" model-id="' + model.id + '" baseline-treatment-id="' +
            analysis.interventionInclusions[0].interventionId + '"]]]\n';
        case 'pairwise':
          return '[[[forest-plot analysis-id="' + analysis.id + '" model-id="' + model.id + '"]]]\n';
        case 'node-split':
          return 'Not yet implemented  \n';
        case 'regression':
          return 'Not yet implemented  \n';
      }
    }

    function renderPrimaryModel(project, analysis) {
      var primaryModelBase = '**Primary model**: ';
      if (analysis.primaryModel) {
        var primaryModel = _.find(analysis.models, ['id', analysis.primaryModel]);
        var modelPart = primaryModel.linearModel === 'fixed' ? 'fixed effect' : 'random effects';
        var typePart = TYPE_STRING[primaryModel.modelType.type];
        var scalePart = AnalysisService.getScaleName(primaryModel);

        var titelLink = '[' + primaryModel.title + '](#/users/' + project.owner.id + '/projects/' +
          project.id + '/nma/' + analysis.id + '/models/' + primaryModel.id + ')  \n';
        var modelSettings = '**model settings**: ' + modelPart + ' ' + typePart + ' on the ' + scalePart + ' scale.  \n';
        var modelPlot = renderModelPlot(analysis, primaryModel);
        return primaryModelBase + titelLink + modelSettings + modelPlot;
      } else {
        return primaryModelBase + '*No primary model set*  \n';
      }
    }

    function generateNmaList(project, analyses) {
      var result = '###Evidence syntheses\n';
      var evidenceSyntheses = _.filter(analyses, ['analysisType', 'Evidence synthesis']);
      if (evidenceSyntheses.length === 0) {
        return result + '*No evidence syntheses defined*\n';
      }
      return _.reduce(evidenceSyntheses, _.partial(renderNma, project), result);
    }

    //     function renderNma(project, accum, analysis) {
    //   var title = '####Analysis: ' + analysis.title + '\n';
    //   var link = '[Details](#/users/' + project.owner.id + '/projects/' + project.id + '/nma/' + analysis.id + ')  \n';
    //   var outcome = '**Outcome**: ' + analysis.outcome.name + '  \n';
    //   var networkPlot = '[[[network-plot analysis-id="' + analysis.id + '"]]]';
    //   var primaryModelText = renderPrimaryModel(project, analysis);
    //   var secondaryModelLinks = renderSecondaryModels(project, analysis);
    //   return accum + title + link + outcome + networkPlot + primaryModelText + secondaryModelLinks;
    // }


    function renderBr(project, accum, analysis) {
      var title = '####Analysis: ' + analysis.title + '\n';
      var link = '[Details](#/users/' + project.owner.id + '/projects/' + project.id + '/br/' + analysis.id + ')  \n';
      return accum + title + link + '*further benefit-risk display functionality coming soon* \n';
    }

    function generateBrList(project, analyses) {
      var result = '###Benefit-risk analyses\n';
      var benefitRiskAnalyses = _.filter(analyses, ['analysisType', 'Benefit-risk analysis']);
      if (benefitRiskAnalyses.length === 0) {
        return result + '*No benefit-risk analyses defined*\n';
      }
      return _.reduce(benefitRiskAnalyses, _.partial(renderBr, project), result);

    }

    return {
      inlineDirectives: inlineDirectives,
      getDirectiveBuilder: getDirectiveBuilder,
      getAllowedModels: getAllowedModels,
      getDecoratedSyntheses: getDecoratedSyntheses,
      getShowSettings: getShowSettings,
      generateDefaultReport: generateDefaultReport
    };
  };
  return dependencies.concat(ReportSubstitutionService);
});
