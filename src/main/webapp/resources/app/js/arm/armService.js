'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var ArmService = function($q, StudyService, SparqlResource, UUIDService) {

      var armsQuery = SparqlResource.get('queryArm.sparql');
      var rawAddArmQuery = SparqlResource.get('addArmQuery.sparql');
      var rawAddArmCommentQuery = SparqlResource.get('addArmCommentQuery.sparql');
      var editArmWithCommentQuery = SparqlResource.get('editArmWithComment.sparql');
      var editArmWithoutCommentQuery = SparqlResource.get('editArmWithoutComment.sparql');
      var rawDeleteArmQuery = SparqlResource.get('deleteArm.sparql');
      var rawDeleteHasArmQuery = SparqlResource.get('deleteHasArm.sparql');

      function queryItems(studyUuid) {
        return armsQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function addItem(arm, studyUUID) {

        var uuid = UUIDService.generate();
        var addArmPromise, addCommentPromise;

        addArmPromise = rawAddArmQuery.then(function(rawQuery) {
          var query = rawQuery
            .replace(/\$newUUID/g, uuid)
            .replace(/\$label/g, arm.label)
            .replace('$studyUUID', studyUUID);
          return StudyService.doModifyingQuery(query);
        });

        if(arm.comment) {
          addCommentPromise = rawAddArmCommentQuery.then(function(rawQuery) {
            var query = rawQuery
              .replace(/\$newUUID/g, uuid)
              .replace(/\$comment/g, arm.comment);
            return StudyService.doModifyingQuery(query);
          });
        }

        return $q.all([addArmPromise, addCommentPromise]);
      }

      function editItem(arm) {
        var defer = $q.defer();
        if (arm.comment) {
          editArmWithCommentQuery.then(function(query) {
            var editArmWithCommentQuery = query.replace(/\$armURI/g, arm.armURI)
              .replace('$newArmLabel', arm.label)
              .replace('$newArmComment', arm.comment);
            defer.resolve(StudyService.doModifyingQuery(editArmWithCommentQuery));
          });
        } else {
          editArmWithoutCommentQuery.then(function(query) {
            var editArmWithoutCommentQuery = query.replace(/\$armURI/g, arm.armURI)
              .replace('$newArmLabel', arm.label);
            defer.resolve(StudyService.doModifyingQuery(editArmWithoutCommentQuery));
          });
        }
        return defer.promise;
      }

      function deleteItem(arm) {
        var deleteArmPromise, deleteHasArmPromise;

        deleteArmPromise = rawDeleteArmQuery.then(function(rawQuery){
          var query = rawQuery.replace(/\$armURI/g, arm.armURI);
          return StudyService.doModifyingQuery(query);
        });

        deleteArmPromise = rawDeleteHasArmQuery.then(function(rawQuery){
          var query = rawQuery.replace(/\$armURI/g, arm.armURI);
          return StudyService.doModifyingQuery(query);
        });

        return $q.all([deleteArmPromise, deleteHasArmPromise]);
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
