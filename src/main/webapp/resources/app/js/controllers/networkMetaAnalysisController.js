'use strict';
define(['d3'], function(d3) {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', 'OutcomeResource', 'InterventionResource',
    'TrialverseTrialDataResource', 'NetworkMetaAnalysisService', 'ModelResource'
  ];

  var NetworkMetaAnalysisController = function($scope, $q, $state, $stateParams, OutcomeResource,
    InterventionResource, TrialverseTrialDataResource, NetworkMetaAnalysisService, ModelResource) {
    $scope.isNetworkDisconnected = true;
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.models = ModelResource.query({
      projectId: $stateParams.projectId,
      analysisId: $stateParams.analysisId
    });
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });
    $scope.trialData = {};
    $scope.interventions = InterventionResource.query({
      projectId: $stateParams.projectId
    });
    $scope.tableHasAmbiguousArm = false;
    $scope.hasLessThanTwoInterventions = false;
    $scope.hasModel = true;

    $q
      .all([
        $scope.analysis.$promise,
        $scope.project.$promise,
        $scope.models.$promise,
        $scope.outcomes.$promise,
        $scope.interventions.$promise
      ])
      .then(function() {
        $scope.hasModel = $scope.models.length > 0;
        $scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions($scope.interventions, $scope.analysis.includedInterventions);
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        if ($scope.analysis.outcome) {
          reloadModel();
        }
      });

    function matchOutcome(outcome) {
      return $scope.analysis.outcome && $scope.analysis.outcome.id === outcome.id;
    }

    function addIncludedInterventionUri(memo, intervention) {
      if (intervention.isIncluded) {
        memo.push(intervention.semanticInterventionUri);
      }
      return memo;
    }

    function drawEdge(enter, fromId, toId, width, circleData) {
      enter.append('line')
        .attr('x1', circleData[fromId].cx)
        .attr('y1', circleData[fromId].cy)
        .attr('x2', circleData[toId].cx)
        .attr('y2', circleData[toId].cy)
        .attr('stroke', 'black')
        .attr('stroke-width', width);
    }

    function tanh(x) {
      var y = Math.exp(2 * x);
      return (y - 1) / (y + 1);
    }

    function drawNetwork(network) {
      var parent = angular.element('#network-graph').parent();
      var n = network.interventions.length;
      var angle = 2.0 * Math.PI / n;
      var originX = parent.width() / 2;
      var originY = parent.width() / 2;
      var margin = 200;
      var radius = originY - margin / 2;
      var circleMaxSize = 30;
      var circleMinSize = 5;
      d3.select('#network-graph').selectAll('g').remove();
      d3.select('#network-graph').selectAll('line').remove();
      var svg = d3.select('#network-graph').select('svg')
        .attr('width', parent.width())
        .attr('height', parent.width());


      var circleData = [];

      _.each(network.interventions, function(intervention, i) {
        var circleDatum = {
          id: intervention.name,
          r: circleMinSize + ((circleMaxSize - circleMinSize) * tanh(intervention.sampleSize / 10000)),
          cx: originX - radius * Math.cos(angle * i),
          cy: originY + radius * Math.sin(angle * i)
        };
        circleData[intervention.name] = circleDatum;
        circleData.push(circleDatum);
      });

      _.each(network.edges, function(edge) {
        drawEdge(svg, edge.from.name, edge.to.name, edge.numberOfStudies, circleData);
      });

      var enter = svg.selectAll('g')
        .data(circleData)
        .enter()
        .append('g')
        .attr('transform', function(d) {
          return 'translate(' + d.cx + ',' + d.cy + ')';
        });

      enter.append('circle')
        .style('fill', 'grey')
        .attr('r', function(d) {
          return d.r;
        });

      var labelMargin = 5;
      var nearCenterMargin = 20;

      function nearCenter(d) {
        var delta = d.cx - originX;
        return delta < -nearCenterMargin ? -1 : (delta > nearCenterMargin ? 1 : 0);
      }

      var cos45 = Math.sqrt(2) * 0.5;
      enter.append('text')
        .attr('dx', function(d) {
          var offset = cos45 * d.r + labelMargin;
          return nearCenter(d) * offset;
        })
        .attr('dy', function(d) {
          var offset = (nearCenter(d) === 0 ? d.r : cos45 * d.r) + labelMargin;
          return (d.cy >= originY ? offset : -offset);
        })
        .attr('text-anchor', function(d) {
          switch (nearCenter(d)) {
            case -1:
              return 'end';
            case 0:
              return 'middle';
            case 1:
              return 'start';
          }
        })
        .attr('dominant-baseline', function(d) {
          if (nearCenter(d) !== 0) {
            return 'central';
          }
          if (d.cy - originY < 0) {
            return 'alphabetic';
          } // text-after-edge doesn't seem to work in Chrome
          return 'text-before-edge';
        })
        .style('font-family', 'Droid Sans')
        .style('font-size', 16)
        .text(function(d) {
          return d.id;
        });
    }

    function getIncludedInterventions(interventions) {
      return _.filter(interventions, function(intervention) {
        return intervention.isIncluded;
      });
    }

    function updateNetwork() {
      var includedInterventions = getIncludedInterventions($scope.interventions);
      var network = NetworkMetaAnalysisService.transformTrialDataToNetwork($scope.trialverseData, includedInterventions, $scope.analysis.excludedArms);
      drawNetwork(network);
      $scope.isNetworkDisconnected = NetworkMetaAnalysisService.isNetworkDisconnected(network);
    }

    function reloadModel() {
      var includedInterventionUris = _.reduce($scope.interventions, addIncludedInterventionUri, []);
      TrialverseTrialDataResource
        .get({
          namespaceUid: $scope.project.namespaceUid,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: includedInterventionUris,
          version: $scope.project.datasetVersion
        })
        .$promise
        .then(function(trialverseData) {
          $scope.trialverseData = trialverseData;
          updateNetwork();
          var includedInterventions = getIncludedInterventions($scope.interventions);
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData, includedInterventions, $scope.analysis.excludedArms);
          $scope.tableHasAmbiguousArm =
            NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
          $scope.hasLessThanTwoInterventions = getIncludedInterventions($scope.interventions).length < 2;
        });
    }

    $scope.changeArmExclusion = function(dataRow) {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, $scope.analysis);
      updateNetwork();
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        $scope.tableHasAmbiguousArm =
          NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
      });
    };

    $scope.lessThanTwoInterventionArms = function(dataRow) {
      var matchedAndIncludedRows = _.filter(dataRow.studyRows, function(studyRow) {
        return studyRow.intervention !== 'unmatched' && studyRow.included;
      });
      var matchedInterventions = _.uniq(_.pluck(matchedAndIncludedRows, 'intervention'));
      return matchedInterventions.length < 2;
    };

    $scope.changeInterventionInclusion = function(intervention) {
      $scope.analysis.includedInterventions =
        NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      if ($scope.trialverseData && !intervention.isIncluded) {
        $scope.analysis.excludedArms = NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, $scope.analysis, $scope.trialverseData);
      }
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        $scope.tableHasAmbiguousArm =
          NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
        reloadModel();
      });
    };

    $scope.changeSelectedOutcome = function() {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis.excludedArms = [];
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        reloadModel();
      });
    };

    $scope.createModelAndGoToModel = function() {
      var model = ModelResource.save($stateParams, {});
      model.$promise.then(function(model) {
        $state.go('analysis.model', {
          modelId: model.id
        });
      });
    };

    $scope.goToModel = function() {
      $state.go('analysis.model', {
        modelId: $scope.models[0].id
      });
    };

    $scope.doesInterventionHaveAmbiguousArms = function(drugId, studyUid) {
      return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, studyUid, $scope.trialverseData, $scope.analysis);
    };

  };

  return dependencies.concat(NetworkMetaAnalysisController);
});