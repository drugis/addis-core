'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService', 'BLINDING_OPTIONS', 'GROUP_ALLOCATION_OPTIONS', 'STATUS_OPTIONS'];
    var StudyInformationService = function($q, StudyService, SparqlResource, UUIDService, BLINDING_OPTIONS, GROUP_ALLOCATION_OPTIONS, STATUS_OPTIONS) {

      var studyInformationQuery = SparqlResource.get('queryStudyInformation.sparql');
      var editBlindingTemplate = SparqlResource.get('editBlinding.sparql');
      var deleteBlindingTemplate = SparqlResource.get('deleteBlinding.sparql');
      var editGroupAllocationTemplate = SparqlResource.get('editGroupAllocation.sparql');
      var deleteGroupAllocationTemplate = SparqlResource.get('deleteGroupAllocation.sparql');
      var editStatusTemplate = SparqlResource.get('editStatus.sparql');
      var deleteStatusTemplate = SparqlResource.get('deleteStatus.sparql');
      var editNumberOfCentersTemplate = SparqlResource.get('editNumberOfCenters.sparql');
      var deleteNumberOfCentersTemplate = SparqlResource.get('deleteNumberOfCenters.sparql');

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
            }
            return [transformedResult];
          });
        });
      }

      function editSelectItem(item, editProperty, options, editTemplate, deleteTemplate) {
        if (item[editProperty].uri === options.unknown.uri) {
          return deleteTemplate.then(function(template) {
            return StudyService.doModifyingQuery(template);
          });
        } else {
          return editTemplate.then(function(template) {
            var query = fillTemplate(template, item);
            return StudyService.doModifyingQuery(query);
          });
        }

      }

      function editGroupAllocation(item) {
        editSelectItem(item, 'groupAllocation', GROUP_ALLOCATION_OPTIONS, editGroupAllocationTemplate, deleteGroupAllocationTemplate);
      }

      function editBlinding(item) {
        editSelectItem(item, 'blinding', BLINDING_OPTIONS, editBlindingTemplate, deleteBlindingTemplate);
      }

      function editStatus(item) {
        editSelectItem(item, 'status', STATUS_OPTIONS, editStatusTemplate, deleteStatusTemplate);
      }

      function editItem(item) {
        var editGroupAllocationPromise = editGroupAllocation(item),
          editBlindingPromise = editBlinding(item),
          editStatusPromise = editStatus(item),
          editNumberOfCentersPromise;

        if(!!parseInt(item.numberOfCenters)) {
          editNumberOfCentersPromise = editNumberOfCentersTemplate.then(function(template){
            var query = fillTemplate(template, item);
            return StudyService.doModifyingQuery(query);
          });
        }

        return $q.all([editGroupAllocationPromise, editBlindingPromise, editStatusPromise]);
      }

      function fillTemplate(template, item) {
        return template
          .replace(/\$groupAllocationUri/g, item.groupAllocation.uri)
          .replace(/\$blindingUri/g, item.blinding.uri)
          .replace(/\$statusUri/g, item.status.uri)
          .replace(/\$numberOfCenters/g, item.numberOfCenters)
          ;
      }

      return {
        queryItems: queryItems,
        editItem: editItem
      };
    };

    return dependencies.concat(StudyInformationService);
  });
