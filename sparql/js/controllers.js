'use strict';

/* Controllers */

angular.module('myApp.controllers', []).
controller('IndicationController', ['$scope', 'SparqlService', function($scope, $sparql) {
  $scope.selected = [];
  $scope.searchString = 'cardio*';
  $scope.source = 'snomed-human-diseases';

  var restrictions = {
    'snomed-human-diseases' : {
      graph: '<http://www.ihtsdo.org/SNOMEDCT/>',
      sparql: '?uri rdfs:subClassOf+ snomed:SCT_64572001 . # IS A disease\n' +
        'MINUS { ?uri rdfs:subClassOf+ snomed:SCT_127326005 } # NOT IS A non-human disease'
    },
    'drugis-indications' : {
      graph: '?g',
      sparql: '?uri rdfs:subClassOf <http://trials.drugis.org/indication> .',
      graphQuery: 'GRAPH <http://trials.drugis.org/namespaces/> {\n' +
        '  ?g rdf:type <http://trials.drugis.org/namespace> .\n' +
        '}'
    }
  };

  var extractValues = function(result) {
    var bindings = result.data.results.bindings;
    return _.map(bindings, function(binding) {
      return _.object(_.map(_.pairs(binding), function(obj) {
        return [ obj[0], obj[1].value ];
      }));
    });
  };

  $scope.query = function() {
    $sparql.query('disease-search', { query: $scope.searchString, restrict: restrictions[$scope.source] }).then(
      function(result) {
        $scope.data = extractValues(result);
        $scope.terms = $scope.data;
        $scope.narrower = [];
        $scope.broader = [];
        $scope.sparql = result.query;
      },
      function(error) {
        console.log(error.cause);
        if (error.query) {
          $scope.sparql = error.query;
        }
      });
  };

  $scope.count = function() {
    $sparql.query('instances', { terms : $scope.selected, restrict: restrictions[$scope.source] }).then(
      function(result) {
        $scope.termCount = extractValues(result)[0].count;
        $scope.sparql = result.query;
      },
      function(error) {
        console.log(error.cause);
        if (error.query) {
          $scope.sparql = error.query;
        }
      });
  };

  $scope.getAdjacent = function(term) { 
    $scope.clicked = term;
    var uri = term.uri;
    var resultsFn = function(assignTo) {
      return function(result) {
        $scope.sparql = result.query;
        $scope.data = extractValues(result);
        $scope[assignTo] = $scope.data;
      };
    };
    var errorFn = function(result) {
      $scope.sparql = result.query;
    };
    var broader = $sparql.query('disease-broader', { uri: uri }).then(resultsFn('broader'), errorFn);
    var narrower = $sparql.query('disease-narrower', { uri: uri }).then(resultsFn('narrower'), errorFn);
  };

  $scope.setCurrent = function(term) {
    $scope.terms = [term];
    $scope.getAdjacent(term);
  };

  $scope.add = function(term) {
    if (!_.find($scope.selected, function(obj) { return obj.uri === term.uri; })) {
      var t = _.clone(term);
      t.transitive = 'exact';
      $scope.selected.push(t);
    }
  };

  $scope.remove = function(term) {
    var idx = _.indexOf($scope.selected, term);
    if (idx != -1) {
      $scope.selected.splice(idx, 1);
    }
  };
}]);

