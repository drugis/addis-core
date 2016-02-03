'use strict';

/* Controllers */

var TermController = ['$scope', 'SparqlService', 'restrictions', function($scope, $sparql, restrictions) {
  $scope.selected = [];
  $scope.searchString = 'cardio*';
  $scope.restrictions = restrictions.all;
  $scope.source = restrictions.all[restrictions.default];

  $scope.$watch('source', function(newVal, oldVal) {
  $scope.selected = [];
  $scope.terms = [];
  $scope.narrower = [];
  $scope.broader = [];
  });

  var extractValues = function(result) {
    var bindings = result.data.results.bindings;
    return _.map(bindings, function(binding) {
      return _.object(_.map(_.toPairs(binding), function(obj) {
        return [ obj[0], obj[1].value ];
      }));
    });
  };

  $scope.query = function() {
    $sparql.query('disease-search', { query: $scope.searchString, restrict: $scope.source }).then(
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
    $sparql.query('instances', { terms : $scope.selected, restrict: $scope.source }).then(
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
    var broader = $sparql.query('disease-broader', { uri: uri, restrict: $scope.source }).then(resultsFn('broader'), errorFn);
    var narrower = $sparql.query('disease-narrower', { uri: uri, restrict: $scope.source }).then(resultsFn('narrower'), errorFn);
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
}];

angular.module('myApp.controllers', []).
controller('IndicationController', ['$scope', '$injector', function($scope, $injector) {
  var restrictions = {
    'snomed-human-diseases' : {
      label: 'SNOMED human diseases',
	  labelUri: 'rdfs:label',
      graph: '<http://www.ihtsdo.org/SNOMEDCT/>',
      sparql: '?uri rdfs:subClassOf+ snomed:SCT_64572001 . # IS A disease\n' +
              'MINUS { ?uri rdfs:subClassOf+ snomed:SCT_127326005 } # NOT IS A non-human disease'
    },
    'drugis-indications' : {
      label: 'drugis.org indications',
	  labelUri: 'rdfs:label',
      graph: '?g',
      sparql: '?uri rdfs:subClassOf <http://trials.drugis.org/indication> .',
      graphQuery: 'GRAPH <http://trials.drugis.org/namespaces/> {\n' +
                  '  ?g rdf:type <http://trials.drugis.org/namespace> .\n' +
                  '}'
    }
  }
  $injector.invoke(TermController, this, {
    $scope: $scope,
    restrictions: { default: 'snomed-human-diseases', all: restrictions }
  });
}]).
controller('DrugController', ['$scope', '$injector', function($scope, $injector) {
  var restrictions = {
    'atc' : {
      label: 'ATC classification',
	  labelUri: 'rdfs:label',
      graph: '<http://www.whocc.no/ATC2011/>',
      sparql: '?uri rdfs:subClassOf+ atc:ATCCode .'
    },
    'drugis-drugs' : {
      label: 'drugis.org drugs',
	  labelUri: 'rdfs:label',
      graph: '?g',
      sparql: '?uri rdfs:subClassOf <http://trials.drugis.org/drug> .',
      graphQuery: 'GRAPH <http://trials.drugis.org/namespaces/> { ' +
                  '?g rdf:type <http://trials.drugis.org/namespace> .' +
                  ' }'
    }
  }
  $injector.invoke(TermController, this, {
    $scope: $scope,
    restrictions: { default: 'atc', all: restrictions }
  });
}]).
controller('AdverseEventController', ['$scope', '$injector', function($scope, $injector) {
  var restrictions = {
    'medra' : {
      label: 'MedDRA adverse events',
	  labelUri: 'skos:prefLabel',
      graph: '<http://purl.bioontology.org/ontology/MDR/>',
      sparql: '?uri skos:prefLabel ?label .'
    },
    'drugis-adverseEvents' : {
      label: 'drugis.org adverse events',
	  labelUri: 'rdfs:label',
      graph: '?g',
      sparql: '?uri rdfs:subClassOf <http://trials.drugis.org/adverseEvent> .',
      graphQuery: 'GRAPH <http://trials.drugis.org/namespaces/> { ' +
                  '?g rdf:type <http://trials.drugis.org/namespace> .' +
                  ' }'
    }
  }
  $injector.invoke(TermController, this, {
    $scope: $scope,
    restrictions: { default: 'drugis-adverseEvents', all: restrictions }
  });
}]).
controller('EndpointController', ['$scope', '$injector', function($scope, $injector) {
  var restrictions = {
    'drugis-endpoints' : {
      label: 'drugis.org endpoints',
	  labelUri: 'rdfs:label',
      graph: '?g',
      sparql: '?uri rdfs:subClassOf <http://trials.drugis.org/endpoint> .',
      graphQuery: 'GRAPH <http://trials.drugis.org/namespaces/> { ' +
                  '?g rdf:type <http://trials.drugis.org/namespace> .' +
                  ' }'
    }
  }
  $injector.invoke(TermController, this, {
    $scope: $scope,
    restrictions: { default: 'drugis-endpoints', all: restrictions }
  });
}]);
