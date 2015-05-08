'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService', 'SanitizeService'];
    var PopulationInformationService = function($q, StudyService, SparqlResource, UUIDService, SanitizeService) {

      var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';

      var populationInformationQuery = SparqlResource.get('queryPopulationInformation.sparql');
      var editPopulationInformationTemplate = SparqlResource.get('editPopulationInformation.sparql');

      function queryItems() {
        return populationInformationQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            var transformedResult = {
              indication: {},
              eligibilityCriteria: {}
            };
            if (result.length > 0) {
              transformedResult.indication.label = result[0].indicationLabel;
              transformedResult.indication.uri = result[0].indicationUri;
              transformedResult.eligibilityCriteria.label = result[0].eligibilityCriteria;
            }

            return [transformedResult];
          });
        });
      }

      function editItem(item) {
        if (!item.indication.uri) {
          item.indication.uri = INSTANCE_PREFIX + UUIDService.generate();
        }

        return editPopulationInformationTemplate.then(function(template) {
          var query = fillTemplate(template, item);
          return StudyService.doModifyingQuery(query);
        });
      }

      function fillTemplate(template, item) {
        return template
          .replace(/\$indicationUri/g, item.indication.uri)
          .replace(/\$indicationLabel/g, SanitizeService.sanitizeStringLiteral(item.indication.label))
          .replace(/\$eligibilityCriteria/g, item.eligibilityCriteria.label)
          ;
      }

      return {
        queryItems: queryItems,
        editItem: editItem,
        INSTANCE_PREFIX: INSTANCE_PREFIX
      };
    };
    return dependencies.concat(PopulationInformationService);
  });
