'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the adverse event service', function() {

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var measurementMomentServiceMock;
    var adverseEventService;
    var studyService;

    var addTemplateRaw
    var addAdverseEventQueryRaw;
    var adverseEventsQuery;
    var deleteAdverseEventRaw;
    var editAdverseEventRaw;
    var queryAdverseEventMeasuredAtRaw;

    var createStoreDeferred;
    var createStorePromise;
    var loadStoreDeferred
    var loadStorePromise

    var graphAsText;
    var graphUri = 'http://karma-test/';

    var scratchStudyUri = 'http://localhost:9876/scratch';

    function queryTeststore(query) {
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open("POST", scratchStudyUri + '/query?output=json', false);
      xmlHTTP.setRequestHeader('Content-type', 'application/sparql-query');
      xmlHTTP.setRequestHeader('Accept', 'application/ld+json');
      xmlHTTP.send(query);
      var result = xmlHTTP.responseText;
      console.log('queryTeststore result = ' + result);
      return result;
    }

    function dropGraph(uri) {
      var xmlHTTP = new XMLHttpRequest();
          xmlHTTP.open("POST", scratchStudyUri + '/update', false);
          xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
          xmlHTTP.send('DROP GRAPH <' + uri +'>');
      return true;
    }

   function deFusekify(data) {
      var json = JSON.parse(data);
      var bindings = json.results.bindings;
      return _.map(bindings, function(binding) {
        return _.object(_.map(_.pairs(binding), function(obj) {
          return [obj[0], obj[1].value];
        }));
      });
    }

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = jasmine.createSpyObj('RemoteRdfStoreService', [
          'create',
          'load',
          'executeUpdate',
          'executeQuery',
          'getGraph',
          'deFusekify'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.adverseEvent'));

    beforeEach(function() {
      module('trialverse', function($provide) {

        measurementMomentServiceMock = jasmine.createSpyObj('MeasurementMomentService', [
          'queryItems',
          'addItem',
          'editItem',
          'deleteIte ',
          'generateLabel'
        ]);
        $provide.value('MeasurementMomentService', measurementMomentServiceMock);

      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, AdverseEventService, StudyService) {
      var xmlHTTP = new XMLHttpRequest();

      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      adverseEventService = AdverseEventService;
      studyService = StudyService;

      // load queryTemplates from disk
      xmlHTTP.open('GET', 'base/app/sparql/addTemplate.sparql', false);
      xmlHTTP.send(null);
      addTemplateRaw = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/addTemplate.sparql').respond(addTemplateRaw);

      xmlHTTP.open('GET', 'base/app/sparql/addAdverseEvent.sparql', false);
      xmlHTTP.send(null);
      addAdverseEventQueryRaw = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/addAdverseEvent.sparql').respond(addAdverseEventQueryRaw);

      xmlHTTP.open('GET', 'base/app/sparql/queryAdverseEvent.sparql', false);
      xmlHTTP.send(null);
      adverseEventsQuery = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/queryAdverseEvent.sparql').respond(adverseEventsQuery);

      xmlHTTP.open('GET', 'base/app/sparql/deleteAdverseEvent.sparql', false);
      xmlHTTP.send(null);
      deleteAdverseEventRaw = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/deleteAdverseEvent.sparql').respond(deleteAdverseEventRaw);

      xmlHTTP.open('GET', 'base/app/sparql/editAdverseEvent.sparql', false);
      xmlHTTP.send(null);
      editAdverseEventRaw = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/editAdverseEvent.sparql').respond(editAdverseEventRaw);

      xmlHTTP.open('GET', 'base/app/sparql/queryMeasuredAt.sparql', false);
      xmlHTTP.send(null);
      queryAdverseEventMeasuredAtRaw = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/queryMeasuredAt.sparql').respond(queryAdverseEventMeasuredAtRaw);


      xmlHTTP.open('GET', 'base/test_graphs/testStudyGraph.ttl', false);
      xmlHTTP.send(null);
      graphAsText = xmlHTTP.responseText;

      httpBackend.flush();

      dropGraph(graphUri); 

      // setup mock dataset store
      createStoreDeferred = $q.defer();
      createStorePromise = createStoreDeferred.promise;
      remotestoreServiceStub.create.and.returnValue(createStorePromise);

      loadStoreDeferred = $q.defer();
      loadStorePromise = loadStoreDeferred.promise;
      remotestoreServiceStub.load.and.returnValue(loadStorePromise);

      var loadStorePromise = studyService.loadStore(graphAsText);
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();
      rootScope.$digest();
    }));

    describe('addItem', function() {

      it('should add the adverseEvent', function(done) {
        var executeUpdateAddAdverseEventDefferd = q.defer();
        var executeUpdateAddAdverseEventPromise = executeUpdateAddAdverseEventDefferd.promise;

        var adverseEvent = {
          label: 'adverse event label',
          measurementType: 'http://some-measurementType'
        };

        remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          console.log('graphUri = ' + uri);
          console.log('query = ' + query);

          var xmlHTTP = new XMLHttpRequest();
          xmlHTTP.open("POST", scratchStudyUri + '/update', false);
          xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
          xmlHTTP.send(query);

          return executeUpdateAddAdverseEventPromise;
        });

        var resultPromise = adverseEventService.addItem(adverseEvent);

        resultPromise.then(function(result) {
          var adverseEventsAsString = queryTeststore(adverseEventsQuery.replace(/\$graphUri/g, graphUri));
          var adverseEventsObject = deFusekify(adverseEventsAsString);
          expect(adverseEventsObject.length).toEqual(1);
          expect(adverseEventsObject[0]).toEqual(adverseEvent);
          done();
        });

        executeUpdateAddAdverseEventDefferd.resolve();
        rootScope.$digest();



      });
    });


  });
});