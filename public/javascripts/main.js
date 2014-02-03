"use strict";

angular.module("app", ["ngRoute", "config"]);

angular.module("app").config(function ($routeProvider, $locationProvider, BASE_PATH) {

    $locationProvider.html5Mode(false);
    $locationProvider.hashPrefix("!");

    $routeProvider
        .when("/", {
            templateUrl: BASE_PATH + "templates/home/home.template.html",
            controller: "HomeController"
        })
        .when("/blobs", {
            templateUrl: BASE_PATH + "templates/blobs/blobs.template.html",
            controller: "BlobsController"
        })
        .when("/blobs/:blobId", {
            templateUrl: BASE_PATH + "templates/blob/blob.template.html",
            controller: "BlobController"
        })
        .when("/tags", {
            templateUrl: BASE_PATH + "templates/tags/tags.template.html",
            controller: "TagsController"
        })
        .otherwise({
            redirectTo: "/"
        });

});

angular.module("app").service("BlobService", function BlobService($http) {


    this.insert = function () {
        return $http.put("/api/blobs").then(function (response) {
            return response.data;
        });
    };

    this.selectAll = function () {
        return $http.get("/api/blobs").then(function (response) {
            return response.data;
        });
    };

    this.selectById = function (id) {
        return $http.get("/api/blobs/" + id).then(function (response) {
            return response.data;
        });
    };

    this.save = function (blob) {
        return $http.post("/api/blobs", blob).then(function (response) {
            return response.data;
        });
    };

    this.delete = function (blob) {
        return $http.delete("/api/blobs/" + blob.id).then(function (response) {
            return response.data;
        });
    };

    this.search = function (value) {
        return $http.get("/api/blobs/search/" + value).then(function (response) {
            return response.data;
        });
    };


});

angular.module("app").service("TagService", function TagService($http) {

    this.selectAll = function () {
        return $http.get("/api/tags").then(function (response) {
            return response.data;
        });
    };

});

angular.module("app").controller("HomeController", function HomeController($scope, BlobService) {

    $scope.search = function (value) {
        console.log("search", value);
        BlobService.search(value).then(function (searchResult) {
            console.log("search result", searchResult);
            $scope.hits = searchResult.hits.hits;
        });
    };

});

angular.module("app").controller("BlobsController", function BlobsController($scope, $location, BlobService) {

    function updateBlobList(scope) {
        BlobService.selectAll().then(function (blobs) {
            scope.blobs = blobs;
        });
    }

    $scope.createBlob = function () {
        BlobService.insert().then(function (blob) {
            updateBlobList($scope);
        });
    };

    $scope.edit = function (blob) {
        $location.path("/blobs/" + blob.id);
    };

    updateBlobList($scope);

});

angular.module("app").controller("BlobController", function ($scope, $location, $routeParams, BlobService) {

    $scope.save = function (blob) {
        BlobService.save(blob).then(function () {
            updateBlob($scope);
        });
    };

    $scope.delete = function (blob) {
        BlobService.delete(blob).then(function () {
            $location.path("/blobs");
        });
    };


    function updateBlob(scope) {
        BlobService.selectById($routeParams["blobId"]).then(function (blob) {
            scope.blob = blob;
        });
    }

    updateBlob($scope);

});

angular.module("app").controller("TagsController", function ($scope, TagService) {

    function updateTagList(scope) {
        TagService.selectAll().then(function (tags) {
            scope.tags = tags;

        });
    }

    updateTagList($scope);

});

angular.module("app").directive("tags", function (BASE_PATH) {

    return {
        restrict: "A",
        templateUrl: BASE_PATH + "templates/directives/tags/tag.template.html",
        replace: true,
        scope: {
            tags: "=",
            tagNameKey: "@"
        },
        controller: function ($scope) {

            function alreadyExists(tagName) {
                var result = false;
                var tags = $scope.tags;
                var tagNameKey = $scope.tagNameKey;
                for (var i = 0, count = tags.length; i < count; i++) {
                    result = result || tags[i][tagNameKey] === tagName;
                }
                return result;
            }

            $scope.delete = function (index) {
                var tags = $scope.tags;
                if (angular.isArray(tags)) {
                    tags.splice(index, 1);
                } else {
                    throw new Error("Oops...");
                }
            };

            $scope.addTag = function (tagName) {
                if (tagName && tagName.length > 0 && !alreadyExists(tagName)) {
                    var tag = {};
                    tag[$scope.tagNameKey] = tagName.trim();
                    $scope.tags.push(tag);
                }
            };


        }
    }

});

angular.module("app").directive("onKeyUp", function () {
    return {
        restrict: "A",
        scope: {
            onKeyUp: "&",
            keyCode: "="
        },
        link: function (scope, element) {

            function onKeyUp(event) {
                if (event.keyCode === scope.keyCode) {
                    scope.$apply(scope.onKeyUp);
                }
            }

            element.on("keyup", onKeyUp);


        }
    }
});

