/** 定义首页控制器层 */
app.controller("indexController", function($scope, $controller, baseService){

    // 指定继承baseController
    $controller('baseController',{$scope:$scope});

    $scope.findContentByCategoryId = function (categoryId) {
        baseService.sendGet("/content/findContentByCategoryId?categoryId=" + categoryId)
            .then(function (response) {
                $scope.contentList = response.data;
            })
    };

    $scope.search = function () {
        var keywords = $scope.keywords ? $scope.keywords : "";
        location.href = "http://search.pinyougou.com?keywords=" + keywords;
    }
});