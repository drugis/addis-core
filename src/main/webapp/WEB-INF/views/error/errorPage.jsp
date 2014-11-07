<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html ng-app="elicit">
<head>
<meta charset="utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="viewport" content="width=device-width" />
<link rel="shortcut icon" href="<c:url value="/app/img/favicon.ico" />" type="image/x-icon" />

<title>trialverse.drugis.org</title>
<link rel="stylesheet" type="text/css" href="<c:url value="/app/css/trialverse-drugis.css" />">

</head>
<body>
  <nav class="top-bar" data-topbar>
    <ul class="title-area">
      <li class="name">
        <h1>
          <a href="#">trialverse.drugis.org</a>
        </h1>
      </li>
    </ul>
  </nav>
    <section id="hero">
    <div class="row">
      <div class="large-12 columns">
        <h1><c:out value="${errorCode}"/><br/>
          <small><c:out value="${reasonPhrase}"/></small>
        </h1>
      </div>
    </div>
  </section>
</body>
</html>