"use strict";

angular.module("app", ["ngRoute", "config"]);

angular.module("app").config(function ($routeProvider, $locationProvider, BASE_PATH) {

    $locationProvider.html5Mode(false);
    $locationProvider.hashPrefix("!");

    $routeProvider.
        when("/", {
            templateUrl: BASE_PATH + "templates/home/home.template.html",
            controller: "HomeController"
        }).
        when("/blobs", {
            templateUrl: BASE_PATH + "templates/blobs/blobs.template.html",
            controller: "BlobsController"
        }).
        when("/blobs/:blobId", {
            templateUrl: BASE_PATH + "templates/blob/blob.template.html",
            controller: "BlobController"
        }).
        otherwise({
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


});

angular.module("app").controller("HomeController", function HomeController($scope) {

});

angular.module("app").controller("BlobsController", function BlobsController($scope, $location, BlobService) {

    function updateBlobList(scope) {
        BlobService.selectAll().then(function (blobs) {
            scope.blobs = blobs;
        });
    }

    $scope.createBlob = function () {
        BlobService.insert().then(function (blob) {
            console.log(blob);
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
