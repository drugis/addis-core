'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService',
     'BLINDING_OPTIONS', 'GROUP_ALLOCATION_OPTIONS', 'STATUS_OPTIONS', 'SanitizeService'];
    var StudyInformationService = function($q, StudyService, SparqlResource,
     UUIDService, BLINDING_OPTIONS, GROUP_ALLOCATION_OPTIONS, STATUS_OPTIONS, SanitizeService) {

      var studyInformationQuery = SparqlResource.get('queryStudyInformation.sparql');
      var editBlindingTemplate = SparqlResource.get('editBlinding.sparql');
      var deleteBlindingTemplate = SparqlResource.get('deleteBlinding.sparql');
      var editGroupAllocationTemplate = SparqlResource.get('editGroupAllocation.sparql');
      var deleteGroupAllocationTemplate = SparqlResource.get('deleteGroupAllocation.sparql');
      var editStatusTemplate = SparqlResource.get('editStatus.sparql');
      var deleteStatusTemplate = SparqlResource.get('deleteStatus.sparql');
      var editNumberOfCentersTemplate = SparqlResource.get('editNumberOfCenters.sparql');
      var deleteNumberOfCentersTemplate = SparqlResource.get('deleteNumberOfCenters.sparql');
      var editObjectiveTemplate = SparqlResource.get('editObjective.sparql');
      var deleteObjectiveTemplate = SparqlResource.get('deleteObjective.sparql');

      function queryItems() {
        return studyInformationQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            var transformedResult = {
              blinding: {},
              groupAllocation: {},
              status: {}
            };
            if (result.length > 0) {
              transformedResult.blinding.uri = result[0].blindingUri;
              transformedResult.groupAllocation.uri = result[0].groupAllocationUri;
              transformedResult.status.uri = result[0].statusUri;
              transformedResult.numberOfCenters = parseInt(result[0].numberOfCenters);
              transformedResult.objective = result[0].objective;
            }
            return [transformedResult];
          });
        });
      }

      function editProperty(item, editCondition, editTemplate, deleteTemplate) {
        if (editCondition) {
          return editTemplate.then(function(template) {
            var query = fillTemplate(template, item);
            return StudyService.doModifyingQuery(query);
          });
        } else {
          return deleteTemplate.then(function(template) {
            return StudyService.doModifyingQuery(template);
          });
        }
      }

      function editSelectItem(item, selectedProperty, options, editTemplate, deleteTemplate) {
        return editProperty(item, selectedProperty && selectedProperty.uri !== options.unknown.uri, editTemplate, deleteTemplate);
      }

      function editGroupAllocation(item) {
        editSelectItem(item, item.groupAllocation, GROUP_ALLOCATION_OPTIONS, editGroupAllocationTemplate, deleteGroupAllocationTemplate);
      }

      function editBlinding(item) {
        editSelectItem(item, item.blinding, BLINDING_OPTIONS, editBlindingTemplate, deleteBlindingTemplate);
      }

      function editStatus(item) {
        editSelectItem(item, item.status, STATUS_OPTIONS, editStatusTemplate, deleteStatusTemplate);
      }

      function editItem(item) {
        var editGroupAllocationPromise = editGroupAllocation(item),
          editBlindingPromise = editBlinding(item),
          editStatusPromise = editStatus(item),
          editNumberOfCentersPromise = editProperty(item, !!parseInt(item.numberOfCenters), editNumberOfCentersTemplate, deleteNumberOfCentersTemplate),
          editObjectivePromise = editProperty(item, item.objective, editObjectiveTemplate, deleteObjectiveTemplate);

        return $q.all([editGroupAllocationPromise, editBlindingPromise, editStatusPromise,
          editNumberOfCentersPromise, editObjectivePromise]);
      }

      function fillTemplate(template, item) {
        return template
          .replace(/\$groupAllocationUri/g, item.groupAllocation ? item.groupAllocation.uri : '')
          .replace(/\$blindingUri/g, item.blinding? item.blinding.uri : '')
          .replace(/\$statusUri/g, item.status? item.status.uri : '')
          .replace(/\$numberOfCenters/g, item.numberOfCenters)
          .replace(/\$objective/g, SanitizeService.sanitizeStringLiteral(item.objective, SanitizeService.SPARQL_STRING_LITERAL));
      }

      return {
        queryItems: queryItems,
        editItem: editItem
      };
    };

    return dependencies.concat(StudyInformationService);
  });
