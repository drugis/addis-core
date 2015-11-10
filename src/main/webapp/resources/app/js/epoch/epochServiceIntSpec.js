'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the epoch service', function() {

    var rootScope, q, epochService,
      queryEpochs, queryAddEpoch, queryAddEpochToEndOfList, queryAddEpochComment,
      editEpoch, addEpochToEndOfList, setEpochPrimary, deleteEpoch,
      removeEpochPrimary, setEpochToPrimary, getLastItemInEpochList, deleteTail,
      studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var studyDefer;
    var studyJsonObject;

    beforeEach(module('trialverse.epoch'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        var uUIDServiceStub = jasmine.createSpyObj('UUIDService', ['generate']);
        uUIDServiceStub.generate.and.returnValue('newUuid');
        $provide.value('UUIDService', uUIDServiceStub);
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, EpochService) {

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


    describe('queryItems', function(done) {

      var expected = [{
        '@id': 'http://trials.drugis.org/instances/aaa',
        '@type': 'ontology:Epoch',
        'duration': 'P14D',
        'label': 'Washout',
        'pos': 0,
        'isPrimary': false
      }, {
        '@id': 'http://trials.drugis.org/instances/bbb',
        '@type': 'ontology:Epoch',
        'label': 'Randomization',
        'pos': 1,
        'isPrimary': false
      }, {
        '@id': 'http://trials.drugis.org/instances/ccc',
        '@type': 'ontology:Epoch',
        'duration': 'P42D',
        'label': 'Main phase',
        'pos': 2,
        'isPrimary': false
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

    describe('queryItems where primary is set', function(done) {

      var expected = [{
        '@id': 'http://trials.drugis.org/instances/aaa',
        '@type': 'ontology:Epoch',
        'duration': 'P14D',
        'label': 'Washout',
        'pos': 0,
        'isPrimary': false
      }, {
        '@id': 'http://trials.drugis.org/instances/ddd',
        '@type': 'ontology:Epoch',
        'label': 'Randomization',
        'pos': 1,
        'isPrimary': true
      }, {
        '@id': 'http://trials.drugis.org/instances/ccc',
        '@type': 'ontology:Epoch',
        'duration': 'P42D',
        'label': 'Main phase',
        'pos': 2,
        'isPrimary': false
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

    ddescribe('addEpoch', function() {

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
          '@id': 'http://trials.drugis.org/instances/newUuid',
          duration: 'P13D',
          label: itemToAdd.label,
          comment: 'new epoch comment',
          pos: 3,
          isPrimary: true
        }

        epochService.addItem(itemToAdd).then(function() {
          epochService.queryItems().then(function(result) {
            expect(result.length).toEqual(4);
            expect(result[3]).toEqual(expectedEpoch);
            expect(result[1].isPrimary).toEqual(false);
            done();
          })
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
        var itemToEdit = {
          '@id': 'http://trials.drugis.org/instances/ddd',
          label: 'Randomization',
          comment: "",
          isPrimaryEpoch: false,
          duration: 'P13D',
        };

        var expectedEpoch = {
          '@id': 'http://trials.drugis.org/instances/ddd',
          duration: 'P13D',
          label: itemToEdit.label,
          pos: 2,
          isPrimary: false
        }

        epochService.editItem(itemToEdit).then(function() {
          epochService.queryItems().then(function(result) {
            expect(result.length).toEqual(3);
            expect(result[2]).toEqual(expectedEpoch);
            done();
          })
        });

        rootScope.$digest();
        
      });
    });

  });
});