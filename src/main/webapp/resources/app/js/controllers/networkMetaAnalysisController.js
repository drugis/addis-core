'use strict';
define(['d3'], function(d3) {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', 'OutcomeResource', 'InterventionResource',
    'TrialverseTrialDataResource', 'NetworkMetaAnalysisService', 'ModelResource'
  ];

  var NetworkMetaAnalysisController = function($scope, $q, $state, $stateParams, OutcomeResource,
    InterventionResource, TrialverseTrialDataResource, NetworkMetaAnalysisService, ModelResource) {
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });
    $scope.trialData = {};
    $scope.interventions = InterventionResource.query({
      projectId: $stateParams.projectId
    });

    function matchOutcome(outcome) {
      return $scope.analysis.outcome && $scope.analysis.outcome.id === outcome.id;
    }

    function getSemanticInterventionUri(object) {
      return object.semanticInterventionUri;
    }

    function updateView(trialverseData) {
      createNetwork(trialverseData);
      return NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData);
    }

    function drawLine(enter, fromId, toId, circleData) {
      enter.append('line')
        .attr('x1', circleData[fromId].cx)
        .attr('y1', circleData[fromId].cy)
        .attr('x2', circleData[toId].cx)
        .attr('y2', circleData[toId].cy)
        .attr('stroke', 'black')
        .attr('stroke-width', Math.random() * 10 + 1);
    }

    function createNetwork(trialverseData) {
      var parent = angular.element('#network-graph').parent();
      var n = 10.0;
      var angle = 2.0 * Math.PI / n;
      var originX = parent.width() / 2;
      var originY = parent.width() / 4;
      var margin = 100;
      var circleSize = 15;
      var radius = originY - margin / 2;
      var svg = d3.select('#network-graph')
        .append('svg')
        .attr('width', parent.width())
        .attr('height', parent.width() / 2);

      var circleData = [];

      for (var i = 0; i < n; ++i) {
        circleData.push({
          id: i,
          r: Math.random() * circleSize,
          cx: originX - radius * Math.cos(angle * i),
          cy: originY + radius * Math.sin(angle * i),
          label: 'hoi connor lange text ' + i
        });
      }

      drawLine(svg, 1, 2, circleData);
      drawLine(svg, 4, 2, circleData);
      drawLine(svg, 3, 2, circleData);
      drawLine(svg, 1, 6, circleData);
      drawLine(svg, 5, 2, circleData);
      drawLine(svg, 2, 4, circleData);
      var enter = svg.selectAll('g')
        .data(circleData)
        .enter()
        .append('g')
        .attr('transform', function(d) { return 'translate(' + d.cx+','+d.cy+')';});

      enter.append('circle')
        .style('fill', 'grey')
        .attr('r', circleSize)

      enter.append('text')
        .attr('dx', -50)
        .text(function(d){return d.label;});


    }




    function reloadTable() {
      TrialverseTrialDataResource
        .get({
          id: $scope.project.trialverseId,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: $scope.interventionUris
        })
        .$promise
        .then(updateView)
        .then(function(tableRows) {
          $scope.trialData = tableRows;
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
          reloadTable();
        }
      });

    $scope.saveAnalysis = function() {
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        reloadTable();
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

  };

  return dependencies.concat(NetworkMetaAnalysisController);
});