'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var ArmService = function($q, StudyService, SparqlResource, UUIDService) {

      var armsQuery = SparqlResource.get({
        name: 'queryArm.sparql'
      });

      var rawAddArmQuery = SparqlResource.get({
        name: 'addArmQuery.sparql'
      });

      var rawAddArmCommentQuery = SparqlResource.get({
        name: 'addArmCommentQuery.sparql'
      });

      var editArmWithCommentQuery = SparqlResource.get({
        name: 'editArmWithComment.sparql'
      });

      var editArmWithoutCommentQuery = SparqlResource.get({
        name: 'editArmWithoutComment.sparql'
      });

      var rawDeleteArmQuery = SparqlResource.get({
        name: 'deleteArm.sparql'
      });

      var rawDeleteHasArmQuery = SparqlResource.get({
        name: 'deleteHasArm.sparql'
      });

      function queryItems() {
        var defer = $q.defer();

        armsQuery.$promise.then(function(query) {
          defer.resolve(StudyService.doNonModifyingQuery(query.data));
        });
        return defer.promise;
      }

      function addItem(arm, studyUUID) {

        var defer = $q.defer();
        var uuid = UUIDService.generate();

        $q.all([rawAddArmQuery.$promise, rawAddArmCommentQuery.$promise]).then(function() {
          return rawAddArmQuery.data
            .replace(/\$newUUID/g, uuid)
            .replace(/\$label/g, arm.label)
            .replace('$studyUUID', studyUUID);
        }).then(function(query) {
          return StudyService.doModifyingQuery(query);
        }).then(function() {
          if (arm.comment) {
            var query = rawAddArmCommentQuery.data
              .replace(/\$newUUID/g, uuid)
              .replace(/\$comment/g, arm.comment);
            defer.resolve(StudyService.doModifyingQuery(query));
          } else {
            defer.resolve(arm);
          }
        });
        return defer.promise;
      }

      function editItem(arm) {
        var defer = $q.defer();
        if (arm.comment) {
          editArmWithCommentQuery.$promise.then(function(query) {
            var editArmWithCommentQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
              .replace('$newArmLabel', arm.label.value)
              .replace('$newArmComment', arm.comment.value);
            defer.resolve(StudyService.doModifyingQuery(editArmWithCommentQuery));
          });
        } else {
          editArmWithoutCommentQuery.$promise.then(function(query) {
            var editArmWithoutCommentQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
              .replace('$newArmLabel', arm.label.value);
            defer.resolve(StudyService.doModifyingQuery(editArmWithoutCommentQuery));
          });
        }
        return defer.promise;
      }

      function deleteItem(arm) {
        var defer = $q.defer();

        $q.all([rawDeleteArmQuery.$promise, rawDeleteHasArmQuery.$promise]).then(function() {
          var deleteArmQuery = rawDeleteArmQuery.data.replace(/\$armURI/g, arm.armURI.value);
          var deleteHasArmQuery = rawDeleteHasArmQuery.data.replace(/\$armURI/g, arm.armURI.value);
          defer.resolve($q.all([StudyService.doModifyingQuery(deleteArmQuery),
            StudyService.doModifyingQuery(deleteHasArmQuery)
          ]));
        });
        return defer.promise;
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
