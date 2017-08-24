'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'CacheService', 'AnalysisService'];
  var DefaultReportService = function($q, CacheService, AnalysisService) {
    var TYPE_STRING = {
      network: 'evidence synthesis',
      pairwise: 'pair-wise meta-analysis',
      'node-split': 'node-splitting analysis',
      regression: 'meta-regression'
    };

    //public

    function generateDefaultReport(projectId) {
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
      //   [ ] scenarios (names, links)
      var params = {
        projectId: projectId
      };
      var dataPromises = [
        CacheService.getProject(params),
        CacheService.getOutcomes(params),
        CacheService.getInterventions(params),
        CacheService.getCovariates(params),
        CacheService.getAnalyses(params),
        CacheService.getModelsByProject(params)
      ];
      return $q.all(dataPromises).then(function(data) {
        var
          project = data[0],
          outcomes = data[1],
          interventions = data[2],
          covariates = data[3],
          analyses = _.reject(data[4], 'isArchived'),
          models = _.reject(data[5], 'isArchived');
        var report = renderReport(project, outcomes, interventions, covariates, analyses, models);
        return report;
      });
    }

    /// private

    function renderReport(project, outcomes, interventions, covariates, analyses, models) {
      var analysesWithModels = _.map(analyses, function(analysis) {
        return _.extend({}, analysis, {
          models: _.filter(models, ['analysisId', analysis.id])
        });
      });
      return generateOutcomeList(outcomes) +
        generateInterventionList(interventions) +
        generateCovariateList(covariates) +
        generateNmaList(project, analysesWithModels) +
        generateBrList(project, analyses);
    }

    function generateOutcomeList(outcomes) {
      var result = '###Outcomes\n';
      if (outcomes.length === 0) {
        return result + '*No outcomes defined*\n';
      }
      return _.reduce(outcomes, function(accum, outcome) {
        return accum + ' - ' + outcome.name + '\n';
      }, result) + '\n';
    }

    function generateInterventionList(interventions) {
      var result = '###Interventions\n';
      if (interventions.length === 0) {
        return result + '*No interventions defined*\n';
      }
      return _.reduce(interventions, function(accum, intervention) {
        return accum + ' - ' + intervention.name + '\n';
      }, result) + '\n';
    }

    function generateCovariateList(covariates) {
      var result = '###Covariates\n';
      if (covariates.length === 0) {
        return result + '*No covariates defined*\n';
      }
      return _.reduce(covariates, function(accum, covariate) {
        return accum + ' - ' + covariate.name + '\n';
      }, result) + '\n';
    }

    function renderNma(project, accum, analysis) {
      var title = '\n####Analysis: ' + analysis.title + '\n';
      var link = '[Details](#/users/' + project.owner.id + '/projects/' + project.id + '/nma/' + analysis.id + ')  \n';
      var outcome = '**Outcome**: ' + analysis.outcome.name + '  \n';
      var networkPlot = '[[[network-plot analysis-id="' + analysis.id + '"]]]\n';
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
      return _.reduce(evidenceSyntheses, _.partial(renderNma, project), result)  + '\n';
    }

    function renderBr(project, accum, analysis) {
      var title = '\n####Analysis: ' + analysis.title + '\n';
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
      generateDefaultReport: generateDefaultReport
    };
  };
  return dependencies.concat(DefaultReportService);
});
