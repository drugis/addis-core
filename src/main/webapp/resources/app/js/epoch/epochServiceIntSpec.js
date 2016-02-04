'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the epoch service', function() {

    var rootScope, q, epochService,
      studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var studyDefer;
    var studyJsonObject;

    beforeEach(module('trialverse.epoch'));

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        var uUIDServiceStub = jasmine.createSpyObj('UUIDService', ['generate']);
        uUIDServiceStub.generate.and.returnValue('newUuid');
        $provide.value('UUIDService', uUIDServiceStub);
      });
      module('trialverse.study', function($provide) {
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(angularMocks.inject(function($q, $rootScope, EpochService) {

      q = $q;
      rootScope = $rootScope;
      epochService = EpochService;
      studyDefer = $q.defer();
      var getStudyPromise = studyDefer.promise;

      studyJsonObject = {
        'has_epochs': [{
          '@id': 'http://trials.drugis.org/instances/aaa',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }, {
          '@id': 'http://trials.drugis.org/instances/bbb',
          '@type': 'ontology:Epoch',
          'label': 'Randomization'
        }, {
          '@id': 'http://trials.drugis.org/instances/ccc',
          '@type': 'ontology:Epoch',
          'duration': 'P42D',
          'label': 'Main phase'
        }],
        'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
      };

      studyService.getStudy.and.returnValue(getStudyPromise);

    }));


    describe('queryItems', function() {

      var expected = [{
        uri: 'http://trials.drugis.org/instances/aaa',
        duration: 'P14D',
        label: 'Washout',
        pos: 0,
        isPrimary: false
      }, {
        uri: 'http://trials.drugis.org/instances/bbb',
        label: 'Randomization',
        duration: 'PT0S',
        pos: 1,
        isPrimary: false
      }, {
        uri: 'http://trials.drugis.org/instances/ccc',
        duration: 'P42D',
        label: 'Main phase',
        pos: 2,
        isPrimary: false
      }];

      it('should query the epochs', function(done) {
        studyDefer.resolve(studyJsonObject);
        epochService.queryItems().then(function(result) {
          expect(result).toEqual(expected);
          done();
        });

        rootScope.$digest();

      });
    });

    describe('queryItems where primary is set', function() {

      var expected = [{
        uri: 'http://trials.drugis.org/instances/aaa',
        duration: 'P14D',
        label: 'Washout',
        pos: 0,
        isPrimary: false
      }, {
        uri: 'http://trials.drugis.org/instances/ddd',
        label: 'Randomization',
        duration: 'PT0S',
        pos: 1,
        isPrimary: true
      }, {
        uri: 'http://trials.drugis.org/instances/ccc',
        duration: 'P42D',
        label: 'Main phase',
        pos: 2,
        isPrimary: false
      }];

      it('should query the epochs', function(done) {
        var getStudyPromise = studyDefer.promise;
        var studyJsonObject = {
          'has_epochs': [{
            '@id': 'http://trials.drugis.org/instances/aaa',
            '@type': 'ontology:Epoch',
            'duration': 'P14D',
            'label': 'Washout'
          }, {
            '@id': 'http://trials.drugis.org/instances/ddd',
            '@type': 'ontology:Epoch',
            'label': 'Randomization'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            'duration': 'P42D',
            'label': 'Main phase'
          }],
          'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
        };
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);
        epochService.queryItems().then(function(result) {
          expect(result).toEqual(expected);
          done();
        });

        rootScope.$digest();

      });
    });

    describe('addEpoch', function() {

      it('should add the epoch with comment and set primary and add it to the list', function(done) {
        var getStudyPromise = studyDefer.promise;
        var epochList = [{
          '@id': 'http://trials.drugis.org/instances/aaa',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }, {
          '@id': 'http://trials.drugis.org/instances/ddd',
          '@type': 'ontology:Epoch',
          'label': 'Randomization'
        }, {
          '@id': 'http://trials.drugis.org/instances/ccc',
          '@type': 'ontology:Epoch',
          'duration': 'P42D',
          'label': 'Main phase'
        }];
        var studyJsonObject = {
          'has_epochs': epochList,
          'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
        };
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);
        var itemToAdd = {
          label: 'new epoch label',
          comment: 'new epoch comment',
          isPrimaryEpoch: true,
          duration: 'P13D',
        };

        var expectedEpoch = {
          uri: 'http://trials.drugis.org/instances/newUuid',
          duration: 'P13D',
          label: itemToAdd.label,
          comment: 'new epoch comment',
          pos: 3,
          isPrimary: true
        };

        epochService.addItem(itemToAdd).then(function() {
          epochService.queryItems().then(function(result) {
            expect(result.length).toEqual(4);
            expect(result[3]).toEqual(expectedEpoch);
            expect(result[1].isPrimary).toEqual(false);
            done();
          });
        });

        rootScope.$digest();

      });
    });


    describe('editEpoch', function() {
      it('should edit to remove the comment and unset primary', function(done) {
        var getStudyPromise = studyDefer.promise;
        var epochList = [{
          '@id': 'http://trials.drugis.org/instances/aaa',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }, {
          '@id': 'http://trials.drugis.org/instances/ddd',
          '@type': 'ontology:Epoch',
          'label': 'Randomization'
        }, {
          '@id': 'http://trials.drugis.org/instances/ccc',
          '@type': 'ontology:Epoch',
          'duration': 'P42D',
          'label': 'Main phase'
        }];
        var studyJsonObject = {
          'has_epochs': epochList,
          'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
        };
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);

        var newItem = {
          uri: 'http://trials.drugis.org/instances/ddd',
          label: 'Randomization',
          comment: "",
          isPrimaryEpoch: false,
          pos: 1,
          duration: 'P13D',
        };

        var expectedEpoch = {
          uri: 'http://trials.drugis.org/instances/ddd',
          duration: 'P13D',
          label: newItem.label,
          pos: 1,
          isPrimary: false
        };

        epochService.editItem(newItem).then(function() {
          epochService.queryItems().then(function(result) {
            expect(result.length).toEqual(3);
            expect(result[1]).toEqual(expectedEpoch);
            done();
          });
        });

        rootScope.$digest();

      });
    });

    describe('editEpoch', function() {
      it('should edit label and leave primary alone', function(done) {
        var getStudyPromise = studyDefer.promise;
        var epochList = [{
          '@id': 'http://trials.drugis.org/instances/aaa',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }, {
          '@id': 'http://trials.drugis.org/instances/ddd',
          '@type': 'ontology:Epoch',
          'label': 'Randomization'
        }, {
          '@id': 'http://trials.drugis.org/instances/ccc',
          '@type': 'ontology:Epoch',
          'duration': 'P42D',
          'label': 'Main phase'
        }];
        var studyJsonObject = {
          'has_epochs': epochList,
          'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
        };
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);

        var newItem = {
          uri: 'http://trials.drugis.org/instances/ccc',
          label: 'Randomization',
          comment: "new comment",
          isPrimaryEpoch: false,
          pos: 2,
          duration: 'P0D',
        };

        var expectedEpoch = {
          uri: 'http://trials.drugis.org/instances/ccc',
          duration: 'P0D',
          label: newItem.label,
          comment: newItem.comment,
          pos: 2,
          isPrimary: false
        };

        epochService.editItem(newItem).then(function() {
          epochService.queryItems().then(function(result) {
            expect(result.length).toEqual(3);
            expect(result[2]).toEqual(expectedEpoch);
            expect(result[1].isPrimary).toEqual(true);
            done();
          });
        });

        rootScope.$digest();

      });
    });

    describe('deleteEpoch', function() {
      it('remove the epoch from the list and remove the primary', function(done) {
        var getStudyPromise = studyDefer.promise;
        var epochList = [{
          '@id': 'http://trials.drugis.org/instances/aaa',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }, {
          '@id': 'http://trials.drugis.org/instances/ddd',
          '@type': 'ontology:Epoch',
          'label': 'Randomization'
        }, {
          '@id': 'http://trials.drugis.org/instances/ccc',
          '@type': 'ontology:Epoch',
          'duration': 'P42D',
          'label': 'Main phase'
        }];
        var studyJsonObject = {
          'has_epochs': epochList,
          'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
        };
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);

        var itemToRemove = {
          uri: 'http://trials.drugis.org/instances/ddd',
          label: 'Randomization',
          comment: "new comment",
          isPrimaryEpoch: true,
          pos: 1,
          duration: 'P42D',
        };

        epochService.deleteItem(itemToRemove).then(function() {
          epochService.queryItems().then(function(result) {
            expect(result.length).toEqual(2);
            expect(result[0].uri).toEqual('http://trials.drugis.org/instances/aaa');
            expect(result[1].uri).toEqual('http://trials.drugis.org/instances/ccc');
            expect(result[0].isPrimary).toEqual(false);
            expect(result[1].isPrimary).toEqual(false);
            done();
          });
        });

        rootScope.$digest();

      });
    });

    describe('deleteEpoch', function() {
      it('remove the epoch from the list leave primary alone', function(done) {
        var getStudyPromise = studyDefer.promise;
        var epochList = [{
          '@id': 'http://trials.drugis.org/instances/aaa',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }, {
          '@id': 'http://trials.drugis.org/instances/ddd',
          '@type': 'ontology:Epoch',
          'label': 'Randomization'
        }, {
          '@id': 'http://trials.drugis.org/instances/ccc',
          '@type': 'ontology:Epoch',
          'duration': 'P42D',
          'label': 'Main phase'
        }];
        var studyJsonObject = {
          'has_epochs': epochList,
          'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
        };
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);

        var itemToRemove = {
          uri: 'http://trials.drugis.org/instances/aaa',
        };

        epochService.deleteItem(itemToRemove).then(function() {
          epochService.queryItems().then(function(result) {
            expect(result.length).toEqual(2);
            expect(result[0].uri).toEqual('http://trials.drugis.org/instances/ddd');
            expect(result[1].uri).toEqual('http://trials.drugis.org/instances/ccc');
            expect(result[0].isPrimary).toEqual(true);
            expect(result[1].isPrimary).toEqual(false);
            done();
          });
        });

        rootScope.$digest();

      });
    });

  });
});
