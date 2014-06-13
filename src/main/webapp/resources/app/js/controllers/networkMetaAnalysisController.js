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
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });
    $scope.trialData = {};
    $scope.interventions = InterventionResource.query({
      projectId: $stateParams.projectId
    });
    $scope.tableHasAmbiguousArm = false;

    function matchOutcome(outcome) {
      return $scope.analysis.outcome && $scope.analysis.outcome.id === outcome.id;
    }

    function getSemanticInterventionUri(object) {
      return object.semanticInterventionUri;
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

    function drawNetwork(network) {
      var parent = angular.element('#network-graph').parent();
      var n = network.interventions.length;
      var angle = 2.0 * Math.PI / n;
      var originX = parent.width() / 2;
      var originY = parent.width() / 2;
      var margin = 200;
      var circleMaxSize = 30;
      var circleMinSize = 5;
      var maxSampleSize = _.max(network.interventions, function(intervention) {
        return intervention.sampleSize;
      }).sampleSize;
      var radius = originY - margin / 2;
      d3.select('#network-graph').selectAll('g').remove();
      d3.select('#network-graph').selectAll('line').remove();
      var svg = d3.select('#network-graph').select('svg')
        .attr('width', parent.width())
        .attr('height', parent.width());


      var circleData = [];

      _.each(network.interventions, function(intervention, i) {
        var circleDatum = {
          id: intervention.name,
          r: maxSampleSize > 0 ?
            Math.max(circleMaxSize * Math.sqrt(intervention.sampleSize) / Math.sqrt(maxSampleSize), circleMinSize) : circleMinSize,
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

    function updateNetwork() {
      var network = NetworkMetaAnalysisService.transformTrialDataToNetwork($scope.trialverseData, $scope.interventions, $scope.analysis.excludedArms);
      drawNetwork(network);
      $scope.isNetworkDisconnected = NetworkMetaAnalysisService.isNetworkDisconnected(network);
    }

    function reloadModel() {
      TrialverseTrialDataResource
        .get({
          id: $scope.project.trialverseId,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: $scope.interventionUris
        })
        .$promise
        .then(function(trialverseData) {
          $scope.trialverseData = trialverseData;
          updateNetwork();
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData, $scope.interventions, $scope.analysis.excludedArms);
        });
    }

    $q
      .all([
        $scope.analysis.$promise,
        $scope.project.$promise,
        $scope.outcomes.$promise,
        $scope.interventions.$promise
      ])
      .then(function() {
        $scope.interventionUris = _.map($scope.interventions, getSemanticInterventionUri);
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        if ($scope.analysis.outcome) {
          reloadModel();
        }
      });

    $scope.changeArmExclusion = function(dataRow) {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, $scope.analysis);
      updateNetwork();
      $scope.saveAnalysis();
    };

    $scope.saveSelectedOutcome = function() {
      $scope.analysis.excludedArms = [];
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        reloadModel();
      });
    };

    $scope.saveAnalysis = function() {
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
      });
    };

    $scope.goToModel = function() {
      var model = ModelResource.save($stateParams, {});
      model.$promise.then(function(model) {
        $state.go('analysis.model', {
          modelId: model.id
        });
      });
    };

    $scope.doesInterventionHaveAmbiguousArms = function(drugId) {
      var isAmbiguousArm = NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, $scope.trialverseData, $scope.analysis);
      $scope.tableHasAmbiguousArm = $scope.tableHasAmbiguousArm || isAmbiguousArm;
      return isAmbiguousArm;
    };

  };

  return dependencies.concat(NetworkMetaAnalysisController);
});