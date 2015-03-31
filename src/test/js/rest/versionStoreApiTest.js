var should = require('should');
var assert = require('assert');
var request = require('supertest');
var qs = require('querystring');
var path = require('path');
var fs = require('fs');

var versionStoreServerUrl = process.env.TRIPLESTORE_BASE_URI;
var newGraph = '<http://mockserver/someMockUuid> <http://purl.org/dc/terms/description> "description" ; ' +
  ' <http://purl.org/dc/terms/title> "my-title" . ';
var queryStudyWithDetails = fs.readFileSync(path.join(__dirname, '../../../main/webapp/resources/app/sparql/queryStudiesWithDetails.sparql'), 'utf8');

describe('create dataset ', function() {

  it('should return a 201 ( created ) and a location header and version', function(done) {
    request(versionStoreServerUrl)
      .post('datasets')
      .set('Content-Type', 'text/turtle')
      .send(newGraph)
      .end(function(err, res) {
        if (err) {
          console.log('err =  ' + err);
          throw err;
        }
        res.should.have.property('status', 201);
        res.headers.location.should.startWith('http://localhost:8080/datasets/');
        res.headers['x-eventsource-version'].should.startWith('http://localhost:8080/versions/');
        done();
      });

  });
});

describe('get dataset', function() {

  var versionedDatasetUrl;

  var expectedTtl =
    '<http://mockserver/someMockUuid>\n' +
    '        <http://purl.org/dc/terms/description>\n' +
    '                "description" ;\n' +
    '        <http://purl.org/dc/terms/title>\n' +
    '                "my-title" .\n';

  before(function(done) {
    request(versionStoreServerUrl)
      .post('datasets')
      .set('Content-Type', 'text/turtle')
      .send(newGraph)
      .end(function(err, res) {
        versionedDatasetUrl = res.headers.location;
        done();
      });
  });

  it('should return the dataset', function(done) {
    request(versionedDatasetUrl)
      .get('/data?default')
      .set('Accept', 'text/turtle')
      .end(function(err, res) {
        if (err) {
          console.log('err =  ' + err);
          throw err;
        }
        console.log('res = ' + JSON.stringify(res));
        res.should.have.property('status', 200);
        res.headers['content-type'].should.equal('text/turtle;charset=UTF-8');
        res.text.should.equal(expectedTtl);
        done();
      });
  });
});

describe('get versioned dataset', function() {

  var versionedDatasetUrl;
  var versionUrl;

  var expectedTtl =
    '<http://mockserver/someMockUuid>\n' +
    '        <http://purl.org/dc/terms/description>\n' +
    '                "description" ;\n' +
    '        <http://purl.org/dc/terms/title>\n' +
    '                "my-title" .\n';

  before(function(done) {
    request(versionStoreServerUrl)
      .post('datasets')
      .set('Content-Type', 'text/turtle')
      .send(newGraph)
      .end(function(err, res) {
        versionedDatasetUrl = res.headers.location;
        versionUrl = res.headers['x-eventsource-version'];
        done();
      });
  });

  it('should return the versioned dataset', function(done) {
    request(versionedDatasetUrl)
      .get('/data?default')
      .set('Accept', 'text/turtle')
      .set('X-Accept-EventSource-Version', versionUrl)
      .end(function(err, res) {
        if (err) {
          console.log('err =  ' + err);
          throw err;
        }
        console.log('res = ' + JSON.stringify(res));
        res.should.have.property('status', 200);
        res.headers['content-type'].should.equal('text/turtle;charset=UTF-8');
        res.text.should.equal(expectedTtl);
        done();
      });
  });
});


describe('create study', function() {

  var versionedDatasetUrl;
  var study = '<http://trials.drugis.org/studies/studyUuid>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
    ' a  <http://trials.drugis.org/ontology#Study> ; ' +
    ' <http://trials.drugis.org/ontology#has_epochs> () . ';

  // create a dataset to add the study to
  before(function(done) {
    request(versionStoreServerUrl)
      .post('datasets')
      .set('Content-Type', 'text/turtle')
      .send(newGraph)
      .end(function(err, res) {
        versionedDatasetUrl = res.headers.location;
        done();
      });
  });

  //
  it('should return a 201', function(done) {
    var studyUri = 'http://trials.drugis.org/studies/studyUuid';

    request(versionedDatasetUrl)
      .put('/data?graph=' + studyUri)
      .set('Content-Type', 'text/turtle')
      .send(study)
      .end(function(err, res) {
        if (err) {
          console.log('err =  ' + err);
          throw err;
        }
        console.log('res = ' + JSON.stringify(res));
        res.should.have.property('status', 200);
        done();
      });
  });
});

