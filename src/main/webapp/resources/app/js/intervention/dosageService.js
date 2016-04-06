'use strict';
define(['lodash', 'moment'], function(_, moment) {
  var dependencies = ['$http', 'SparqlResource'];
  var DosageService = function($http, SparqlResource) {

    var queryUnits = SparqlResource.get('queryUnits.sparql');

    function deFusekify(data) {
      var json = JSON.parse(data);
      var bindings = json.results.bindings;
      return _.map(bindings, function(binding) {
        return _.fromPairs(_.map(_.toPairs(binding), function(obj) {
          return [obj[0], obj[1].value];
        }));
      });
    }

    function get(userUid, datasetUuid, datasetVersionUuid) {
      return queryUnits.then(function(query) {
        var restPath = '/users/' + userUid + '/datasets/' + datasetUuid;
        if (datasetVersionUuid) {
          restPath = restPath + '/versions/' + datasetVersionUuid;
        }
        return $http.get(
          restPath + '/query', {
            params: {
              query: query
            },
            headers: {
              Accept: 'application/sparql-results+json'
            },
            transformResponse: function(data) {
              return deFusekify(data);
            }
          });
      }).then(function(response) {
        var uniqueCombinations = _.uniqBy(response.data, function(row) {
          return row.unitName + row.unitPeriod;
        });
        return uniqueCombinations.map(function(unit) {
          var periodLabel = moment.duration(unit.unitPeriod).humanize();
          periodLabel = periodLabel === 'a day' ? 'day' : periodLabel;
          return {
            unitName: unit.unitName,
            label: unit.unitName + '/' + periodLabel,
            unitPeriod: unit.unitPeriod
          };
        });
      });
    }

    return {
      get: get
    };
  };
  return dependencies.concat(DosageService);
});
