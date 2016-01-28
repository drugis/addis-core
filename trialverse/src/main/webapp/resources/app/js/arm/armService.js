'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService', 'SanitizeService'];
    var ArmService = function($q, StudyService, SparqlResource, UUIDService, SanitizeService) {

      var queryArms = SparqlResource.get('queryArm.sparql');
      var addArmTemplate = SparqlResource.get('addArmQuery.sparql');
      var addArmCommentTemplate = SparqlResource.get('addArmCommentQuery.sparql');
      var editArmWithCommentTemplate = SparqlResource.get('editArmWithComment.sparql');
      var editArmWithoutCommentTemplate = SparqlResource.get('editArmWithoutComment.sparql');
      var deleteSubjectTemplate = SparqlResource.get('deleteSubject.sparql');
      var deleteHasArmTemplate = SparqlResource.get('deleteHasArm.sparql');

      function queryItems() {
        return queryArms.then(function(query) {
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function addItem(item) {
        var addArmPromise, addCommentPromise;
        var armToAdd = angular.copy(item);
        armToAdd.uuid = UUIDService.generate();

        addArmPromise = addArmTemplate.then(function(template) {
          return StudyService.doModifyingQuery(fillTemplate(template, armToAdd));
        });

        if (armToAdd.comment) {
          addCommentPromise = addArmCommentTemplate.then(function(template) {
            return StudyService.doModifyingQuery(fillTemplate(template, armToAdd));
          });
        }

        return $q.all([addArmPromise, addCommentPromise]);
      }

      function editItem(item) {
        var defer = $q.defer();
        if (item.comment) {
          editArmWithCommentTemplate.then(function(template) {
            defer.resolve(StudyService.doModifyingQuery(fillTemplate(template, item)));
          });
        } else {
          editArmWithoutCommentTemplate.then(function(template) {
            defer.resolve(StudyService.doModifyingQuery(fillTemplate(template, item)));
          });
        }
        return defer.promise;
      }

      function deleteItem(arm) {
        var deleteArmPromise, deleteHasArmPromise;

        deleteArmPromise = deleteSubjectTemplate.then(function(template) {
          return StudyService.doModifyingQuery(fillTemplate(template, arm));
        });

        deleteArmPromise = deleteHasArmTemplate.then(function(template) {
          return StudyService.doModifyingQuery(fillTemplate(template, arm));
        });

        return $q.all([deleteArmPromise, deleteHasArmPromise]);
      }

      function fillTemplate(template, item) {
        return template
          .replace(/\$newUUID/g, item.uuid)
          .replace(/\$subjectURI/g, item.armURI)
          .replace(/\$armURI/g, item.armURI)
          .replace(/\$label/g, item.label)
          .replace(/\$comment/g, SanitizeService.sanitizeStringLiteral(item.comment));
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        editItem: editItem,
        deleteItem: deleteItem,
      };
    };

    return dependencies.concat(ArmService);
  });