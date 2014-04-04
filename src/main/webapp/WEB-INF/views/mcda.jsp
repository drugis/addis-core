<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ page session="false"%>

<!DOCTYPE html>
<html ng-app="elicit">
<head>
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  <meta name="viewport" content="width=device-width" />

  <title>Preference elicitation</title>

  <link rel="stylesheet" type="text/css" href="mcda/app/js/bower_components/font-awesome/css/font-awesome.min.css">
  <link rel="stylesheet" type="text/css" href="mcda/app/js/bower_components/nprogress/nprogress.css">
  <link rel="stylesheet" type="text/css" href="mcda/app/js/lib/jslider/bin/jquery.slider.min.css">
  <link rel="stylesheet" type="text/css" href="mcda/app/js/bower_components/nvd3/src/nv.d3.css">
  <link rel="stylesheet" type="text/css" href="mcda/app/css/mcda-plain.css">

  <script src="mcda/app/js/bower_components/foundation/js/vendor/custom.modernizr.js"></script>

  <script src="mcda/app/js/bower_components/requirejs/require.js" data-main="mcda/app/js/main.js"></script>
</head>
<body>
  <div class="row">
    <div class="columns">
      <alert type="info">
        <strong>Disclaimer:</strong> this tool comes with <em>absolutely no warranty</em>, to the extent
        permitted by applicable law. No user data is stored or otherwise persisted,
        however no guarantees about the safety of your data can be made.
      </alert>
    </div>
  </div>
  <div ui-view></div>

  <script>
    window.patavi = { "WS_URI": "wss://patavi.drugis.org/ws" };
    window.config = { examplesRepository: "/examples/",
    workspacesRepository: { service: "LocalWorkspaces" } };
  </script>
</body>
</html>