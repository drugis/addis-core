'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource'];
    var OutcomeServiceService = function($q, StudyService, SparqlResource) {

      var setOutcomeResultPropertyTemplate = SparqlResource.get('setOutcomeResultProperty.sparql');

      function setOutcomeProperty(outcome) {
        return setOutcomeResultPropertyTemplate.then(function(template) {
          var query = template.replace(/\$URI/g, outcome.uri);
          return StudyService.doModifyingQuery(query);
        });
      }

      return {
        setOutcomeProperty: setOutcomeProperty
      };
    };
    return dependencies.concat(OutcomeServiceService);
  });
