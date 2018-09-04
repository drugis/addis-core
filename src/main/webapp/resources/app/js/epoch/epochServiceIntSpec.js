'use strict';
define(['angular-mocks'], function() {
  describe('the epoch service', function() {

    var rootScope, q, epochService,
      studyService = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph', 'findStudyNode']),
      rdfListService = jasmine.createSpyObj('RdfListService', ['flattenList', 'unFlattenList']);
    var studyGraphDefer;
    var studyJsonObject;
    var studyJsonGraphObject;
    var flattenResult = [];

    beforeEach(angular.mock.module('trialverse.epoch'));

    beforeEach(function() {
      angular.mock.module('trialverse.util', function($provide) {
        var uUIDServiceStub = jasmine.createSpyObj('UUIDService', ['generate']);
        uUIDServiceStub.generate.and.returnValue('newUuid');
        $provide.value('UUIDService', uUIDServiceStub);
      });
      angular.mock.module('trialverse.study', function($provide) {
        $provide.value('StudyService', studyService);
        $provide.value('RdfListService', rdfListService);
      });
    });

    beforeEach(inject(function($q, $rootScope, EpochService) {

      q = $q;
      rootScope = $rootScope;
      epochService = EpochService;
      studyGraphDefer = $q.defer();
      var getJsonGraphPromise = studyGraphDefer.promise;

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

      studyJsonGraphObject = {
        '@graph': [studyJsonObject]
      };

      studyService.findStudyNode.and.returnValue(studyJsonObject);
      studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
    }));

    afterEach(function() {
      rdfListService.flattenList.calls.reset();
      rdfListService.unFlattenList.calls.reset();
    });

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
        inject(function(RdfListService) {
          flattenResult = studyJsonObject.has_epochs;
          RdfListService.flattenList.and.returnValue(flattenResult);

          studyGraphDefer.resolve(studyJsonGraphObject);
          epochService.queryItems().then(function(result) {
            expect(result).toEqual(expected);
            done();
          });

          rootScope.$digest();
        });
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
        inject(function(RdfListService) {
          flattenResult = studyJsonObject.has_epochs;
          RdfListService.flattenList.and.returnValue(flattenResult);
          var getJsonGraphPromise = studyGraphDefer.promise;
          studyJsonObject = {
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
          rdfListService.flattenList.and.returnValue(studyJsonObject.has_epochs);
          studyJsonGraphObject = {
            '@graph': [studyJsonObject]
          };
          studyGraphDefer.resolve(studyJsonGraphObject);
          studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
          epochService.queryItems().then(function(result) {
            expect(result).toEqual(expected);
            done();
          });

          rootScope.$digest();
        });
      });
    });

    describe('addEpoch', function() {

      it('should add the epoch with comment and set primary and add it to the list', function(done) {
        inject(function(RdfListService) {
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
          var expectedEpochList = [{
            '@id': 'http://trials.drugis.org/instances/aaa',
            '@type': 'ontology:Epoch',
            duration: 'P14D',
            label: 'Washout'
          }, {
            '@id': 'http://trials.drugis.org/instances/ddd',
            '@type': 'ontology:Epoch',
            label: 'Randomization'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            duration: 'P42D',
            label: 'Main phase'
          }, {
            '@id': 'http://trials.drugis.org/instances/newUuid',
            '@type': 'ontology:Epoch',
            label: 'new epoch label',
            duration: 'P13D',
            comment: 'new epoch comment'
          }];
          RdfListService.flattenList.and.returnValue(epochList);
          var getJsonGraphPromise = studyGraphDefer.promise;
          var studyJsonObject = {
            'has_epochs': epochList,
            'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
          };
          studyGraphDefer.resolve(studyJsonObject);
          studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
          var itemToAdd = {
            label: 'new epoch label',
            comment: 'new epoch comment',
            isPrimaryEpoch: true,
            duration: 'P13D',
          };

          epochService.addItem(itemToAdd).then(function() {
            epochService.queryItems().then(function() {
              expect(RdfListService.unFlattenList).toHaveBeenCalledWith(expectedEpochList);
              done();
            });
          });

          rootScope.$digest();
        });
      });

      it('should work if there were no epochs', function(done) {
        inject(function(RdfListService) {
          var getJsonGraphPromise = studyGraphDefer.promise;
          var studyJsonObject = {};
          studyGraphDefer.resolve(studyJsonObject);
          studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
          RdfListService.flattenList.and.returnValue([]);
          var itemToAdd = {
            label: 'new epoch label',
            comment: 'new epoch comment',
            isPrimaryEpoch: true,
            duration: 'P13D',
          };

          var expectedEpoch = {
            '@id': 'http://trials.drugis.org/instances/newUuid',
            '@type': 'ontology:Epoch',
            label: 'new epoch label',
            duration: 'P13D',
            comment: 'new epoch comment'
          };

          epochService.addItem(itemToAdd).then(function() {
            epochService.queryItems().then(function() {
              expect(RdfListService.unFlattenList).toHaveBeenCalledWith([expectedEpoch]);
              done();
            });
          });

          rootScope.$digest();
        });
      });
    });


    describe('editEpoch', function() {
      it('should edit to remove the comment and unset primary', function(done) {
        inject(function(RdfListService) {
          var getJsonGraphPromise = studyGraphDefer.promise;
          var epochList = [{
            '@id': 'http://trials.drugis.org/instances/aaa',
            '@type': 'ontology:Epoch',
            'duration': 'P14D',
            'label': 'Washout'
          }, {
            '@id': 'http://trials.drugis.org/instances/ddd',
            '@type': 'ontology:Epoch',
            'label': 'Randomization',
            'comment': 'it had a comment'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            'duration': 'P42D',
            'label': 'Main phase'
          }];
          studyGraphDefer.resolve();
          studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
          RdfListService.flattenList.and.returnValue(epochList);

          var newItem = {
            uri: 'http://trials.drugis.org/instances/ddd',
            label: 'Randomization',
            comment: '',
            isPrimaryEpoch: false,
            pos: 1,
            duration: 'P13D',
          };

          var expectedList = [{
            '@id': 'http://trials.drugis.org/instances/aaa',
            '@type': 'ontology:Epoch',
            duration: 'P14D',
            label: 'Washout'
          }, {
            '@id': 'http://trials.drugis.org/instances/ddd',
            '@type': 'ontology:Epoch',
            label: 'Randomization',
            duration: 'P13D'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            duration: 'P42D',
            label: 'Main phase'
          }];

          epochService.editItem(newItem).then(function() {
            epochService.queryItems().then(function() {
              expect(RdfListService.unFlattenList).toHaveBeenCalledWith(expectedList);
              expect(studyJsonObject.has_primary_epoch).toBeFalsy();
              done();
            });
          });

          rootScope.$digest();
        });
      });
    });

    describe('editEpoch', function() {
      it('should edit label and leave primary alone', function(done) {
        inject(function(RdfListService) {
          var getJsonGraphPromise = studyGraphDefer.promise;
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

          studyGraphDefer.resolve();
          studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
          RdfListService.flattenList.and.returnValue(epochList);

          var newItem = {
            uri: 'http://trials.drugis.org/instances/ccc',
            label: 'Randomization',
            comment: 'new comment',
            isPrimaryEpoch: false,
            pos: 2,
            duration: 'P0D',
          };

          var expectedList = [{
            '@id': 'http://trials.drugis.org/instances/aaa',
            '@type': 'ontology:Epoch',
            duration: 'P14D',
            label: 'Washout'
          }, {
            '@id': 'http://trials.drugis.org/instances/ddd',
            '@type': 'ontology:Epoch',
            label: 'Randomization'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            duration: 'P0D',
            label: 'Randomization',
            comment: 'new comment'
          }];
          epochService.editItem(newItem).then(function() {
            epochService.queryItems().then(function() {
              expect(RdfListService.unFlattenList).toHaveBeenCalledWith(expectedList);
              expect(studyJsonObject.has_primary_epoch).toBeTruthy();
              done();
            });
          });

          rootScope.$digest();
        });
      });
    });

    describe('deleteEpoch', function() {
      it('remove the epoch from the list and remove the primary', function(done) {
        inject(function(RdfListService) {
          var getJsonGraphPromise = studyGraphDefer.promise;
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
          studyGraphDefer.resolve();
          studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
          RdfListService.flattenList.and.returnValue(epochList);

          var itemToRemove = {
            uri: 'http://trials.drugis.org/instances/ddd',
            label: 'Randomization',
            comment: 'new comment',
            isPrimaryEpoch: true,
            pos: 1,
            duration: 'P42D',
          };

          var expectedList = [{
            '@id': 'http://trials.drugis.org/instances/aaa',
            '@type': 'ontology:Epoch',
            duration: 'P14D',
            label: 'Washout'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            duration: 'P42D',
            label: 'Main phase'
          }];

          epochService.deleteItem(itemToRemove).then(function() {
            epochService.queryItems().then(function() {
              expect(RdfListService.unFlattenList).toHaveBeenCalledWith(expectedList);
              expect(studyJsonObject.has_primary_epoch).toBeFalsy();
              done();
            });
          });

          rootScope.$digest();
        });
      });
    });

    describe('deleteEpoch', function() {
     it('remove the epoch from the list leave primary alone', function(done) {
        var getJsonGraphPromise = studyGraphDefer.promise;
        inject(function(RdfListService) {
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
          studyGraphDefer.resolve();
          studyService.getJsonGraph.and.returnValue(getJsonGraphPromise);
          RdfListService.flattenList.and.returnValue(epochList);

          var itemToRemove = {
            uri: 'http://trials.drugis.org/instances/aaa',
          };

          var expectedList = [{
            '@id': 'http://trials.drugis.org/instances/ddd',
            '@type': 'ontology:Epoch',
            label: 'Randomization'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            duration: 'P42D',
            label: 'Main phase'
          }];

          epochService.deleteItem(itemToRemove).then(function() {
            epochService.queryItems().then(function() {
              expect(RdfListService.unFlattenList).toHaveBeenCalledWith(expectedList);
              expect(studyJsonObject.has_primary_epoch).toBeTruthy();
              done();

              done();
            });
          });

          rootScope.$digest();
        });
      });
    });

  });
});
