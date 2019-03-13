/** 定义控制器层 */
app.controller('userMsgController', function($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});


    $scope.findProvinces = function () {
        baseService.sendGet("/user/findProvinces")
            .then(function (resopnse) {
                $scope.provinces = resopnse.data;
            })
    };

    $scope.findCities = function (provinceId) {
        baseService.sendGet("/user/findCities?provinceId=" + provinceId)
            .then(function (resopnse) {
                $scope.cities = resopnse.data;
            })
    };

    $scope.findCAreas = function (cityId) {
        baseService.sendGet("/user/findCAreas?cityId=" + cityId)
            .then(function (resopnse) {
                $scope.areas = resopnse.data;
            })
    };


    $scope.$watch('user.area.provinceId', function (newVal, oldVal) {
        if (newVal) {
            $scope.findCities(newVal);
        } else {
            $scope.cities = null;
        }
    });

    $scope.$watch('user.area.cityId', function (newVal, oldVal) {
        if (newVal) {
            $scope.findCAreas(newVal);
        } else {
            $scope.areas = null;
        }
    });

    $scope.saveMsg = function () {
        baseService.sendPost("/user/saveMsg", $scope.user)
            .then(function (response) {
                if (response) {
                    alert("保存成功");
                } else {
                    alert("保存失败");
                }
            })
    };

    $scope.uploadFile = function () {
        baseService.uploadFile()
            .then(function (response) {
                if (response.data.status == 200) {
                    $scope.user.headPic = response.data.url;
                }
            })
    };
});