describe('when the study is created, query the details', function() {

  var versionedDatasetUrl;
  var studyUri = 'http://trials.drugis.org/studies/studyUuid';
  var study = '<http://trials.drugis.org/studies/studyUuid>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
    ' <http://www.w3.org/2000/01/rdf-schema#comment> "myComment" ;' +
    ' a  <http://trials.drugis.org/ontology#Study> ; ' +
    ' <http://trials.drugis.org/ontology#has_epochs> () . ';

  before(function(done) {
    request(versionStoreServerUrl)
      .post('datasets')
      .set('Content-Type', 'text/turtle')
      .send(newGraph)
      .end(function(err, res) {
        versionedDatasetUrl = res.headers.location;
        request(versionedDatasetUrl)
          .put('/data?graph=' + studyUri)
          .set('Content-Type', 'text/turtle')
          .send(study)
          .end(function(err, res) {
            if (err) {
              console.log('err =  ' + err);
              throw err;
            }
            console.log('res = ' + JSON.stringify(res));
            res.should.have.property('status', 200);
            done();
          });
      });
  });

  it('should return the details', function(done) {

    request(versionedDatasetUrl)
      .get('/query?query=' + encodeURIComponent(queryStudyWithDetails))
      .set('Content-Type', 'application/sparql-query')
      .set('Accept', 'application/sparql-results+json')
      .end(function(err, res) {
        if (err) {
          console.log('err =  ' + err);
          throw err;
        }
        console.log('study query res = ' + JSON.stringify(res));
        res.should.have.property('status', 200);
        JSON.parse(res.text).results.bindings[0].label.value.should.equal('mystudy');
        JSON.parse(res.text).results.bindings[0].title.value.should.equal('myComment');
        done();

      });
  });
});

describe('querying a specific dataset version', function() {
  describe('after creating a study', function() {


    var versionedDatasetUrl;
    var previousVersionUrl;
    var studyUri = 'http://trials.drugis.org/studies/studyUuid';
    var study = '<http://trials.drugis.org/studies/studyUuid>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
      ' <http://www.w3.org/2000/01/rdf-schema#comment> "myComment" ;' +
      ' a  <http://trials.drugis.org/ontology#Study> ; ' +
      ' <http://trials.drugis.org/ontology#has_epochs> () . ';

    before(function(done) {
      request(versionStoreServerUrl)
        .post('datasets')
        .set('Content-Type', 'text/turtle')
        .send(newGraph)
        .end(function(err, res) {
          versionedDatasetUrl = res.headers.location;
          previousVersionUrl = res.headers['x-eventsource-version'];
          request(versionedDatasetUrl)
            .put('/data?graph=' + studyUri)
            .set('Content-Type', 'text/turtle')
            .send(study)
            .end(function(err, res) {
              if (err) {
                console.log('err =  ' + err);
                throw err;
              }
              done();
            });
        });
    });

    it('the previous version should return the details without the new study', function(done) {

      request(versionedDatasetUrl)
        .get('/query?query=' + encodeURIComponent(queryStudyWithDetails))
        .set('Content-Type', 'application/sparql-query')
        .set('Accept', 'application/sparql-results+json')
        .set('X-Accept-EventSource-Version', previousVersionUrl)
        .end(function(err, res) {
          if (err) {
            console.log('err =  ' + err);
            throw err;
          }
          console.log('study query res = ' + JSON.stringify(res));
          res.should.have.property('status', 200);
          JSON.parse(res.text).results.bindings.length.should.equal(0);
          done();
        });
    });
  });
  describe('after creating two studies', function() {


    var versionedDatasetUrl;
    var previousVersionUrl;
    var studyUri1 = 'http://trials.drugis.org/studies/studyUuid1';
    var studyUri2 = 'http://trials.drugis.org/studies/studyUuid2';
    var study1 = '<http://trials.drugis.org/studies/studyUuid1>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
      ' <http://www.w3.org/2000/01/rdf-schema#comment> "myComment" ;' +
      ' a  <http://trials.drugis.org/ontology#Study> ; ' +
      ' <http://trials.drugis.org/ontology#has_epochs> () . ';
    var study2 = '<http://trials.drugis.org/studies/studyUuid2>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
      ' <http://www.w3.org/2000/01/rdf-schema#comment> "myComment" ;' +
      ' a  <http://trials.drugis.org/ontology#Study> ; ' +
      ' <http://trials.drugis.org/ontology#has_epochs> () . ';

    before(function(done) {
      request(versionStoreServerUrl)
        .post('datasets')
        .set('Content-Type', 'text/turtle')
        .send(newGraph)
        .end(function(err, res) {
          versionedDatasetUrl = res.headers.location;
          request(versionedDatasetUrl)
            .put('/data?graph=' + studyUri1)
            .set('Content-Type', 'text/turtle')
            .send(study1)
            .end(function(err, res) {
              previousVersionUrl = res.headers['x-eventsource-version'];
              request(versionedDatasetUrl)
                .put('/data?graph=' + studyUri2)
                .set('Content-Type', 'text/turtle')
                .send(study2)
                .end(done);
            });
        });
    });

    it('the previous version should return the details without the new study', function(done) {

      request(versionedDatasetUrl)
        .get('/query?query=' + encodeURIComponent(queryStudyWithDetails))
        .set('Content-Type', 'application/sparql-query')
        .set('Accept', 'application/sparql-results+json')
        .set('X-Accept-EventSource-Version', previousVersionUrl)
        .end(function(err, res) {
          if (err) {
            console.log('err =  ' + err);
            throw err;
          }
          console.log('study query res = ' + JSON.stringify(res));
          res.should.have.property('status', 200);
          JSON.parse(res.text).results.bindings.length.should.equal(1);
          done();
        });
    });
  });
});

//
//   node-debug -p 8030 _mocha jena-api-test.js
//
